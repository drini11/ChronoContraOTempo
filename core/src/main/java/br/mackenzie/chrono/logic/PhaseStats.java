package br.mackenzie.chrono.logic;

import java.util.List;

public final class PhaseStats {
    public final Phase phase;
    public final int attempt;
    public final boolean cleared;
    public final int rounds;
    public final int hits;
    public final int misses;
    public final int timeouts;
    public final double meanAbsError;
    public final double stdDev;
    public final double bestError;
    public final int bestCombo;
    public final int consistencyBonus;
    public final int score;

    private PhaseStats(Phase phase, int attempt, boolean cleared, int rounds, int hits, int misses, int timeouts,
                       double meanAbsError, double stdDev, double bestError, int bestCombo,
                       int consistencyBonus, int score) {
        this.phase = phase;
        this.attempt = attempt;
        this.cleared = cleared;
        this.rounds = rounds;
        this.hits = hits;
        this.misses = misses;
        this.timeouts = timeouts;
        this.meanAbsError = meanAbsError;
        this.stdDev = stdDev;
        this.bestError = bestError;
        this.bestCombo = bestCombo;
        this.consistencyBonus = consistencyBonus;
        this.score = score;
    }

    public double hitRate() {
        return rounds == 0 ? 0 : 100.0 * hits / rounds;
    }

    public static PhaseStats compute(Phase phase, int attempt, boolean cleared, List<RoundResult> results,
                                     int bestCombo, int baseScore) {
        int hits = 0, misses = 0, timeouts = 0, pressed = 0;
        double sumAbs = 0, sumSigned = 0, best = Double.NaN;

        for (RoundResult r : results) {
            if (r.damageDealt > 0) hits++; else misses++;
            if (r.timedOut) {
                timeouts++;
                continue;
            }
            pressed++;
            sumAbs += r.absError();
            sumSigned += r.signedError();
            if (Double.isNaN(best) || r.absError() < best) best = r.absError();
        }

        double meanAbs = pressed == 0 ? Double.NaN : sumAbs / pressed;

        double std = 0;
        if (pressed >= 2) {
            double mean = sumSigned / pressed;
            double var = 0;
            for (RoundResult r : results) {
                if (!r.timedOut) var += Math.pow(r.signedError() - mean, 2);
            }
            std = Math.sqrt(var / pressed);
        }

        int bonus = cleared ? consistencyBonus(std, pressed, phase.marginScale) : 0;
        return new PhaseStats(phase, attempt, cleared, results.size(), hits, misses, timeouts,
            meanAbs, std, best, bestCombo, bonus, baseScore + bonus);
    }

    public static int consistencyBonus(double stdDev, int pressed, float scale) {
        if (pressed < 3) return 0;
        if (stdDev <= 0.05 * scale) return 300;
        if (stdDev <= 0.10 * scale) return 200;
        if (stdDev <= 0.20 * scale) return 100;
        return 0;
    }
}
