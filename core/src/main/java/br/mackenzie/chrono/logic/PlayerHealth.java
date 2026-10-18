package br.mackenzie.chrono.logic;

public final class PlayerHealth {
    public static final int CHANCES = 3;
    public static final int HP_PER_CHANCE = 10;

    private int chances = CHANCES;
    private int hp = HP_PER_CHANCE;

    public boolean damage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            chances--;
            hp = chances > 0 ? HP_PER_CHANCE : 0;
            return true;
        }
        return false;
    }

    public int chances() { return chances; }
    public int hp() { return hp; }
    public boolean isDead() { return chances <= 0; }
}
