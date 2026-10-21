package br.mackenzie.chrono;

import br.mackenzie.chrono.input.GameInput;
import br.mackenzie.chrono.screens.MenuScreen;
import br.mackenzie.chrono.ui.Assets;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ChronoGame extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public Assets assets;
    public GameInput input;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        assets = new Assets();
        input = new GameInput();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        input.update(); // lê o botão uma única vez por quadro
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
        shapes.dispose();
        batch.dispose();
    }
}
