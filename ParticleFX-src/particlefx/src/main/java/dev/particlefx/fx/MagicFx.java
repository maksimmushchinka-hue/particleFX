package dev.particlefx.fx;

import dev.particlefx.core.BaseFx;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.Emitter;
import dev.particlefx.core.Geo;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/** Effects 25-33: magic. */
public final class MagicFx {
    private MagicFx() {}
    private static double RND01() { return java.util.concurrent.ThreadLocalRandom.current().nextDouble(); }

    // 25. Moon portal: rotating square frames unfold into a big portal
    public static class MoonPortal extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public MoonPortal(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double open = Geo.easeOut(Math.min(1, f * 2)); // frames grow
            Location c = o.clone().add(0, 2.5 * s, 0);
            for (int fr = 0; fr < 3; fr++) {
                double half = (0.6 + fr * 0.7) * s * open;
                double a = f * (2 + fr) * 2 * (fr % 2 == 0 ? 1 : -1);
                for (int i = 0; i < 4; i++) {
                    Location p1 = c.clone().add(Math.cos(a) * half, Math.sin(a) * half, fr * 0.25 * s);
                    Location p2 = c.clone().add(Math.cos(a + Math.PI / 2) * half, Math.sin(a + Math.PI / 2) * half, fr * 0.25 * s);
                    out.line(Particle.END_ROD, p1, p2, 0.35, d);
                }
            }
            if (f > 0.5) out.sphere(Particle.PORTAL, c, 0.8 * s * open, 26, d);
            if (f > 0.8 && t % 8 == 0) out.flash(c);
            return true;
        }
    }

    // 26. Magic portal: spiral tunnel expands then collapses
    public static class MagicPortal extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public MagicPortal(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double rMul = f < 0.75 ? 1 + f : Math.max(0.1, 1 - (f - 0.75) * 3.5); // expand then collapse
            for (int arm = 0; arm < 3; arm++) {
                for (int i = 0; i < 24; i++) {
                    double p = i / 23.0;
                    double r = (0.3 + p * 1.8) * s * rMul;
                    double a = arm * 2 * Math.PI / 3 + p * 6 + f * 10;
                    out.point(Particle.PORTAL, o.clone().add(r * Math.cos(a), p * 4 * s, r * Math.sin(a)), 1, 0, 0, 0, 0.02);
                }
            }
            if (f < 0.75 && t % 12 == 0) out.dustRing(o, 2 * s * rMul, 1.0f, PURPLE, 30, false, d);
            if (f > 0.75) { out.boom(o.clone().add(0, 2 * s, 0), true); out.point(Particle.POOF, o.clone().add(0, 2 * s, 0), out.count(8, d), 0.5, 0.5, 0.5, 0.1); }
            return true;
        }
    }

    // 27. Rune: geometric rune is drawn segment by segment, then explodes
    public static class Rune extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        private static final double[][] SEGS = {
            {0,-1.4, 0,1.4}, {-0.8,-0.7, 0.8,0.7}, {0.8,0.7, 0,-0.2}, {-0.8,0.7, 0,-0.2},
            {-0.8,-0.7, -0.8,0.7}, {0.8,-0.7, 0.8,0.7}, {-1.1,0, 1.1,0}
        };
        public Rune(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            Location c = o.clone().add(0, 2.2 * s, 0);
            int segsToDraw = (int) Math.min(SEGS.length, f * 2.2 * SEGS.length);
            for (int i = 0; i < segsToDraw; i++) {
                double[] sg = SEGS[i];
                out.line(Particle.WAX_ON, c.clone().add(sg[0] * s, sg[1] * s, 0), c.clone().add(sg[2] * s, sg[3] * s, 0), 0.25, d);
            }
            if (f < 0.75) out.dust(c, 0.8f, CYAN, out.count(3, d), 1.4 * s, 1.4 * s, 0.2, 0.01);
            if (f > 0.78) { // rune explodes outward
                double e = (f - 0.78) / 0.22;
                out.boom(c, true);
                for (int i = 0; i < 10; i++) {
                    double a = RND01() * Math.PI * 2, r = e * 4 * s;
                    out.point(Particle.GLOW, c.clone().add(r * Math.cos(a), r * Math.sin(a), r * Math.cos(a) * 0.6), 1, 0.05, 0.05, 0.05, 0);
                }
            }
            return true;
        }
    }

    // 28. Magic circle: circles + triangle and square counter-rotating
    public static class MagicCircle extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public MagicCircle(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            Location c = o.clone().add(0, 0.15, 0);
            out.dustRing(c, 2.4 * s, 1.0f, PURPLE, 44, false, d);
            out.dustRing(c, 1.9 * s, 0.8f, CYAN, 36, false, d);
            double a1 = f * 6, a2 = -f * 4.5;
            for (int i = 0; i < 3; i++) { // triangle
                double a = a1 + 2 * Math.PI * i / 3;
                Location p = c.clone().add(1.4 * Math.cos(a) * s, 0, 1.4 * Math.sin(a) * s);
                Location q = c.clone().add(1.4 * Math.cos(a + 2 * Math.PI / 3) * s, 0, 1.4 * Math.sin(a + 2 * Math.PI / 3) * s);
                out.line(Particle.ENCHANT, p, q, 0.35, d);
            }
            for (int i = 0; i < 4; i++) { // square
                double a = a2 + Math.PI / 4 + 2 * Math.PI * i / 4;
                Location p = c.clone().add(2.1 * Math.cos(a) * s, 0.1, 2.1 * Math.sin(a) * s);
                Location q = c.clone().add(2.1 * Math.cos(a + Math.PI / 2) * s, 0.1, 2.1 * Math.sin(a + Math.PI / 2) * s);
                out.line(Particle.WITCH, p, q, 0.35, d);
            }
            if (t % 16 == 0) out.flash(c.clone().add(0, 0.5, 0));
            return true;
        }
    }

    // 29. Energy sphere: shells converge, then burst outward
    public static class EnergySphere extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public EnergySphere(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double r;
            if (f < 0.6) r = Math.max(0.4, (1 - f / 0.6) * 3.5 + 0.5) * s; // converge
            else if (f < 0.7) r = 0.5 * s;
            else r = Geo.easeOut((f - 0.7) / 0.3) * 5 * s; // sudden burst
            Location c = o.clone().add(0, 1.5 * s, 0);
            out.dustSphere(c, r, 1.1f, CYAN, 34, d);
            out.sphere(Particle.ELECTRIC_SPARK, c, r, 20, d);
            if (f > 0.62 && f < 0.72) out.flash(c);
            if (f > 0.7) out.ring(Particle.GLOW, c, r * 1.2, 34, false, d);
            return true;
        }
    }

    // 30. Energy beam: four spirals converge into a beam + shockwave
    public static class EnergyBeam extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public EnergyBeam(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double converge = Geo.easeOut(Math.min(1, f * 1.8)); // 0 -> 1
            for (int arm = 0; arm < 4; arm++) {
                for (int i = 0; i < 18; i++) {
                    double p = i / 17.0;
                    double rr = (1.8 * (1 - converge)) * s * Math.sin(p * Math.PI) + 0.15;
                    double a = arm * Math.PI / 2 + p * 7 + f * 9;
                    double h = p * 5 * s;
                    out.dust(o.clone().add(rr * Math.cos(a), h, rr * Math.sin(a)), 0.9f, PURPLE, 1, 0, 0.05, 0, 0);
                }
            }
            Location top = o.clone().add(0, 5 * s, 0);
            out.line(Particle.END_ROD, o.clone().add(0, 0.2, 0), top, 0.5, d);
            if (f > 0.85) { // impact shockwave at the top
                double e = (f - 0.85) / 0.15;
                out.boom(top, true);
                out.ring(Particle.GLOW, top, Geo.easeOut(e) * 3 * s, 34, false, d);
                out.sphere(Particle.ELECTRIC_SPARK, top, 1.2 * s, 26, d);
            }
            return true;
        }
    }

    // 31. Pulsating shield: dome pulses, waves reflected outward
    public static class PulsatingShield extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public PulsatingShield(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double pulse = 1 + 0.18 * Math.sin(t * 0.4);
            double r = 2.6 * s * pulse;
            int n = out.count(50, d);
            for (int i = 0; i < n; i++) {
                double phi = RND01() * Math.PI * 2, th = RND01() * Math.PI / 2; // upper hemisphere
                double st = Math.sin(th);
                out.point(Particle.ELECTRIC_SPARK, o.clone().add(r * st * Math.cos(phi), r * Math.cos(th), r * st * Math.sin(phi)), 1, 0, 0, 0, 0);
            }
            if (t % 20 == 0) { // reflected wave
                out.dustRing(o, r * 1.5, 1.1f, LIGHTBLUE, 40, false, d);
                out.ring(Particle.GLOW, o, r * 1.8, 40, false, d);
            }
            if (t % 30 == 0) out.flash(o.clone().add(0, r, 0));
            return true;
        }
    }

    // 32. Crystal: growing octahedron, then shatters
    public static class Crystal extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Crystal(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double size = f < 0.7 ? Geo.easeOut(f / 0.7) * 2.2 * s : 2.2 * s;
            Location c = o.clone().add(0, 2.2 * s, 0);
            // octahedron edges
            Vector[] verts = {new Vector(0, size, 0), new Vector(0, -size, 0), new Vector(size, 0, 0),
                    new Vector(-size, 0, 0), new Vector(0, 0, size), new Vector(0, 0, -size)};
            int[][] edges = {{0,2},{0,3},{0,4},{0,5},{1,2},{1,3},{1,4},{1,5},{2,4},{4,3},{3,5},{5,2}};
            double spin = f * 4;
            for (int[] ed : edges) {
                Vector a = Geo.rotY(verts[ed[0]], spin), b = Geo.rotY(verts[ed[1]], spin);
                out.line(Particle.GLOW, c.clone().add(a), c.clone().add(b), 0.3, d);
            }
            if (f > 0.75) { // shatter
                double e = (f - 0.75) / 0.25;
                if (e < 0.15) out.boom(c, false);
                for (int i = 0; i < out.count(30, d); i++) {
                    double a = RND01() * Math.PI * 2, r = e * 4 * s;
                    out.point(Particle.CRIT, c.clone().add(r * Math.cos(a), RND01() * 3 * s, r * Math.sin(a)), 1, 0.1, 0.1, 0.1, 0.05);
                }
            }
            return true;
        }
    }

    // 33. Crystal explosion: eight crystal rays from the center
    public static class CrystalExplosion extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public CrystalExplosion(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            for (int ray = 0; ray < 8; ray++) {
                double th = Math.PI * (ray % 2 == 0 ? 0.35 : 0.75), phi = ray * Math.PI / 4;
                Vector dir = new Vector(Math.sin(th) * Math.cos(phi), Math.cos(th), Math.sin(th) * Math.sin(phi));
                double grow = Geo.easeOut(Math.min(1, f * 2.2)) * 4 * s * (ray % 2 == 0 ? 1 : 0.7);
                Location tip = o.clone().add(dir.getX() * grow, dir.getY() * grow + 1.5 * s, dir.getZ() * grow);
                out.line(Particle.GLOW, o.clone().add(0, 1.5 * s, 0), tip, 0.3, d);
                out.dust(tip, 1.2f, CYAN, 2, 0, 0, 0, 0);
                if (f > 0.85) out.point(Particle.CRIT, tip, 2, 0.1, 0.1, 0.1, 0.05);
            }
            if (f < 0.2) out.flash(o.clone().add(0, 1.5 * s, 0));
            return true;
        }
    }
}
