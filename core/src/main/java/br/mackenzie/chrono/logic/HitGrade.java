package br.mackenzie.chrono.logic;

public enum HitGrade {
    CRITICAL("CRÍTICO!", 100, 0.05),
    STRONG("GOLPE FORTE!", 60, 0.15),
    WEAK("GOLPE FRACO", 30, 0.30),
    MISS("ERROU!", 0, Double.MAX_VALUE);

    public final String label;
    public final int damage;
    public final double baseMargin;

    HitGrade(String label, int damage, double baseMargin) {
        this.label = label;
        this.damage = damage;
        this.baseMargin = baseMargin;
    }

    public static HitGrade of(double error, float scale) {
        for (HitGrade g : values()) {
            if (error <= g.baseMargin * scale + 1e-9) return g;
        }
        return MISS;
    }

    public double margin(float scale) {
        return baseMargin * scale;
    }
}
