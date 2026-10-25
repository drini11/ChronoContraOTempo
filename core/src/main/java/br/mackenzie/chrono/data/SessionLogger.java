package br.mackenzie.chrono.data;

import br.mackenzie.chrono.logic.HitGrade;
import br.mackenzie.chrono.logic.Phase;
import br.mackenzie.chrono.logic.PhaseStats;
import br.mackenzie.chrono.logic.PlayerHealth;
import br.mackenzie.chrono.logic.RoundResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SessionLogger {
    public enum Outcome { VITORIA, DERROTA, ABANDONO }

    public static final String ROUNDS_FILE = "rodadas.csv";
    public static final String PHASES_FILE = "fases.csv";

    private static final String SEP = ";";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String ROUNDS_HEADER = String.join(SEP,
        "data_hora", "sessao", "fase", "monstro", "tentativa", "rodada",
        "alvo_s", "tempo_s", "erro_s", "erro_abs_s", "classificacao", "acertou",
        "dano_causado", "dano_recebido", "perdeu_chance", "combo", "chances", "vida", "tempo_esgotado");

    private static final String PHASES_HEADER = String.join(SEP,
        "data_hora", "sessao", "fase", "monstro", "tentativa", "resultado",
        "rodadas", "acertos", "erros", "tempos_esgotados", "taxa_acerto_pct",
        "erro_medio_s", "desvio_padrao_s", "melhor_erro_s", "maior_combo",
        "bonus_consistencia", "pontos", "duracao_s");

    private final Path folder;
    private long phaseStartNano = -1;
    private boolean ok = true;
    private String lastError = "";

    public SessionLogger(Path folder) {
        this.folder = folder;
    }

    public static Path defaultFolder() {
        return Paths.get(System.getProperty("user.home"), "ChronoContraOTempo", "dados");
    }

    public void beginPhase() {
        phaseStartNano = System.nanoTime();
    }

    public void logRound(String sessionId, Phase phase, int attempt, RoundResult r, PlayerHealth player) {
        String line = String.join(SEP,
            now(), sessionId, String.valueOf(phase.number), phase.monsterName,
            String.valueOf(attempt), String.valueOf(r.round),
            num(r.target, 1), num(r.elapsed, 3), num(r.signedError(), 3), num(r.absError(), 3),
            grade(r.grade), bit(r.damageDealt > 0),
            String.valueOf(r.damageDealt), String.valueOf(r.damageTaken), bit(r.chanceLost),
            String.valueOf(r.combo), String.valueOf(player.chances()), String.valueOf(player.hp()),
            bit(r.timedOut));
        append(ROUNDS_FILE, ROUNDS_HEADER, line);
    }

    public void logPhase(String sessionId, PhaseStats s, Outcome outcome) {
        double duration = phaseStartNano < 0 ? Double.NaN : (System.nanoTime() - phaseStartNano) / 1e9;
        phaseStartNano = -1;

        String line = String.join(SEP,
            now(), sessionId, String.valueOf(s.phase.number), s.phase.monsterName,
            String.valueOf(s.attempt), outcome.name(),
            String.valueOf(s.rounds), String.valueOf(s.hits), String.valueOf(s.misses),
            String.valueOf(s.timeouts), num(s.hitRate(), 1),
            num(s.meanAbsError, 3), num(s.stdDev, 3), num(s.bestError, 3),
            String.valueOf(s.bestCombo), String.valueOf(s.consistencyBonus), String.valueOf(s.score),
            num(duration, 1));
        append(PHASES_FILE, PHASES_HEADER, line);
    }

    public boolean ok() { return ok; }
    public String lastError() { return lastError; }
    public Path folder() { return folder; }



    private void append(String fileName, String header, String line) {
        try {
            Files.createDirectories(folder);
            Path file = folder.resolve(fileName);
            StringBuilder sb = new StringBuilder();
            if (!Files.exists(file) || Files.size(file) == 0) {
                sb.append('\uFEFF');
                sb.append(header).append(System.lineSeparator());
            }
            sb.append(line).append(System.lineSeparator());
            Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            ok = true;
        } catch (IOException | RuntimeException e) {
            ok = false;
            lastError = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            System.err.println("[SessionLogger] erro ao gravar " + fileName + ": " + lastError);
        }
    }

    private static String now() {
        return LocalDateTime.now().format(STAMP);
    }

    private static String num(double v, int decimals) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "";
        return String.format(Locale.US, "%." + decimals + "f", v).replace('.', ',');
    }

    private static String bit(boolean b) {
        return b ? "1" : "0";
    }

    private static String grade(HitGrade g) {
        switch (g) {
            case CRITICAL: return "CRITICO";
            case STRONG: return "FORTE";
            case WEAK: return "FRACO";
            default: return "ERRO";
        }
    }
}
