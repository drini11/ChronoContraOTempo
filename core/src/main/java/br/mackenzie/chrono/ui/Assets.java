package br.mackenzie.chrono.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

public final class Assets implements Disposable {
    public static final int W = 1280;
    public static final int H = 720;

    private static final String ACCENTS = "ÁÀÂÃÉÊÍÓÔÕÚÇáàâãéêíóôõúç–—−…·←→↑↓×";

    public final BitmapFont small;
    public final BitmapFont medium;
    public final BitmapFont large;
    public final BitmapFont huge;

    private final Map<String, Texture> textures = new HashMap<>();

    public Assets() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/main.ttf"));
        String chars = FreeTypeFontGenerator.DEFAULT_CHARS + ACCENTS;
        small = font(gen, 24, chars, 2f);
        medium = font(gen, 34, chars, 2f);
        large = font(gen, 60, chars, 3f);
        huge = font(gen, 150, "0123456789,.?+-−: %", 4f);
        gen.dispose();
    }

    private static BitmapFont font(FreeTypeFontGenerator gen, int size, String chars, float border) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = size;
        p.characters = chars;
        p.color = Color.WHITE;
        p.borderWidth = border;
        p.borderColor = new Color(0f, 0f, 0f, 0.85f);
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        return gen.generateFont(p);
    }

    public Texture texture(String path, boolean pixelArt) {
        Texture t = textures.get(path);
        if (t == null) {
            t = new Texture(Gdx.files.internal(path));
            Texture.TextureFilter filter = pixelArt ? Texture.TextureFilter.Nearest : Texture.TextureFilter.Linear;
            t.setFilter(filter, filter);
            textures.put(path, t);
        }
        return t;
    }

    @Override
    public void dispose() {
        small.dispose();
        medium.dispose();
        large.dispose();
        huge.dispose();
        for (Texture t : textures.values()) t.dispose();
    }
}
