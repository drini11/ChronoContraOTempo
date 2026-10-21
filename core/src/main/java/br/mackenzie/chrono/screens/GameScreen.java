package br.mackenzie.chrono.screens;

import br.mackenzie.chrono.ChronoGame;
import br.mackenzie.chrono.logic.Phase;
import br.mackenzie.chrono.ui.Assets;
import br.mackenzie.chrono.ui.Draw;
import br.mackenzie.chrono.ui.Fighter;
import br.mackenzie.chrono.ui.FighterType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public final class GameScreen extends BaseScreen {

    private static final float GROUND_Y = 125f;
    private static final float PLAYER_X = 250f;
    private static final float ENEMY_X = 1030f;

    private static final FighterType[] ENEMIES = {FighterType.MINOTAUR, FighterType.GOLEM, FighterType.ONORHC};

    private final Phase phase;
    private final Texture background;
    private final Fighter chrono;
    private final Fighter enemy;

    public GameScreen(ChronoGame game, int phaseIndex) {
        super(game);
        this.phase = Phase.ALL[phaseIndex];
        this.background = game.assets.texture("backgrounds/bg_fase" + phase.number + ".jpg", false);
        this.chrono = new Fighter(game.assets, FighterType.CHRONO, PLAYER_X, GROUND_Y, true);
        this.enemy = new Fighter(game.assets, ENEMIES[phaseIndex], ENEMY_X, GROUND_Y, false);
    }

    @Override
    public void render(float delta) {
        handleTestKeys();
        chrono.update(delta);
        enemy.update(delta);

        beginFrame();
        SpriteBatch batch = game.batch;
        batch.begin();
        batch.draw(background, 0f, 0f, Assets.W, Assets.H);
        chrono.draw(batch);
        enemy.draw(batch);

        Draw.left(batch, game.assets.small, phase.name + " - " + phase.monsterName + "  (teste de animações)", 30f, 700f, Draw.GOLD);
        Draw.left(batch, game.assets.small, "CHRONO:  1 / 2 / 3 ataques  ·  H dano  ·  G falha temporal  ·  D morte", 30f, 665f, Draw.WHITE);
        Draw.left(batch, game.assets.small, "INIMIGO:  Q / W ataques  ·  E dano  ·  T morte", 30f, 630f, Draw.WHITE);
        Draw.left(batch, game.assets.small, "R reiniciar  ·  ESC voltar ao menu", 30f, 595f, Draw.GRAY);
        batch.end();
    }

    private void handleTestKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) game.setScreen(new MenuScreen(game));

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) chrono.playAttack(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) chrono.playAttack(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) chrono.playAttack(3);
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) chrono.playHit();
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) chrono.playGlitch();
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) chrono.playDeath();

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) enemy.playAttack(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) enemy.playAttack(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) enemy.playHit();
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) enemy.playDeath();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            chrono.reset();
            enemy.reset();
        }
    }
}
