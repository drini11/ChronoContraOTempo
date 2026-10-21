package br.mackenzie.chrono.screens;

import br.mackenzie.chrono.ChronoGame;
import br.mackenzie.chrono.ui.Assets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

abstract class BaseScreen implements Screen {
    protected final ChronoGame game;
    protected final OrthographicCamera camera = new OrthographicCamera();
    protected final Viewport viewport = new FitViewport(Assets.W, Assets.H, camera);

    protected BaseScreen(ChronoGame game) {
        this.game = game;
    }

    protected void beginFrame() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        camera.position.set(Assets.W / 2f, Assets.H / 2f, 0f);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapes.setProjectionMatrix(camera.combined);
    }

    protected void beginShapes() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
