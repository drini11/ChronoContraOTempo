package br.mackenzie.chrono.screens;

import br.mackenzie.chrono.ChronoGame;
import br.mackenzie.chrono.logic.BattleModel;
import br.mackenzie.chrono.logic.HitGrade;
import br.mackenzie.chrono.logic.Phase;
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
import com.badlogic.gdx.math.MathUtils;

public final class GameScreen extends BaseScreen {
    private enum State { INTRO, SHOW_TARGET, COUNTDOWN, RUNNING, RESULT }
    private static final float INTRO_LOCK = 0.8f;
    private static final float SHOW_TARGET_TIME = 2.0f;
    private static final float COUNTDOWN_TIME = 3.0f;
    private static final float RESULT_TIME = 2.0f;
    private static final float TIMEOUT_EXTRA = 2.0f;
    private static final float FADE_FRACTION = 0.4f;
    private static final float GROUND_Y = 125f;
    private static final float PLAYER_X = 250f;
    private static final float ENEMY_X = 1030f;
    private static final float CENTER_X = Assets.W / 2f;
    private static final FighterType[] ENEMIES = {FighterType.MINOTAUR, FighterType.GOLEM, FighterType.ONORHC};
    private final Phase phase;
    private final BattleModel model;
    private final Texture background;
    private final Fighter chrono;
    private final Fighter enemy;
    private final Color tmp = new Color();
    private State state = State.INTRO;
    private float stateTime;
    private float time;
    private int round;
    private float target;
    private long runStartNano;
    private double elapsedNow;
    private int lastTick;
    private float earlyMessage;
    private double resultElapsed;
    private boolean resultTimedOut;
    private HitGrade resultGrade = HitGrade.MISS;

    public GameScreen(ChronoGame game, int phaseIndex) {
        super(game);
        this.phase = Phase.ALL[phaseIndex];
        this.model = new BattleModel(phase);
        this.background = game.assets.texture("backgrounds/bg_fase" + phase.number + ".jpg", false);
        this.chrono = new Fighter(game.assets, FighterType.CHRONO, PLAYER_X, GROUND_Y, true);
        this.enemy = new Fighter(game.assets, ENEMIES[phaseIndex], ENEMY_X, GROUND_Y, false);
    }

    @Override
    public void show() {
        game.input.clear();
    }


    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.1f);
        time += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        update(delta);
        chrono.update(delta);
        enemy.update(delta);
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
                }
                if (stateTime >= COUNTDOWN_TIME) startRunning();
                break;

            case RUNNING:
                updateRunning();
                break;

            case RESULT:
                game.input.clear();
                if (stateTime >= RESULT_TIME) startRound();
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
        resultTimedOut = (pressNano == null);
        if (resultTimedOut) {
            resultElapsed = Double.NaN;
            resultGrade = HitGrade.MISS;
        } else {
            resultElapsed = (pressNano - runStartNano) / 1e9;
            elapsedNow = resultElapsed;
            resultGrade = HitGrade.of(Math.abs(resultElapsed - target), phase.marginScale);
        }
        state = State.RESULT;
        stateTime = 0f;
    }


    private void draw() {
        beginFrame();
        SpriteBatch batch = game.batch;

        batch.begin();
        batch.draw(background, 0f, 0f, Assets.W, Assets.H);
        chrono.draw(batch);
        enemy.draw(batch);
        batch.end();

        beginShapes();
        drawCenterPanel();
        game.shapes.end();

        batch.begin();
        Draw.centered(batch, game.assets.small, phase.name + "  ·  Rodada " + Math.max(round, 1),
            CENTER_X, 700f, Draw.GRAY);
        Draw.centered(batch, game.assets.small, "ESC: menu", CENTER_X, 36f, tmp.set(1f, 1f, 1f, 0.45f));
        drawCenterTexts(batch);
        batch.end();
    }

    private void drawCenterPanel() {
        game.shapes.setColor(0f, 0f, 0f, state == State.INTRO ? 0.55f : 0.4f);
        if (state == State.INTRO) game.shapes.rect(CENTER_X - 400f, 290f, 800f, 270f);
        else game.shapes.rect(CENTER_X - 190f, 300f, 380f, 300f);
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
        if (resultTimedOut) {
            Draw.centered(batch, a.large, "TEMPO ESGOTADO", CENTER_X, 580f, Draw.RED);
        } else {
            Color c = Draw.gradeColor(resultGrade);
            Draw.centered(batch, a.large, resultGrade.label, CENTER_X, 580f, c);
            Draw.centered(batch, a.medium, "Você parou em " + Fmt.sec(resultElapsed), CENTER_X, 490f, Draw.WHITE);
            Draw.centered(batch, a.medium, Fmt.signed(resultElapsed - target), CENTER_X, 440f, c);
        }
        Draw.centered(batch, a.small, "Alvo: " + Fmt.target(target) + " s", CENTER_X, 385f, Draw.GRAY);
    }
}
