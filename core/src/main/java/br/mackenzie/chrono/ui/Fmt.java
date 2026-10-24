package br.mackenzie.chrono.ui;
import java.util.Locale;

public final class Fmt {
    private Fmt() {}

    public static String sec2(double s) {
        return String.format(Locale.US, "%.2f", s).replace('.', ',');
    }

    public static String sec3(double s) {
        return String.format(Locale.US, "%.3f", s).replace('.', ',');
    }

    public static String sec(double s) {
        return Double.isNaN(s) ? "—" : sec2(s) + " s";
    }

    public static String signed(double s) {
        if (Double.isNaN(s)) return "—";
        return (s >= 0 ? "+" : "−") + sec2(Math.abs(s)) + " s";
    }

    public static String target(float t) {
        return String.format(Locale.US, "%.1f", t).replace('.', ',');
    }

    public static String pct(double p) {
        return String.format(Locale.US, "%.0f", p) + "%";
    }
}
