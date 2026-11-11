package dev.particlefx.core;

/** Validated per-run settings for one effect. */
public final class EffectSettings {
    public final double scale;
    public final int duration;
    public final double speed;
    public final double density;
    public final boolean ahead; // true = run in front of the player, false = at player position

    public EffectSettings(double scale, int duration, double speed, double density, boolean ahead) {
        this.scale = scale;
        this.duration = duration;
        this.speed = speed;
        this.density = density;
        this.ahead = ahead;
    }

    static double fin(double v, double fallback) {
        return (Double.isNaN(v) || Double.isInfinite(v) || v <= 0) ? fallback : v;
    }

    public static EffectSettings safe(double scale, int duration, double speed, double density, boolean ahead) {
        return new EffectSettings(fin(scale, 1.0), (int) Math.max(20, fin(duration, 160)),
                fin(speed, 1.0), fin(density, 1.0), ahead);
    }
}
