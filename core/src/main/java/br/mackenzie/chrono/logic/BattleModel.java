package br.mackenzie.chrono.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BattleModel {
    public static final float TARGET_STEP = 0.5f;
    public static final int COMBO_BONUS_STEP = 10;
    public static final int COMBO_BONUS_CAP = 50;

    private final Phase phase;
    private final Random rng;
    private final PlayerHealth player = new PlayerHealth();
    private final List<RoundResult> results = new ArrayList<>();
    private int monsterHp;
    private int combo;
    private int bestCombo;
    private int score;
    private float lastTarget = -1f;

    public BattleModel(Phase phase) {
        this(phase, System.nanoTime());
    }

    public BattleModel(Phase phase, long seed) {
        this.phase = phase;
        this.rng = new Random(seed);
        this.monsterHp = phase.monsterHp;
    }

    public float nextTarget() {
        int steps = Math.round((phase.maxTarget - phase.minTarget) / TARGET_STEP);
        float t;
        do {
            t = phase.minTarget + TARGET_STEP * rng.nextInt(steps + 1);
        } while (t == lastTarget);
        lastTarget = t;
        return t;
    }

    public RoundResult submit(float target, double elapsed) {
        boolean timedOut = Double.isNaN(elapsed);
        HitGrade grade = timedOut ? HitGrade.MISS : HitGrade.of(Math.abs(elapsed - target), phase.marginScale);

        int dealt = grade.damage;
        int taken = 0;
        boolean chanceLost = false;
        int gained = 0;

        if (dealt > 0) {
            monsterHp = Math.max(0, monsterHp - dealt);
            combo++;
            bestCombo = Math.max(bestCombo, combo);
            int comboBonus = Math.min((combo - 1) * COMBO_BONUS_STEP, COMBO_BONUS_CAP);
            gained = dealt + comboBonus;
            score += gained;
        } else {
            combo = 0;
            taken = phase.errorDamage;
            chanceLost = player.damage(taken);
        }

        RoundResult r = new RoundResult(results.size() + 1, target, elapsed, timedOut, grade,
            dealt, taken, chanceLost, combo, gained);
        results.add(r);
        return r;
    }

    public PhaseStats finish(int attempt, boolean cleared) {
        return PhaseStats.compute(phase, attempt, cleared, results, bestCombo, score);
    }

    public Phase phase() { return phase; }
    public PlayerHealth player() { return player; }
    public int monsterHp() { return monsterHp; }
    public int combo() { return combo; }
    public int bestCombo() { return bestCombo; }
    public int score() { return score; }
    public int roundsPlayed() { return results.size(); }
    public boolean isMonsterDefeated() { return monsterHp <= 0; }
    public boolean isPlayerDefeated() { return player.isDead(); }
    public List<RoundResult> results() { return results; }
}
