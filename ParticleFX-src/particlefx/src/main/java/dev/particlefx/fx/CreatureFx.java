package dev.particlefx.fx;

import dev.particlefx.core.BaseFx;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.Emitter;
import dev.particlefx.core.Geo;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/** Effects 19-24: creatures and celestial bodies. */
public final class CreatureFx {
    private CreatureFx() {}
    private static double RND01() { return java.util.concurrent.ThreadLocalRandom.current().nextDouble(); }

    // 19. Stone golem: silhouette assembles bottom-up, then crumbles
    public static class StoneGolem extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        private final Color STONE = Color.fromRGB(140, 140, 140);
        public StoneGolem(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            if (f < 0.6) { // assemble bottom-up
                double reach = Geo.easeOut(f / 0.6) * 3.4 * s;
                for (int i = 0; i < out.count(40, d); i++) {
                    double h = RND01() * reach;
                    double bw = h < 1.2 * s ? 1.5 : (h < 2.4 * s ? 1.0 : 0.8); // legs, torso, head
                    out.dust(o.clone().add(RND01() * bw * s - bw * s / 2, h, RND01() * bw * s - bw * s / 2),
                            1.2f, STONE, 1, 0, 0.05, 0, 0);
                }
            } else if (f < 0.8) { // hold: glowing eyes pulse
                out.dust(o.clone().add(0.15 * s, 3.1 * s, 0.2 * s), 1.0f, RED, 1, 0, 0, 0, 0);
                out.dust(o.clone().add(-0.15 * s, 3.1 * s, 0.2 * s), 1.0f, RED, 1, 0, 0, 0, 0);
                if (t % 8 == 0) out.ring(Particle.ASH, o, 1.6 * s, 20, false, d);
            } else { // crumble
                for (int i = 0; i < out.count(50, d); i++) {
                    double a = RND01() * Math.PI * 2, r = RND01() * 3 * s;
                    out.point(Particle.POOF, o.clone().add(r * Math.cos(a), RND01() * 3.4 * s, r * Math.sin(a)), 1, 0.1, 0.05, 0.1, 0.02);
                    out.dust(o.clone().add(r * Math.cos(a), RND01() * 3 * s, r * Math.sin(a)), 1.0f, STONE, 1, 0.1, -0.05, 0.1, 0);
                }
            }
            return true;
        }
    }

    // 20. Dragon: silhouette flies an arc, fire wave on pass
    public static class Dragon extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Dragon(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double a = f * Math.PI * 1.6;
            Location head = o.clone().add(Math.cos(a) * 4 * s, 2.5 * s + Math.sin(a) * 1.5 * s, Math.sin(a) * 4 * s);
            // body: segments trailing behind the head along the arc
            for (int seg = 0; seg < 10; seg++) {
                double aa = a - seg * 0.09;
                Location bp = o.clone().add(Math.cos(aa) * 4 * s, 2.5 * s + Math.sin(aa) * 1.5 * s, Math.sin(aa) * 4 * s);
                double w = seg < 2 ? 0.9 : (seg < 7 ? 0.5 : 0.2);
                out.dustSphere(bp, w * s, 1.1f, DARK, 5, d);
            }
            // wings: two beating planes
            double beat = Math.sin(f * 14) * 0.7;
            for (int side = -1; side <= 1; side += 2) {
                for (int i = 0; i < 8; i++) {
                    double spread = i * 0.28;
                    Vector v = new Vector(side * spread * Math.cos(beat) * s, spread * Math.sin(beat) * s, 0);
                    out.dust(head.clone().add(v).add(Geo.rotY(v.clone().setY(0), 0)), 0.8f, PURPLE, 1, 0, 0, 0, 0);
                }
            }
            if (f > 0.85) { // fire wave
                out.boom(head, true);
                out.ring(Particle.FLAME, head, 2.5 * s, 36, false, d);
                out.sphere(Particle.SMALL_FLAME, head, 2 * s, 30, d);
            }
            return true;
        }
    }

    // 21. Phoenix: wings unfold, rebirth from a flash pillar
    public static class Phoenix extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Phoenix(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            Location c = o.clone().add(0, 2.2 * s, 0);
            if (f < 0.7) { // wings unfold with flapping
                double spread = Geo.easeOut(Math.min(1, f / 0.5)) * 3 * s;
                double beat = Math.sin(f * 12) * 0.5;
                for (int side = -1; side <= 1; side += 2) {
                    for (int i = 0; i < 14; i++) {
                        double p = i / 13.0;
                        Vector v = new Vector(side * spread * p, p * 1.4 * Math.sin(beat + p * 2) * s, p * 0.8 * s);
                        out.dust(c.clone().add(v), 1.0f, p < 0.5 ? YELLOW : ORANGE, 1, 0, 0, 0, 0);
                        out.point(Particle.FLAME, c.clone().add(v), 1, 0.03, 0.03, 0.03, 0.01);
                    }
                }
                out.point(Particle.END_ROD, c, 2, 0.05, 0.05, 0.05, 0);
            } else { // rebirth flash + rising pillar
                if (f < 0.75) { out.flash(c); out.boom(c, true); }
                for (int i = 0; i < out.count(30, d); i++) {
                    double h = RND01() * 5 * s, a = h * 2 + f * 8, r = 0.3;
                    out.point(Particle.TOTEM_OF_UNDYING, o.clone().add(r * Math.cos(a), h, r * Math.sin(a)), 1, 0.05, 0.1, 0.05, 0.02);
                }
                out.dustSphere(c, 1 * s, 1.6f, GOLD, 26, d);
            }
            return true;
        }
    }

    // 22. Fire meteor: arc fall, expanding explosion
    public static class FireMeteor extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public FireMeteor(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            if (f < 0.75) {
                double p = f / 0.75;
                Location m = o.clone().add((1 - p) * 5 * s, 6 * s - p * 6 * s, (1 - p) * 3 * s);
                out.point(Particle.FLAME, m, 3, 0.1, 0.1, 0.1, 0.02);
                out.dust(m, 1.4f, ORANGE, out.count(4, d), 0.2, 0.2, 0.2, 0.02);
                for (int k = 1; k <= 5; k++) {
                    Location tl = m.clone().add(k * 0.4 * s, k * 0.5 * s, k * 0.25 * s);
                    out.dust(tl, 0.8f, RED, 1, 0, 0, 0, 0);
                }
            } else { // expanding explosion
                double e = (f - 0.75) / 0.25;
                Location g = o.clone().add(0, 0.3, 0);
                if (e < 0.12) { out.boom(g, true); out.flash(g); }
                out.sphere(Particle.FLAME, g, Geo.easeOut(e) * 4 * s, 36, d);
                out.ring(Particle.LARGE_SMOKE, g, Geo.easeOut(e) * 5 * s, 34, false, d);
            }
            return true;
        }
    }

    // 23. Ice crown: spikes grow around, then burst outward
    public static class IceCrown extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public IceCrown(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double h = f < 0.75 ? Geo.easeOut(f / 0.75) * 2.2 * s : 2.2 * s;
            for (int i = 0; i < 12; i++) {
                double a = 2 * Math.PI * i / 12;
                Location base = o.clone().add(1.6 * Math.cos(a) * s, 0.2, 1.6 * Math.sin(a) * s);
                if (f < 0.75) {
                    out.line(Particle.SNOWFLAKE, base, base.clone().add(0, h, 0), 0.3, d);
                    out.dust(base.clone().add(0, h, 0), 1.0f, LIGHTBLUE, 2, 0, 0.05, 0, 0);
                } else { // sudden outward burst of tips
                    double e = (f - 0.75) / 0.25;
                    Location tip = base.clone().add(0, h, 0).add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(e * 3 * s));
                    out.line(Particle.SNOWFLAKE, base, tip, 0.3, d);
                    out.dust(tip, 1.1f, WHITE, 2, 0, 0, 0, 0);
                }
            }
            if (f < 0.75 && t % 10 == 0) out.flash(o.clone().add(0, 2.4 * s, 0));
            if (f > 0.95) out.ring(Particle.GLOW, o, 2.5 * s, 30, false, d);
            return true;
        }
    }

    // 24. Solar eclipse: rings rotate around a dark core, sudden corona flash
    public static class SolarEclipse extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public SolarEclipse(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            Location c = o.clone().add(0, 2.5 * s, 0);
            out.dustSphere(c, 1.1 * s, 1.8f, DARK, 30, d); // dark disc
            int corona = f < 0.8 ? 2 : 5;
            for (int ring = 0; ring < corona; ring++) {
                double r = (1.5 + ring * 0.4) * s, a = f * (5 + ring) * 1.5;
                int n = out.count(28, d);
                for (int i = 0; i < n; i++) {
                    double aa = 2 * Math.PI * i / n + a;
                    out.dust(c.clone().add(r * Math.cos(aa), r * Math.sin(aa) * 0.35, r * Math.sin(aa) * 0.9),
                            0.9f, ring % 2 == 0 ? GOLD : YELLOW, 1, 0, 0, 0, 0);
                }
            }
            if (f > 0.8) { // sudden corona flash
                out.flash(c);
                out.sphere(Particle.GLOW, c, 2.5 * s, 40, d);
                out.ring(Particle.FIREWORK, c, 3.2 * s, 40, false, d);
            }
            return true;
        }
    }
}
