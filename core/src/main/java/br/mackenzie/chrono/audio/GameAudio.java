package br.mackenzie.chrono.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;
import java.util.Map;

public final class GameAudio implements Disposable {
    public enum Sfx {
        TICK("sfx/tick.ogg", 0.7f),
        HIT_WEAK("sfx/hit_weak.ogg", 0.9f),
        HIT_STRONG("sfx/hit_strong.mp3", 1f),
        HIT_CRITICAL("sfx/hit_critical.mp3", 1f),
        MISS("sfx/miss.wav", 0.9f),
        MONSTER_DEFEATED_1("sfx/monster_defeated_1.ogg", 1f),
        MONSTER_DEFEATED_2("sfx/monster_defeated_2.wav", 1f),
        MONSTER_DEFEATED_3("sfx/monster_defeated_3.ogg", 1f),
        PLAYER_DEFEATED("sfx/player_defeated.ogg", 1f),
        VICTORY("sfx/victory.ogg", 0.9f),
        DEFEAT("sfx/defeat.ogg", 0.9f);

        final String path;
        final float volume;

        Sfx(String path, float volume) {
            this.path = path;
            this.volume = volume;
        }

        public static Sfx monsterDefeated(int phaseNumber) {
            switch (phaseNumber) {
                case 1: return MONSTER_DEFEATED_1;
                case 2: return MONSTER_DEFEATED_2;
                default: return MONSTER_DEFEATED_3;
            }
        }
    }

    private static final float MUSIC_VOLUME = 0.45f;

    private final Map<Sfx, Sound> sounds = new EnumMap<>(Sfx.class);
    private Music music;
    private String musicPath;
    private boolean muted;

    public void preload() {
        for (Sfx s : Sfx.values()) sound(s);
    }

    public void play(Sfx s) {
        if (muted) return;
        Sound snd = sound(s);
        if (snd != null) snd.play(s.volume);
    }

    public void playMusic(String path) {
        if (music != null && path.equals(musicPath)) {
            if (!muted && !music.isPlaying()) music.play();
            return;
        }
        stopMusic();
        musicPath = path;
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal(path));
            music.setLooping(true);
            music.setVolume(MUSIC_VOLUME);
            if (!muted) music.play();
        } catch (RuntimeException e) {
            music = null;
            System.err.println("[GameAudio] não foi possível abrir " + path + ": " + e.getMessage());
        }
    }

    public void pauseMusic() {
        if (music != null) music.pause();
    }

    public void resumeMusic() {
        if (music != null && !muted) music.play();
    }

    public void stopMusic() {
        if (music != null) {
            music.stop();
            music.dispose();
            music = null;
        }
        musicPath = null;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) pauseMusic();
    }

    private Sound sound(Sfx s) {
        if (sounds.containsKey(s)) return sounds.get(s);
        Sound snd = null;
        try {
            snd = Gdx.audio.newSound(Gdx.files.internal(s.path));
        } catch (RuntimeException e) {
            System.err.println("[GameAudio] não foi possível abrir " + s.path + ": " + e.getMessage());
        }
        sounds.put(s, snd);
        return snd;
    }

    @Override
    public void dispose() {
        stopMusic();
        for (Sound s : sounds.values()) {
            if (s != null) s.dispose();
        }
        sounds.clear();
    }
}
