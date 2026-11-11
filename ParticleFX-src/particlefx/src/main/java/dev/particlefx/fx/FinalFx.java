package dev.particlefx.fx;

import dev.particlefx.core.BaseFx;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.Emitter;
import dev.particlefx.core.Geo;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/** Effects 45-50: finales. */
public final class FinalFx {
    private FinalFx() {}
    private static double RND01() { return java.util.concurrent.ThreadLocalRandom.current().nextDouble(); }

    // 45. Star explosion: star pulses and throws out rays
    public static class StarExplosion extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public StarExplosion(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double pulse = 1 + 0.2 * Math.sin(t * 0.6);
            Location c = o.clone().add(0, 2.5 * s, 0);
            // five-pointed star outline
            for (int i = 0; i < 5; i++) {
                double a1 = -Math.PI / 2 + 2 * Math.PI * i / 5;
                double a2 = -Math.PI / 2 + 2 * Math.PI * ((i + 2) % 5) / 5;
                Vector p1 = new Vector(2 * Math.cos(a1), Math.sin(a1), 0).multiply(pulse * s);
                Vector p2 = new Vector(2 * Math.cos(a2), Math.sin(a2), 0).multiply(pulse * s);
                out.line(Particle.FIREWORK, c.clone().add(Geo.rotY(p1, f * 2)), c.clone().add(Geo.rotY(p2, f * 2)), 0.3, d);
            }
            if (f > 0.7) { // ray ejection
                double e = (f - 0.7) / 0.3;
                for (int i = 0; i < 10; i++) {
                    double a = 2 * Math.PI * i / 10;
                    Location tip = c.clone().add(Geo.easeOut(e) * 5 * Math.cos(a) * s, Geo.easeOut(e) * 5 * Math.sin(a) * s, 0);
                    out.line(Particle.FIREWORK, c, tip, 0.5, d);
                }
                if (e < 0.2) { out.flash(c); out.boom(c, true); }
            }
            return true;
        }
    }

    // 46. Double spiral: counter spirals meet, explode at the middle
    public static class DoubleSpiral extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public DoubleSpiral(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            Location mid = o.clone().add(0, 2.2 * s, 0);
            for (int dir = -1; dir <= 1; dir += 2) {
                for (int i = 0; i < 22; i++) {
                    double p = i / 21.0;
                    double r = p * 2.2 * s;
                    double a = p * 9 * dir + f * 6 * dir;
                    double yOff = dir * p * 1.6 * s * (f < 0.7 ? 1 : 1 - (f - 0.7) / 0.3); // converge at the end
                    out.dust(mid.clone().add(r * Math.cos(a), yOff, r * Math.sin(a)),
                            0.9f, dir < 0 ? CYAN : MAGENTA, 1, 0, 0, 0, 0);
                }
            }
            if (f > 0.7) {
                double e = (f - 0.7) / 0.3;
                if (e < 0.15) { out.boom(mid, true); out.flash(mid); }
                out.sphere(Particle.GLOW, mid, Geo.easeOut(e) * 3 * s, 34, d);
                out.ring(Particle.ELECTRIC_SPARK, mid, Geo.easeOut(e) * 4 * s, 36, false, d);
            }
            return true;
        }
    }

    // 47. Light arch: a wide arch grows, then breaks into waves
    public static class LightArch extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public LightArch(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double span = Geo.easeOut(Math.min(1, f * 1.5)) * 5 * s;
            int n = out.count(50, d);
            for (int i = 0; i < n; i++) {
                double p = i / (double) n;
                double x = (p - 0.5) * 2 * span;
                double y = Math.cos(p * Math.PI) * 3.5 * s * Math.min(1, f * 1.5);
                out.point(Particle.END_ROD, o.clone().add(x, y, 0), 1, 0, 0, 0, 0);
                if (i % 3 == 0) out.dust(o.clone().add(x, y, 0), 1.0f, GOLD, 1, 0, 0, 0, 0);
            }
            if (f > 0.75) { // break into waves
                double e = (f - 0.75) / 0.25;
                for (int w = 0; w < 3; w++) {
                    double r = Geo.easeOut(e) * (1.5 + w) * s;
                    out.ring(Particle.GLOW, o.clone().add(0, 2 * s, 0), r, 36, false, d);
                }
            }
            return true;
        }
    }

    // 48. Energy pillar: rings rise, breathing between compression and expansion
    public static class EnergyPillar extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public EnergyPillar(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            for (int ring = 0; ring < 5; ring++) {
                double p = Geo.frac(f * 2 + ring * 0.2);
                double y = p * 5 * s;
                double breathe = 1 + 0.3 * Math.sin(t * 0.5 + ring);
                double r = (0.8 + p * 1.4) * s * breathe;
                Color c = ring % 2 == 0 ? PURPLE : CYAN;
                out.dustRing(o.clone().add(0, y, 0), r, 1.0f, c, 28, false, d);
                if (t % 4 == 0) out.ring(Particle.GLOW, o.clone().add(0, y, 0), r * 0.9, 20, false, d);
            }
            out.line(Particle.END_ROD, o.clone().add(0, 0.2, 0), o.clone().add(0, 5 * s, 0), 0.6, d);
            if (f > 0.95) out.flash(o.clone().add(0, 5 * s, 0));
            return true;
        }
    }

    // 49. Shockwave: rings expand in horizontal and vertical planes at once
    public static class Shockwave extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Shockwave(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double wave = ((t % 30) / 30.0);
            double r = Geo.easeOut(wave) * 6 * s;
            out.dustRing(o, r, 1.1f, WHITE, 46, false, d);           // horizontal
            out.dustRing(o, r, 1.1f, LIGHTBLUE, 30, true, d);        // vertical XZ->XY
            Location c = o.clone().add(0, 2.5 * s, 0);
            out.ring(Particle.GLOW, c, r * 0.7, 30, true, d);
            if (t % 30 == 0) { out.boom(o, false); out.flash(c); }
            return true;
        }
    }

    // 50. Final apocalypse: vortex -> flash -> rings -> vertical rays -> collapse
    public static class Apocalypse extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Apocalypse(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            if (f < 0.25) { // vortex
                for (int arm = 0; arm < 4; arm++) {
                    for (int i = 0; i < 16; i++) {
                        double p = i / 15.0, r = (0.3 + p * 2.6) * s, a = arm * Math.PI / 2 + p * 5 + f * 16;
                        out.dust(o.clone().add(r * Math.cos(a), p * 3.5 * s, r * Math.sin(a)),
                                1.0f, i % 2 == 0 ? DARK : RED, 1, 0, 0.02, 0, 0);
                    }
                }
            } else if (f < 0.35) { // flash
                if (t % 2 == 0) out.flash(o.clone().add(0, 1.8 * s, 0));
                out.boom(o.clone().add(0, 1.8 * s, 0), true);
                out.sphere(Particle.FLAME, o.clone().add(0, 1.8 * s, 0), 2.5 * s, 40, d);
            } else if (f < 0.6) { // giant rings
                double e = (f - 0.35) / 0.25;
                for (int k = 0; k < 3; k++) {
                    double r = Geo.easeOut(Math.min(1, e + k * 0.15)) * 7 * s;
                    out.dustRing(o, r, 1.4f, new Color[]{RED, ORANGE, YELLOW}[k], 44, false, d);
                }
            } else if (f < 0.85) { // vertical rays
                double e = (f - 0.6) / 0.25;
                for (int i = 0; i < 10; i++) {
                    double a = 2 * Math.PI * i / 10;
                    double h = Geo.easeOut(e) * 7 * s;
                    out.line(Particle.GLOW, o.clone().add(2 * Math.cos(a) * s, 0.2, 2 * Math.sin(a) * s),
                            o.clone().add(2 * Math.cos(a) * s, h, 2 * Math.sin(a) * s), 0.6, d);
                    out.point(Particle.FIREWORK, o.clone().add(2 * Math.cos(a) * s, h, 2 * Math.sin(a) * s), 2, 0.1, 0.1, 0.1, 0);
                }
            } else { // final collapse
                double e = (f - 0.85) / 0.15;
                double r = Math.max(0.15, (1 - e) * 5) * s;
                out.dustSphere(o.clone().add(0, 1.5 * s, 0), r, 1.6f, DARK, 40, d);
                out.point(Particle.POOF, o.clone().add(0, 1.5 * s, 0), out.count(10, d), 0.4, 0.4, 0.4, 0.1);
                if (f > 0.99) out.flash(o.clone().add(0, 1.5 * s, 0));
            }
            return true;
        }
    }
}
