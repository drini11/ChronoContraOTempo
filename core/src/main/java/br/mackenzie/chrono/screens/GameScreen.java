package br.mackenzie.chrono.screens;

import br.mackenzie.chrono.ChronoGame;
import br.mackenzie.chrono.audio.GameAudio;
import br.mackenzie.chrono.logic.BattleModel;
import br.mackenzie.chrono.logic.HitGrade;
import br.mackenzie.chrono.logic.Phase;
import br.mackenzie.chrono.logic.PlayerHealth;
import br.mackenzie.chrono.logic.RoundResult;
import br.mackenzie.chrono.logic.TimerMode;
import br.mackenzie.chrono.ui.Assets;
import br.mackenzie.chrono.ui.Draw;
import br.mackenzie.chrono.ui.Fighter;
import br.mackenzie.chrono.ui.FighterType;
import br.mackenzie.chrono.ui.Fmt;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import br.mackenzie.chrono.logic.PhaseStats;
import br.mackenzie.chrono.logic.RunSession;
import br.mackenzie.chrono.data.SessionLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class GameScreen extends BaseScreen {
    private enum State { INTRO, SHOW_TARGET, COUNTDOWN, RUNNING, RESULT }

    private static final float INTRO_LOCK = 0.8f;
    private static final float SHOW_TARGET_TIME = 2.0f;
    private static final float COUNTDOWN_TIME = 3.0f;
    private static final float RESULT_TIME = 2.0f;
    private static final float RESULT_TIME_END = 3.2f;
    private static final float TIMEOUT_EXTRA = 2.0f;
    private static final float FADE_FRACTION = 0.4f;
    private static final float PLAYER_IMPACT_DELAY = 0.25f;
    private static final float ENEMY_IMPACT_DELAY = 0.45f;
    private static final float FLASH_TIME = 0.25f;
    private static final float SHAKE_TIME = 0.30f;

    private static final float GROUND_Y = 125f;
    private static final float PLAYER_X = 250f;
    private static final float ENEMY_X = 1030f;
    private static final float CENTER_X = Assets.W / 2f;

    private static final FighterType[] ENEMIES = {FighterType.MINOTAUR, FighterType.GOLEM, FighterType.ONORHC};
    private static final String[] PAUSE_LABELS = {"Continuar", "Som", "Sair para o menu"};

    private static final class FloatText {
        final String text;
        final float x;
        float y;
        float life = 1.4f;
        final Color color;

        FloatText(String text, float x, float y, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = new Color(color);
        }
    }

    private final int phaseIndex;
    private final RunSession run;
    private final int attempt;
    private final Phase phase;
    private final BattleModel model;
    private final Texture background;
    private final Fighter chrono;
    private final Fighter enemy;
    private final Color tmp = new Color();
    private final List<FloatText> floats = new ArrayList<>();

    private State state = State.INTRO;
    private float stateTime;
    private float time;
    private int round;

    private float target;
    private long runStartNano;
    private double elapsedNow;
    private int lastTick;
    private float earlyMessage;
    private RoundResult last;
    private float resultDuration = RESULT_TIME;
    private float impactLeft;
    private boolean impactPending;
    private boolean fadeStarted;

    private int shownMonsterHp;
    private int shownChances;
    private int shownPlayerHp;
    private int shownScore;
    private int shownCombo;
    private float flashLeft;
    private float shakeLeft;
    private boolean paused;
    private int pauseSelected;

    public GameScreen(ChronoGame game, RunSession run, int phaseIndex) {
        super(game);
        this.run = run;
        this.phaseIndex = phaseIndex;
        this.attempt = run.nextAttempt(phaseIndex);
        this.phase = Phase.ALL[phaseIndex];
        this.model = new BattleModel(phase);
        this.background = game.assets.texture("backgrounds/bg_fase" + phase.number + ".jpg", false);
        this.chrono = new Fighter(game.assets, FighterType.CHRONO, PLAYER_X, GROUND_Y, true);
        this.enemy = new Fighter(game.assets, ENEMIES[phaseIndex], ENEMY_X, GROUND_Y, false);
        syncHud();
    }

    @Override
    public void show() {
        game.input.clear();
        game.audio.playMusic("music/fase" + phase.number + ".mp3");
    }

    @Override
    public void pause() {
        if (!paused) openPause();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.1f);

        if (!paused && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            openPause();
        } else if (paused && handlePauseInput()) {
            return;
        }
        if (paused) {
            draw();
            drawPause();
            return;
        }

        time += delta;

        update(delta);
        chrono.update(delta);
        enemy.update(delta);
        updateEffects(delta);
        draw();
    }

    private void update(float delta) {
        stateTime += delta;
        if (earlyMessage > 0f) earlyMessage -= delta;

        switch (state) {
            case INTRO:
                if (stateTime < INTRO_LOCK) game.input.clear();
                else if (game.input.poll() != null) startRound();
                break;

            case SHOW_TARGET:
                drainEarlyPresses();
                if (stateTime >= SHOW_TARGET_TIME) {
                    state = State.COUNTDOWN;
                    stateTime = 0f;
                    lastTick = 0;
                }
                break;

            case COUNTDOWN:
                drainEarlyPresses();
                int shown = countdownNumber();
                if (shown != lastTick) {
                    lastTick = shown;
                    game.audio.play(GameAudio.Sfx.TICK);
                }
                if (stateTime >= COUNTDOWN_TIME) startRunning();
                break;

            case RUNNING:
                updateRunning();
                break;

            case RESULT:
                updateResult(delta);
                break;
        }
    }

    private int countdownNumber() {
        return Math.max(1, 3 - (int) stateTime);
    }

    private void drainEarlyPresses() {
        while (game.input.poll() != null) earlyMessage = 1.2f;
    }

    private void startRound() {
        target = model.nextTarget();
        round++;
        if (round == 1) game.logger.beginPhase();
        state = State.SHOW_TARGET;
        stateTime = 0f;
        elapsedNow = 0.0;
        game.input.clear();
    }

    private void startRunning() {
        game.input.clear();
        runStartNano = System.nanoTime();
        state = State.RUNNING;
        stateTime = 0f;
    }

    private void updateRunning() {
        Long press;
        while ((press = game.input.poll()) != null) {
            if (press >= runStartNano) {
                finishRound(press);
                return;
            }
        }
        elapsedNow = (System.nanoTime() - runStartNano) / 1e9;
        if (elapsedNow > target + TIMEOUT_EXTRA) finishRound(null);
    }

    private void finishRound(Long pressNano) {

        double elapsed = (pressNano == null) ? Double.NaN : (pressNano - runStartNano) / 1e9;
        if (pressNano != null) elapsedNow = elapsed;

        last = model.submit(target, elapsed);
        game.logger.logRound(run.id, phase, attempt, last, model.player());

        if (last.damageDealt > 0) {
            chrono.playAttack(attackLevel(last.grade));
            impactLeft = PLAYER_IMPACT_DELAY;
        } else {
            enemy.playAttack(1 + MathUtils.random(2));
            impactLeft = ENEMY_IMPACT_DELAY;
        }
        impactPending = true;

        boolean ending = model.isMonsterDefeated() || model.isPlayerDefeated();
        resultDuration = ending ? RESULT_TIME_END : RESULT_TIME;
        state = State.RESULT;
        stateTime = 0f;
    }

    private static int attackLevel(HitGrade grade) {
        switch (grade) {
            case CRITICAL: return 3;
            case STRONG: return 2;
            default: return 1;
        }
    }

    private void updateResult(float delta) {
        game.input.clear();

        if (impactPending) {
            impactLeft -= delta;
            if (impactLeft <= 0f) applyImpact();
        }

        if (model.isMonsterDefeated() && !fadeStarted && enemy.isDeathFinished()) {
            enemy.fadeOut(0.8f);
            fadeStarted = true;
        }

        if (stateTime >= resultDuration) afterResult();
    }

    private void applyImpact() {
        impactPending = false;
        syncHud();

        if (last.damageDealt > 0) {
            enemy.playHit();
            game.audio.play(hitSound(last.grade));
            if (model.isMonsterDefeated()) {
                enemy.playDeath();
                game.audio.stopMusic();
                game.audio.play(GameAudio.Sfx.monsterDefeated(phase.number));
            }
            floats.add(new FloatText("-" + last.damageDealt, ENEMY_X, GROUND_Y + 330f, Draw.gradeColor(last.grade)));
            if (last.grade == HitGrade.CRITICAL) {
                flashLeft = FLASH_TIME;
                shakeLeft = SHAKE_TIME;
            }
        } else {
            chrono.playGlitch(); // "falha temporal"
            game.audio.play(GameAudio.Sfx.MISS);
            if (model.isPlayerDefeated()) {
                chrono.playDeath();
                game.audio.stopMusic();
                game.audio.play(GameAudio.Sfx.PLAYER_DEFEATED);
            }
            floats.add(new FloatText("-" + last.damageTaken + " HP", PLAYER_X, GROUND_Y + 310f, Draw.RED));
            shakeLeft = SHAKE_TIME * 0.8f;
        }
    }
    private void syncHud() {
        shownMonsterHp = model.monsterHp();
        shownChances = model.player().chances();
        shownPlayerHp = model.player().hp();
        shownScore = model.score();
        shownCombo = model.combo();
    }

    private static GameAudio.Sfx hitSound(HitGrade grade) {
        switch (grade) {
            case CRITICAL: return GameAudio.Sfx.HIT_CRITICAL;
            case STRONG: return GameAudio.Sfx.HIT_STRONG;
            default: return GameAudio.Sfx.HIT_WEAK;
        }
    }
    private void openPause() {
        paused = true;
        pauseSelected = 0;
        shakeLeft = 0f;
        flashLeft = 0f;
        game.audio.pauseMusic();
    }

    private boolean handlePauseInput() {
        while (game.input.poll() != null) {
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            resumeGame();
            return false;
        }
        int n = PAUSE_LABELS.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) pauseSelected = (pauseSelected + n - 1) % n;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) pauseSelected = (pauseSelected + 1) % n;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            switch (pauseSelected) {
                case 0:
                    resumeGame();
                    break;
                case 1:
                    game.audio.setMuted(!game.audio.isMuted());
                    break;
                default:
                    quitToMenu();
                    return true;
            }
        }
        return false;
    }

    private void resumeGame() {
        paused = false;
        game.input.clear();
        if (isRoundInterrupted()) {
            state = State.SHOW_TARGET;
            stateTime = 0f;
            elapsedNow = 0.0;
            lastTick = 0;
        }
        game.audio.resumeMusic();
    }

    private boolean isRoundInterrupted() {
        return state == State.COUNTDOWN || state == State.RUNNING;
    }

    private void quitToMenu() {
        if (model.roundsPlayed() > 0) {
            game.logger.logPhase(run.id, model.finish(attempt, false), SessionLogger.Outcome.ABANDONO);
        }
        game.setScreen(new MenuScreen(game));
    }

    private static float pauseItemTop(int i) {
        return 440f - i * 70f;
    }
    private void drawPause() {
        beginShapes();
        game.shapes.setColor(0f, 0f, 0f, 0.7f);
        game.shapes.rect(-50f, -50f, Assets.W + 100f, Assets.H + 100f);
        game.shapes.setColor(1f, 1f, 1f, 0.14f);
        game.shapes.rect(440f, pauseItemTop(pauseSelected) - 50f, 400f, 58f);
        game.shapes.end();

        SpriteBatch batch = game.batch;
        batch.begin();
        Draw.centered(batch, game.assets.large, "PAUSADO", CENTER_X, 590f, Draw.GOLD);
        for (int i = 0; i < PAUSE_LABELS.length; i++) {
            String label = PAUSE_LABELS[i];
            if (i == 1) label = game.audio.isMuted() ? "Som: desligado" : "Som: ligado";
            Color c = i == pauseSelected ? Draw.GOLD : Draw.WHITE;
            Draw.centered(batch, game.assets.medium, label, CENTER_X, pauseItemTop(i), c);
        }
        if (isRoundInterrupted()) {
            Draw.centered(batch, game.assets.small, "Ao continuar, a rodada recomeça com o mesmo alvo.",
                CENTER_X, 200f, Draw.GRAY);
        }
        Draw.centered(batch, game.assets.small, "↑↓ navegar  ·  ENTER selecionar  ·  ESC continuar",
            CENTER_X, 60f, Draw.GRAY);
        batch.end();
    }

    private void afterResult() {
        if (model.isMonsterDefeated()) finishPhase(true);
        else if (model.isPlayerDefeated()) finishPhase(false);
        else startRound();
    }

    private void finishPhase(boolean cleared) {
        PhaseStats stats = model.finish(attempt, cleared);
        if (cleared) run.addCleared(stats);
        game.logger.logPhase(run.id, stats, cleared ? SessionLogger.Outcome.VITORIA : SessionLogger.Outcome.DERROTA);

        EndScreen.Kind kind;
        if (!cleared) kind = EndScreen.Kind.DEFEAT;
        else if (phaseIndex == Phase.count() - 1) kind = EndScreen.Kind.VICTORY;
        else kind = EndScreen.Kind.CLEARED;

        game.setScreen(new EndScreen(game, run, phaseIndex, stats, kind));
    }

    private void updateEffects(float delta) {
        flashLeft = Math.max(0f, flashLeft - delta);
        shakeLeft = Math.max(0f, shakeLeft - delta);
        for (Iterator<FloatText> it = floats.iterator(); it.hasNext(); ) {
            FloatText f = it.next();
            f.life -= delta;
            f.y += 60f * delta; // sobe
            if (f.life <= 0f) it.remove();
        }
    }


    private void draw() {
        float sx = shakeLeft > 0f ? MathUtils.random(-7f, 7f) * (shakeLeft / SHAKE_TIME) : 0f;
        float sy = shakeLeft > 0f ? MathUtils.random(-7f, 7f) * (shakeLeft / SHAKE_TIME) : 0f;
        beginFrame(sx, sy);
        SpriteBatch batch = game.batch;

        batch.begin();
        batch.draw(background, 0f, 0f, Assets.W, Assets.H);
        chrono.draw(batch);
        enemy.draw(batch);
        batch.end();

        beginShapes();
        drawHudBars();
        drawCenterPanel();
        if (flashLeft > 0f) {
            game.shapes.setColor(1f, 1f, 1f, 0.45f * flashLeft / FLASH_TIME);
            game.shapes.rect(-50f, -50f, Assets.W + 100f, Assets.H + 100f);
        }
        game.shapes.end();

        batch.begin();
        drawHudTexts(batch);
        drawCenterTexts(batch);
        drawFloats(batch);
        batch.end();
    }


    private void drawHudBars() {
        ShapeRenderer s = game.shapes;

        for (int i = 0; i < PlayerHealth.CHANCES; i++) {
            float cx = 58f + i * 46f, cy = 640f;
            s.setColor(0f, 0f, 0f, 0.5f);
            s.circle(cx, cy, 20f);
            if (i < shownChances) {
                s.setColor(Draw.GOLD);
                s.circle(cx, cy, 16f);
                s.setColor(0.2f, 0.15f, 0.1f, 1f);
                s.rectLine(cx, cy, cx, cy + 10f, 3f);
                s.rectLine(cx, cy, cx + 7f, cy - 3f, 3f);
            } else {
                s.setColor(0.3f, 0.3f, 0.35f, 1f);
                s.circle(cx, cy, 14f);
            }
        }

        float ratio = shownPlayerHp / (float) PlayerHealth.HP_PER_CHANCE;
        for (int i = 0; i < PlayerHealth.HP_PER_CHANCE; i++) {
            if (i < shownPlayerHp) s.setColor(1f - ratio * 0.6f, 0.3f + ratio * 0.6f, 0.3f, 1f);
            else s.setColor(0f, 0f, 0f, 0.5f);
            s.rect(40f + i * 30f, 590f, 26f, 22f);
        }

        float bx = 820f, by = 640f, bw = 420f, bh = 30f;
        s.setColor(0f, 0f, 0f, 0.55f);
        s.rect(bx - 4f, by - 4f, bw + 8f, bh + 8f);
        s.setColor(0.85f, 0.2f, 0.2f, 1f);
        s.rect(bx, by, bw * (shownMonsterHp / (float) phase.monsterHp), bh);
    }

    private void drawCenterPanel() {
        game.shapes.setColor(0f, 0f, 0f, state == State.INTRO ? 0.55f : 0.4f);
        if (state == State.INTRO) game.shapes.rect(CENTER_X - 400f, 290f, 800f, 270f);
        else if (state == State.RESULT) game.shapes.rect(CENTER_X - 240f, 255f, 480f, 345f);
        else game.shapes.rect(CENTER_X - 190f, 300f, 380f, 300f);
    }

    private void drawHudTexts(SpriteBatch batch) {
        Assets a = game.assets;
        Draw.left(batch, a.small, "CHRONO", 40f, 700f, Draw.WHITE);
        Draw.left(batch, a.small, "Pontos: " + shownScore, 40f, 560f, Draw.WHITE);
        if (shownCombo > 1) Draw.left(batch, a.small, "Combo x" + shownCombo, 40f, 528f, Draw.GOLD);

        Draw.right(batch, a.small, phase.monsterName.toUpperCase(), 1240f, 700f, Draw.WHITE);
        Draw.right(batch, a.small, shownMonsterHp + " / " + phase.monsterHp, 1240f, 608f, Draw.GRAY);

        Draw.centered(batch, a.small, phase.name + "  ·  Rodada " + Math.max(round, 1), CENTER_X, 700f, Draw.GRAY);
        Draw.centered(batch, a.small, "ESC: menu", CENTER_X, 36f, tmp.set(1f, 1f, 1f, 0.45f));
    }

    private void drawCenterTexts(SpriteBatch batch) {
        Assets a = game.assets;
        switch (state) {
            case INTRO:
                Draw.centered(batch, a.large, phase.name.toUpperCase(), CENTER_X, 545f, Draw.GOLD);
                Draw.centered(batch, a.medium, phase.monsterName, CENTER_X, 470f, Draw.WHITE);
                Draw.centered(batch, a.small, "Crítico: erro de até ±" + Fmt.sec3(HitGrade.CRITICAL.margin(phase.marginScale))
                    + " s  ·  " + timerModeText(), CENTER_X, 418f, Draw.GRAY);
                tmp.set(1f, 1f, 1f, 0.55f + 0.45f * MathUtils.sin(time * 4f));
                Draw.centered(batch, a.medium, "Acione o botão para começar", CENTER_X, 350f, tmp);
                break;

            case SHOW_TARGET:
                Draw.centered(batch, a.medium, "TEMPO-ALVO", CENTER_X, 590f, Draw.GOLD);
                Draw.centered(batch, a.huge, Fmt.target(target), CENTER_X, 545f, Draw.GOLD);
                Draw.centered(batch, a.small, "segundos  ·  memorize!", CENTER_X, 335f, Draw.GRAY);
                break;

            case COUNTDOWN:
                Draw.centered(batch, a.medium, "PREPARE-SE", CENTER_X, 590f, Draw.WHITE);
                Draw.centered(batch, a.huge, String.valueOf(countdownNumber()), CENTER_X, 545f, Draw.WHITE);
                Draw.centered(batch, a.small, "lembre do tempo-alvo", CENTER_X, 335f, Draw.GRAY);
                break;

            case RUNNING:
                drawRunning(batch);
                break;

            case RESULT:
                drawResult(batch);
                break;
        }

        if (earlyMessage > 0f && (state == State.SHOW_TARGET || state == State.COUNTDOWN)) {
            Draw.centered(batch, a.small, "Calma! Espere o cronômetro começar.", CENTER_X, 285f, Draw.RED);
        }
    }

    private String timerModeText() {
        switch (phase.timerMode) {
            case VISIBLE: return "cronômetro sempre visível";
            case FADE_OUT: return "cronômetro some no meio do caminho";
            default: return "cronômetro oculto";
        }
    }

    private void drawRunning(SpriteBatch batch) {
        Assets a = game.assets;
        boolean visible = phase.timerMode == TimerMode.VISIBLE
            || (phase.timerMode == TimerMode.FADE_OUT && elapsedNow < target * FADE_FRACTION);

        Draw.centered(batch, a.medium, visible ? "CRONÔMETRO" : "CRONÔMETRO OCULTO", CENTER_X, 590f, Draw.WHITE);
        if (visible) {
            Draw.centered(batch, a.huge, Fmt.sec2(elapsedNow), CENTER_X, 545f, Draw.WHITE);
        } else {
            tmp.set(1f, 1f, 1f, 0.55f + 0.45f * MathUtils.sin(time * 5f));
            Draw.centered(batch, a.huge, "?", CENTER_X, 545f, tmp);
        }
        Draw.centered(batch, a.small, "acione o botão no momento certo!", CENTER_X, 335f, Draw.GRAY);
    }

    private void drawResult(SpriteBatch batch) {
        Assets a = game.assets;
        if (last == null) return;

        if (last.timedOut) {
            Draw.centered(batch, a.large, "TEMPO ESGOTADO", CENTER_X, 580f, Draw.RED);
        } else {
            Color c = Draw.gradeColor(last.grade);
            Draw.centered(batch, a.large, last.grade.label, CENTER_X, 580f, c);
            Draw.centered(batch, a.medium, "Você parou em " + Fmt.sec(last.elapsed), CENTER_X, 490f, Draw.WHITE);
            Draw.centered(batch, a.medium, Fmt.signed(last.signedError()), CENTER_X, 440f, c);
        }
        Draw.centered(batch, a.small, "Alvo: " + Fmt.target(last.target) + " s", CENTER_X, 385f, Draw.GRAY);

        if (last.damageDealt > 0) {
            Draw.centered(batch, a.medium, "Dano " + last.damageDealt + "  ·  +" + last.scoreGained + " pts",
                CENTER_X, 345f, Draw.GOLD);
        } else {
            Draw.centered(batch, a.medium, "−" + last.damageTaken + " de vida", CENTER_X, 345f, Draw.RED);
        }

        if (model.isMonsterDefeated()) {
            Draw.centered(batch, a.medium, "MONSTRO DERROTADO!", CENTER_X, 300f, Draw.GOLD);
        } else if (model.isPlayerDefeated()) {
            Draw.centered(batch, a.medium, "SUAS CHANCES ACABARAM...", CENTER_X, 300f, Draw.RED);
        } else if (last.chanceLost) {
            Draw.centered(batch, a.small, "Você perdeu uma chance!", CENTER_X, 300f, Draw.RED);
        }
    }

    private void drawFloats(SpriteBatch batch) {
        for (FloatText f : floats) {
            tmp.set(f.color.r, f.color.g, f.color.b, MathUtils.clamp(f.life / 0.5f, 0f, 1f));
            Draw.centered(batch, game.assets.large, f.text, f.x, f.y, tmp);
        }
    }
}
