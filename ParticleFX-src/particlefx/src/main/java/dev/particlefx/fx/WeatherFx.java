package dev.particlefx.fx;

import dev.particlefx.core.BaseFx;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.Emitter;
import dev.particlefx.core.Geo;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/** Effects 11-18 + 34 + 44: weather, water and stone. */
public final class WeatherFx {
    private WeatherFx() {}
    private static double RND01() { return java.util.concurrent.ThreadLocalRandom.current().nextDouble(); }

    // 11. Lightning: jagged vertical branches + flash strike
    public static class Lightning extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Lightning(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            int bolts = f < 0.5 ? 2 : 3;
            for (int b = 0; b < bolts; b++) {
                double wob = Math.sin(f * 20 + b) * 0.6 * s;
                for (double y = 6 * s; y > 0; y -= 0.6) {
                    double x = (y > 3.2 * s && y < 4.2 * s) ? wob : Math.sin(y * 2 + b) * 0.3 * s;
                    Location l = o.clone().add(x, y, Math.cos(y * 2 + b) * 0.3 * s);
                    out.point(Particle.ELECTRIC_SPARK, l, 1, 0.05, 0, 0.05, 0);
                    out.dust(l, 0.9f, LIGHTBLUE, 1, 0.1, 0.1, 0.1, 0.02);
                }
            }
            if (f > 0.85) { out.flash(o); out.boom(o, false); }
            return true;
        }
    }

    // 12. Storm front: sequential bolts then a simultaneous volley
    public static class StormFront extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public StormFront(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            int active = f < 0.75 ? (int) (f * 4) + 1 : 5; // sequential, then all at once
            for (int b = 0; b < active; b++) {
                double a = b * 1.9;
                Location base = o.clone().add(Math.cos(a) * 2 * s, 0, Math.sin(a) * 2 * s);
                for (double y = 5.5 * s; y > 0; y -= 0.55) {
                    Location l = base.clone().add(Math.sin(y * 3 + f * 8) * 0.35 * s, y, Math.cos(y * 3) * 0.35 * s);
                    out.point(Particle.ELECTRIC_SPARK, l, 1, 0, 0, 0, 0);
                }
                if (f > 0.75) out.flash(base);
            }
            if (f > 0.75 && f < 0.8) out.boom(o, true);
            out.ring(Particle.SMOKE, o, 2 * s, 24, false, d);
            return true;
        }
    }

    // 13. Fire tornado: fire spiral with a sudden 2.6x expansion
    public static class FireTornado extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public FireTornado(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double widen = f < 0.75 ? 1.0 : 2.6; // sudden expansion
            for (int i = 0; i < out.count(34, d); i++) {
                double h = RND01() * 5 * s;
                double r = (0.4 + h * 0.22) * widen * s;
                double a = h * 2.2 + f * 12;
                Particle p = RND01() < 0.6 ? Particle.FLAME : Particle.SMALL_FLAME;
                out.point(p, o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 1, 0.02, 0.08, 0.02, 0.01);
                if (RND01() < 0.3) out.dust(o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 0.8f, ORANGE, 1, 0.05, 0.05, 0.05, 0);
            }
            if (f > 0.75 && t % 10 == 0) out.boom(o.clone().add(0, 2.5 * s, 0), false);
            if (f > 0.9) out.ring(Particle.FLAME, o, 3 * s, 32, false, d);
            return true;
        }
    }

    // 14. Ice tornado: snow spiral + sudden shard scatter
    public static class IceTornado extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public IceTornado(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            for (int i = 0; i < out.count(30, d); i++) {
                double h = RND01() * 4.5 * s;
                double r = (0.4 + h * 0.2) * s, a = h * 2 + f * 10;
                out.point(Particle.SNOWFLAKE, o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 1, 0.02, 0.05, 0.02, 0.01);
                out.dust(o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 0.8f, LIGHTBLUE, 1, 0, 0.05, 0, 0);
            }
            if (f > 0.8) { // shard scatter
                for (int i = 0; i < 10; i++) {
                    double a = RND01() * Math.PI * 2, r = RND01() * 4 * s;
                    out.point(Particle.SNOWFLAKE, o.clone().add(r * Math.cos(a), RND01() * 4 * s, r * Math.sin(a)), 2, 0.3, 0.3, 0.3, 0.15);
                }
                if (t % 6 == 0) out.ring(Particle.GLOW, o, 3 * s, 28, false, d);
            }
            return true;
        }
    }

    // 15. Waterfall: falling arc becomes outward waves
    public static class Waterfall extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Waterfall(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            for (int i = 0; i < out.count(26, d); i++) {
                double p = Geo.frac(f * 2 + i * 0.13);
                double x = Math.cos(p * 2.4) * 2.4 * s, y = 4 * s - p * 4 * s, z = Math.sin(p * 2.4) * 2.4 * s;
                out.point(RND01() < 0.5 ? Particle.NAUTILUS : Particle.SPLASH, o.clone().add(x, y, z), 1, 0, -0.02, 0, 0);
            }
            if (f > 0.55) {
                double r = Geo.easeOut((f - 0.55) / 0.45) * 4.5 * s;
                out.ring(Particle.DOLPHIN, o, r, 36, false, d);
                out.dustRing(o, r * 0.8, 1.0f, BLUE, 30, false, d);
            }
            return true;
        }
    }

    // 16. Tsunami: growing wave wall that suddenly rolls outward
    public static class Tsunami extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Tsunami(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            if (f < 0.6) { // growing wall
                double h = Geo.easeOut(f / 0.6) * 5 * s;
                for (int i = 0; i < out.count(40, d); i++) {
                    double a = RND01() * Math.PI * 2, r = 0.6 * s;
                    out.point(Particle.NAUTILUS, o.clone().add(r * Math.cos(a), RND01() * h, r * Math.sin(a)), 1, 0, 0.05, 0, 0.01);
                    out.dust(o.clone().add(r * Math.cos(a), RND01() * h, r * Math.sin(a)), 1.2f, BLUE, 1, 0, 0, 0, 0);
                }
            } else { // sudden roll outward
                double r = Geo.easeOut((f - 0.6) / 0.4) * 7 * s;
                for (int ring = 0; ring < 2; ring++) {
                    double rr = r - ring * 1.2 * s;
                    if (rr < 0) continue;
                    int n = out.count(44 - ring * 8, d);
                    for (int i = 0; i < n; i++) {
                        double a = 2 * Math.PI * i / n;
                        out.dust(o.clone().add(rr * Math.cos(a), 0.3 + ring * 0.8, rr * Math.sin(a)), 1.3f, BLUE, 1, 0, 0.1, 0, 0);
                        out.point(Particle.SPLASH, o.clone().add(rr * Math.cos(a), 0.8, rr * Math.sin(a)), 1, 0, 0.1, 0, 0);
                    }
                }
            }
            return true;
        }
    }

    // 17. Volcano: rising column then massive eruption
    public static class Volcano extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Volcano(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            if (f < 0.65) { // column rises
                for (int i = 0; i < out.count(24, d); i++) {
                    double h = RND01() * 4.5 * s;
                    double r = (0.5 - h * 0.06) * s, a = RND01() * Math.PI * 2;
                    out.point(Particle.SMALL_FLAME, o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 1, 0.05, 0.12, 0.05, 0.02);
                    out.dust(o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 1.1f, DARK, 1, 0.05, 0.05, 0.05, 0);
                }
            } else { // eruption
                if (f < 0.72) { out.boom(o.clone().add(0, 4 * s, 0), true); out.flash(o.clone().add(0, 4 * s, 0)); }
                for (int i = 0; i < out.count(40, d); i++) {
                    double a = RND01() * Math.PI * 2, e = 0.3 + RND01() * 0.9;
                    double r = 3 * s, h = 1 + RND01() * 4 * s;
                    out.point(Particle.LAVA, o.clone().add(r * Math.cos(a) * (0.4 + RND01()), h, r * Math.sin(a) * (0.4 + RND01())), 1, 0.2, 0.2, 0.2, 0.1);
                    out.point(Particle.FLAME, o.clone().add(e * r * Math.cos(a), h, e * r * Math.sin(a)), 1, 0.1, 0.05, 0.1, 0.05);
                }
                out.ring(Particle.LARGE_SMOKE, o, 3.5 * s, 30, false, d);
            }
            return true;
        }
    }

    // 18. Earthquake: concentric rings + outward cracks, double strike
    public static class Earthquake extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Earthquake(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            for (int ring = 0; ring < 3; ring++) {
                double r = ((t * 0.12 + ring * 1.4) % 4.5) * s;
                out.dustRing(o, r, 1.0f, new Color[]{DARK, RED, ORANGE}[ring], 30, false, d);
            }
            for (int c = 0; c < 8; c++) {
                double a = c * Math.PI / 4;
                double len = 1 + f * 4 * s;
                out.line(Particle.ASH, o, o.clone().add(len * Math.cos(a), 0, len * Math.sin(a)), 0.5, d);
                out.line(Particle.CRIT, o.clone().add(0, 0.1, 0), o.clone().add(len * Math.cos(a), 0.1, len * Math.sin(a)), 0.5, d);
            }
            if (f > 0.45 && f < 0.55) { out.boom(o, false); }
            if (f > 0.8) out.flash(o);
            return true;
        }
    }

    // 34. Sandstorm: sand pillar with sudden base expansion
    public static class Sandstorm extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        private final Color SAND = Color.fromRGB(220, 190, 120);
        public Sandstorm(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double baseW = f < 0.8 ? 1.2 : 3.0; // sudden base expansion
            for (int i = 0; i < out.count(34, d); i++) {
                double h = RND01() * 4.5 * s;
                double w = h < 1.2 * s ? baseW : 1.2;
                double r = h * 0.18 * w * s + 0.3, a = h * 1.8 + f * 14;
                out.dust(o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 1.0f, SAND, 1, 0.1, 0.05, 0.1, 0.02);
                if (RND01() < 0.2) out.point(Particle.ASH, o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 1, 0.1, 0.05, 0.1, 0.01);
            }
            if (f > 0.8 && t % 5 == 0) out.ring(Particle.DUST_PLUME, o, 2.5 * s, 30, false, d);
            return true;
        }
    }

    // 44. Vortex: rotating layers suddenly suck into the center
    public static class Vortex extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Vortex(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double shrink = f < 0.8 ? 1.0 : Math.max(0.12, 1 - (f - 0.8) * 4); // sudden suction
            for (int layer = 0; layer < 4; layer++) {
                double r = (2.4 - layer * 0.5) * s * shrink, h = layer * 0.9 * s;
                int n = out.count(26 - layer * 3, d);
                for (int i = 0; i < n; i++) {
                    double a = 2 * Math.PI * i / n + f * (7 - layer) * 2;
                    out.point(layer % 2 == 0 ? Particle.CLOUD : Particle.SMOKE,
                            o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 1, 0, 0.02, 0, 0);
                }
            }
            if (f > 0.9) out.point(Particle.POOF, o.clone().add(0, 1.5 * s, 0), out.count(6, d), 0.4, 0.4, 0.4, 0.1);
            return true;
        }
    }
}
