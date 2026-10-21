package br.mackenzie.chrono.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.EnumMap;
import java.util.Map;

/**
 * Um lutador na tela (Chrono ou um inimigo): toca as animações dos sprites e aplica efeitos
 * de dano (piscada vermelha, tremida, "falha temporal") e de morte.
 */
public final class Fighter {

    /** Animações possíveis. A ordem precisa bater com a lista de arquivos do FighterType. */
    public enum Anim {
        IDLE(0.10f, true),
        ATTACK_1(0.07f, false),
        ATTACK_2(0.07f, false),
        ATTACK_3(0.07f, false),
        HIT(0.10f, false),
        DEATH(0.12f, false);

        final float frameTime;
        final boolean loop;

        Anim(float frameTime, boolean loop) {
            this.frameTime = frameTime;
            this.loop = loop;
        }
    }

    private static final float FLASH_TIME = 0.35f;
    private static final float SHAKE_TIME = 0.35f;
    private static final float GLITCH_TIME = 0.5f;
    private static final float DISSOLVE_TIME = 1.2f;

    private final FighterType type;
    private final Map<Anim, Animation<TextureRegion>> anims = new EnumMap<>(Anim.class);
    private final float x;
    private final float groundY;
    private final boolean facesRight;

    private Anim current = Anim.IDLE;
    private float animTime;
    private boolean dead;
    private float alpha = 1f;
    private float dissolveSpeed;      // > 0 enquanto o personagem está sumindo
    private float flashLeft;
    private float shakeLeft;
    private float glitchLeft;


    public Fighter(Assets assets, FighterType type, float x, float groundY, boolean facesRight) {
        this.type = type;
        this.x = x;
        this.groundY = groundY;
        this.facesRight = facesRight;

        Anim[] all = Anim.values();
        for (int i = 0; i < all.length; i++) {
            String file = type.files[i];
            if (file == null) continue;

            Texture sheet = assets.texture("sprites/" + type.folder + "/" + file + ".png", true);
            TextureRegion[] frames = TextureRegion.split(sheet, type.frameW, type.frameH)[0];
            Animation<TextureRegion> animation = new Animation<>(all[i].frameTime, frames);
            animation.setPlayMode(all[i].loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
            anims.put(all[i], animation);
        }
    }

    public void playAttack(int level) {
        if (dead) return;
        play(level <= 1 ? Anim.ATTACK_1 : level == 2 ? Anim.ATTACK_2 : Anim.ATTACK_3);
    }

    public void playHit() {
        if (dead) return;
        flashLeft = FLASH_TIME;
        shakeLeft = SHAKE_TIME;
        play(Anim.HIT);
    }

    public void playGlitch() {
        if (dead) return;
        glitchLeft = GLITCH_TIME;
        play(Anim.HIT);
    }

    public void playDeath() {
        if (dead) return;
        dead = true;
        play(Anim.DEATH);
        if (!anims.containsKey(Anim.DEATH)) dissolveSpeed = 1f / DISSOLVE_TIME;
    }

    public void fadeOut(float seconds) {
        dissolveSpeed = 1f / seconds;
    }

    public void reset() {
        current = Anim.IDLE;
        animTime = 0f;
        dead = false;
        alpha = 1f;
        dissolveSpeed = 0f;
        flashLeft = 0f;
        shakeLeft = 0f;
        glitchLeft = 0f;
    }

    public boolean isDeathFinished() {
        if (!dead) return false;
        Animation<TextureRegion> death = anims.get(Anim.DEATH);
        return death != null ? death.isAnimationFinished(animTime) : alpha <= 0f;
    }

    public boolean isDead() {
        return dead;
    }

    public void update(float delta) {
        if (!dead || anims.containsKey(Anim.DEATH)) animTime += delta;

        if (!dead && current != Anim.IDLE && anims.get(current).isAnimationFinished(animTime)) {
            current = Anim.IDLE;
            animTime = 0f;
        }

        flashLeft = Math.max(0f, flashLeft - delta);
        shakeLeft = Math.max(0f, shakeLeft - delta);
        glitchLeft = Math.max(0f, glitchLeft - delta);
        if (dissolveSpeed > 0f) alpha = Math.max(0f, alpha - dissolveSpeed * delta);
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = anims.get(current).getKeyFrame(animTime);
        float shake = shakeLeft > 0f ? MathUtils.sin(shakeLeft * 80f) * 12f * (shakeLeft / SHAKE_TIME) : 0f;

        if (glitchLeft > 0f) {
            float strength = glitchLeft / GLITCH_TIME;
            float offset = 16f * strength * (((int) (glitchLeft * 20f)) % 2 == 0 ? 1f : -1f);
            batch.setColor(0f, 1f, 1f, 0.6f * alpha);
            drawFrame(batch, frame, shake - offset);
            batch.setColor(1f, 0f, 1f, 0.6f * alpha);
            drawFrame(batch, frame, shake + offset);
        }

        float g = 1f - 0.65f * (flashLeft / FLASH_TIME);
        batch.setColor(1f, g, g, alpha);
        drawFrame(batch, frame, shake);
        batch.setColor(Color.WHITE);
    }

    private void play(Anim wanted) {
        current = resolve(wanted);
        animTime = 0f;
    }

    private Anim resolve(Anim wanted) {
        if (anims.containsKey(wanted)) return wanted;
        if (wanted == Anim.ATTACK_3 && anims.containsKey(Anim.ATTACK_2)) return Anim.ATTACK_2;
        if ((wanted == Anim.ATTACK_3 || wanted == Anim.ATTACK_2) && anims.containsKey(Anim.ATTACK_1)) return Anim.ATTACK_1;
        return Anim.IDLE;
    }

    private void drawFrame(SpriteBatch batch, TextureRegion frame, float offsetX) {
        float w = type.frameW * type.scale;
        float h = type.frameH * type.scale;
        boolean flip = facesRight != type.naturalFacesRight;
        float pivot = flip ? type.frameW - type.pivotX : type.pivotX;
        float left = x + offsetX - pivot * type.scale;
        float bottom = groundY - type.feet * type.scale;

        if (flip) batch.draw(frame, left + w, bottom, -w, h);
        else batch.draw(frame, left, bottom, w, h);
    }
}