package br.mackenzie.chrono.logic;


public final class RoundResult {
    public final int round;
    public final float target;
    public final double elapsed;
    public final boolean timedOut;
    public final HitGrade grade;
    public final int damageDealt;
    public final int damageTaken;
    public final boolean chanceLost;
    public final int combo;
    public final int scoreGained;

    public RoundResult(int round, float target, double elapsed, boolean timedOut, HitGrade grade,
                       int damageDealt, int damageTaken, boolean chanceLost, int combo, int scoreGained) {
        this.round = round;
        this.target = target;
        this.elapsed = elapsed;
        this.timedOut = timedOut;
        this.grade = grade;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.chanceLost = chanceLost;
        this.combo = combo;
        this.scoreGained = scoreGained;
    }

    public double signedError() {
        return timedOut ? Double.NaN : elapsed - target;
    }

    public double absError() {
        return timedOut ? Double.NaN : Math.abs(elapsed - target);
    }
}
