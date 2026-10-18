package br.mackenzie.chrono.logic;

public final class Phase {
    public final int number;
    public final String name;
    public final String monsterName;
    public final int monsterHp;
    public final float minTarget;
    public final float maxTarget;
    public final float marginScale;
    public final TimerMode timerMode;
    public final int errorDamage;

    public Phase(int number, String name, String monsterName, int monsterHp,
                 float minTarget, float maxTarget, float marginScale,
                 TimerMode timerMode, int errorDamage) {
        this.number = number;
        this.name = name;
        this.monsterName = monsterName;
        this.monsterHp = monsterHp;
        this.minTarget = minTarget;
        this.maxTarget = maxTarget;
        this.marginScale = marginScale;
        this.timerMode = timerMode;
        this.errorDamage = errorDamage;
    }

    public static final Phase[] ALL = {
        new Phase(1, "Fase 1", "Minotauro Ancião",  200,  2f,   4f,   1.5f,   TimerMode.VISIBLE,    3),
        new Phase(2, "Fase 2", "Golem Steampunk",   300,  4f,   8f,   1.0f,   TimerMode.FADE_OUT,   4),
        new Phase(3, "Fase 3", "Onorhc",            500,  6f,   12f,  0.8f,   TimerMode.HIDDEN,     5),
    };

    public static int count() {
        return ALL.length;
    }
}
