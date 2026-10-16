package br.mackenzie.chrono.screens;

import br.mackenzie.chrono.ChronoGame;
import br.mackenzie.chrono.ui.Assets;
import br.mackenzie.chrono.ui.Draw;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public final class MenuScreen extends BaseScreen {
    private final Texture background;
    private final Color blink = new Color(1f, 1f, 1f, 1f);
    private float time;

    public MenuScreen(ChronoGame game) {
        super(game);
        background = game.assets.texture("backgrounds/bg_menu.jpg", false);
    }

    @Override
    public void render(float delta) {
        time += delta;
        beginFrame();

        SpriteBatch batch = game.batch;
        batch.begin();

        batch.setColor(0.55f, 0.55f, 0.55f, 1f);
        batch.draw(background, 0f, 0f, Assets.W, Assets.H);
        batch.setColor(Color.WHITE);

        Draw.centered(batch, game.assets.large, "CHRONO CONTRA O TEMPO", Assets.W / 2f, 460f, Draw.GOLD);
        Draw.centered(batch, game.assets.small, "Jogo de reabilitação com precisão de tempo",
            Assets.W / 2f, 380f, Draw.GRAY);

        blink.a = 0.55f + 0.45f * MathUtils.sin(time * 4f);
        Draw.centered(batch, game.assets.medium, "Pressione ENTER", Assets.W / 2f, 230f, blink);

        batch.end();
    }
}
