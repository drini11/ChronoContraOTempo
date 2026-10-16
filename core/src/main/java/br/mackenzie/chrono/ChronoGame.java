package br.mackenzie.chrono;

import br.mackenzie.chrono.screens.MenuScreen;
import br.mackenzie.chrono.ui.Assets;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ChronoGame extends Game {
    public SpriteBatch batch;
    public Assets assets;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
        batch.dispose();
    }
}
