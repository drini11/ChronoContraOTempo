package br.mackenzie.chrono.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class Draw {
    public static final Color WHITE = new Color(1f, 1f, 1f, 1f);
    public static final Color GOLD = new Color(1f, 0.82f, 0.25f, 1f);
    public static final Color GREEN = new Color(0.4f, 0.9f, 0.45f, 1f);
    public static final Color RED = new Color(1f, 0.38f, 0.32f, 1f);
    public static final Color GRAY = new Color(0.72f, 0.74f, 0.8f, 1f);

    private static final GlyphLayout layout = new GlyphLayout();

    private Draw() {}
    public static void centered(SpriteBatch batch, BitmapFont font, String text, float cx, float top, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width / 2f, top);
        font.setColor(Color.WHITE);
    }

    public static void left(SpriteBatch batch, BitmapFont font, String text, float x, float top, Color color) {
        font.setColor(color);
        font.draw(batch, text, x, top);
        font.setColor(Color.WHITE);
    }
}
