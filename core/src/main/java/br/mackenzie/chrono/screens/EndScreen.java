package br.mackenzie.chrono.screens;

import br.mackenzie.chrono.ChronoGame;
import br.mackenzie.chrono.logic.PhaseStats;
import br.mackenzie.chrono.logic.RunSession;
import br.mackenzie.chrono.ui.Assets;
import br.mackenzie.chrono.ui.Draw;
import br.mackenzie.chrono.ui.Fmt;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class EndScreen extends BaseScreen {
    public enum Kind {
        CLEARED,
        DEFEAT,
        VICTORY
    }

    private static final float BUTTON_LOCK = 1.2f;

    private final RunSession run;
    private final int phaseIndex;
    private final PhaseStats stats;
    private final Kind kind;
    private final Texture background;
    private final Color tmp = new Color();
    private float time;

    public EndScreen(ChronoGame game, RunSession run, int phaseIndex, PhaseStats stats, Kind kind) {
        super(game);
        this.run = run;
        this.phaseIndex = phaseIndex;
        this.stats = stats;
        this.kind = kind;
        this.background = game.assets.texture("backgrounds/bg_fase" + stats.phase.number + ".jpg", false);
    }

    @Override
    public void show() {
        game.input.clear();
    }

    @Override
    public void render(float delta) {
        time += delta;

        boolean buttonPressed = false;
        while (game.input.poll() != null) {
            if (time >= BUTTON_LOCK) buttonPressed = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }
        if (buttonPressed || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            proceed();
            return;
        }
        draw();
    }

    private void proceed() {
        switch (kind) {
            case CLEARED:
                game.setScreen(new GameScreen(game, run, phaseIndex + 1));
                break;
            case DEFEAT:
                game.setScreen(new GameScreen(game, run, phaseIndex));
                break;
            default:
                game.setScreen(new MenuScreen(game));
                break;
        }
    }

    private void draw() {
        beginFrame();
        SpriteBatch batch = game.batch;

        batch.begin();
        if (kind == Kind.DEFEAT) batch.setColor(0.45f, 0.2f, 0.2f, 1f);
        else batch.setColor(0.35f, 0.35f, 0.35f, 1f);
        batch.draw(background, 0f, 0f, Assets.W, Assets.H);
        batch.setColor(Color.WHITE);
        batch.end();

        beginShapes();
        game.shapes.setColor(0f, 0f, 0f, 0.5f);
        game.shapes.rect(240f, 130f, 800f, 440f);
        game.shapes.end();

        batch.begin();
        String title;
        Color titleColor;
        switch (kind) {
            case DEFEAT:
                title = "DERROTA";
                titleColor = Draw.RED;
                break;
            case VICTORY:
                title = "VOCÊ VENCEU!";
                titleColor = Draw.GOLD;
                break;
            default:
                title = "FASE CONCLUÍDA!";
                titleColor = Draw.GOLD;
                break;
        }
        Draw.centered(batch, game.assets.large, title, Assets.W / 2f, 690f, titleColor);
        Draw.centered(batch, game.assets.medium,
            stats.phase.name + "  ·  " + stats.phase.monsterName + "  ·  tentativa " + stats.attempt,
            Assets.W / 2f, 615f, Draw.GRAY);

        if (kind == Kind.VICTORY) drawSummaryTable(batch);
        else drawStatsList(batch);

        if (time >= BUTTON_LOCK) {
            String hint;
            switch (kind) {
                case CLEARED: hint = "Acione o botão para a próxima fase"; break;
                case DEFEAT: hint = "Acione o botão para tentar de novo"; break;
                default: hint = "Acione o botão para voltar ao menu"; break;
            }
            tmp.set(1f, 1f, 1f, 0.55f + 0.45f * MathUtils.sin(time * 4f));
            Draw.centered(batch, game.assets.medium, hint, Assets.W / 2f, 100f, tmp);
        }
        Draw.right(batch, game.assets.small, "ENTER: continuar  ·  M: menu", 1240f, 700f, Draw.GRAY);
        batch.end();
    }

    private void row(SpriteBatch batch, String label, String value, float top, Color valueColor) {
        Draw.left(batch, game.assets.medium, label, 290f, top, Draw.WHITE);
        Draw.right(batch, game.assets.medium, value, 990f, top, valueColor);
    }

    private void drawStatsList(SpriteBatch batch) {
        float y = 545f;
        float step = 44f;
        row(batch, "Rodadas", String.valueOf(stats.rounds), y, Draw.WHITE);
        y -= step;
        row(batch, "Acertos", stats.hits + "  (" + Fmt.pct(stats.hitRate()) + ")", y, Draw.GREEN);
        y -= step;
        row(batch, "Erros", String.valueOf(stats.misses), y, stats.misses > 0 ? Draw.RED : Draw.WHITE);
        y -= step;
        row(batch, "Erro médio (precisão)", Fmt.sec(stats.meanAbsError), y, Draw.WHITE);
        y -= step;
        row(batch, "Desvio padrão (consistência)", Fmt.sec(stats.stdDev), y, Draw.WHITE);
        y -= step;
        row(batch, "Melhor tentativa", Fmt.sec(stats.bestError), y, Draw.WHITE);
        y -= step;
        row(batch, "Maior combo", "x" + stats.bestCombo, y, Draw.GOLD);
        y -= step;
        if (stats.cleared) {
            row(batch, "Bônus de consistência", "+" + stats.consistencyBonus, y, Draw.GOLD);
            y -= step;
        }
        row(batch, "Pontos da fase", String.valueOf(stats.score), y, Draw.GOLD);
    }

    private void drawSummaryTable(SpriteBatch batch) {
        Assets a = game.assets;
        float y = 545f;
        Draw.left(batch, a.small, "FASE", 290f, y, Draw.GRAY);
        Draw.right(batch, a.small, "ACERTOS", 560f, y, Draw.GRAY);
        Draw.right(batch, a.small, "ERRO MÉDIO", 760f, y, Draw.GRAY);
        Draw.right(batch, a.small, "DESVIO PADRÃO", 930f, y, Draw.GRAY);
        Draw.right(batch, a.small, "PONTOS", 1010f, y, Draw.GRAY);
        y -= 50f;

        for (PhaseStats ps : run.cleared()) {
            Draw.left(batch, a.medium, "Fase " + ps.phase.number, 290f, y, Draw.WHITE);
            Draw.right(batch, a.medium, ps.hits + "/" + ps.rounds, 560f, y, Draw.WHITE);
            Draw.right(batch, a.medium, Fmt.sec(ps.meanAbsError), 760f, y, Draw.WHITE);
            Draw.right(batch, a.medium, Fmt.sec(ps.stdDev), 930f, y, Draw.WHITE);
            Draw.right(batch, a.medium, String.valueOf(ps.score), 1010f, y, Draw.GOLD);
            y -= 52f;
        }

        y -= 20f;
        Draw.left(batch, a.medium, "TOTAL", 290f, y, Draw.GOLD);
        Draw.right(batch, a.large, String.valueOf(run.totalScore()), 1010f, y + 14f, Draw.GOLD);
    }
}
