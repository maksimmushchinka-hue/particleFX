package dev.particlefx.fx;

import dev.particlefx.core.BaseFx;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.Emitter;
import dev.particlefx.core.Geo;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Deque;

/** Effects 1-10: space. */
public final class SpaceFx {
    private SpaceFx() {}

    // 1. Supernova: core implodes, shells expand, final burst of rays
    public static class Supernova extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Supernova(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            if (f < 0.2) { // implosion
                double r = 2.5 - 11 * f * s;
                out.dustSphere(o, Math.max(0.1, r), 1.2f, YELLOW, 30, d);
                if (t % 4 == 0) out.flash(o);
            } else if (f < 0.7) { // expanding shells
                double r = Geo.easeOut((f - 0.2) / 0.5) * 6 * s;
                out.dustRing(o, r, 1.0f, ORANGE, 40, false, d);
                out.dustRing(o, r * 0.7, 1.4f, RED, 30, false, d);
                out.sphere(Particle.FLAME, o, r * 0.5, 25, d);
            } else { // sharp burst: flash + rays
                if (t % 7 == 0) out.boom(o, true);
                for (int i = 0; i < 12; i++) {
                    double a = 2 * Math.PI * i / 12 + f * 4;
                    Vector v = new Vector(Math.cos(a) * 6 * s, (RND01() - 0.3) * 3 * s, Math.sin(a) * 6 * s);
                    out.line(Particle.CRIT, o, o.clone().add(v), 0.8, d);
                }
                out.dustSphere(o, 1.5 * s, 2.0f, WHITE, 40, d);
            }
            return true;
        }
    }

    // 2. Galactic vortex: four spiral arms suddenly unfold
    public static class GalacticVortex extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public GalacticVortex(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double spin = f * 10;
            double unfold = f < 0.75 ? 0.35 : (f < 0.85 ? 1.6 : 2.2); // sudden unfold at 75%
            for (int arm = 0; arm < 4; arm++) {
                for (int i = 0; i < 22; i++) {
                    double r = (0.4 + i * 0.22) * unfold * s;
                    double a = arm * Math.PI / 2 + spin + i * 0.16;
                    double y = Math.sin(i * 0.3 + f * 6) * 0.25 * s;
                    Color c = i % 2 == 0 ? LIGHTBLUE : PURPLE;
                    out.dust(o.clone().add(r * Math.cos(a), y, r * Math.sin(a)), 0.9f, c, 1, 0, 0.02, 0, 0);
                }
            }
            if (f < 0.75) out.dustSphere(o, 0.4 * s, 1.5f, WHITE, 12, d);
            else if (f > 0.75 && t % 10 == 0) out.flash(o);
            return true;
        }
    }

    // 3. Black hole: spiral infall then violent ejection
    public static class BlackHole extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public BlackHole(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            if (f < 0.8) {
                for (int i = 0; i < out.count(30, d); i++) {
                    double p = Geo.frac(f * 3 + i * 0.037);
                    double r = (1 - p) * 5 * s, a = p * 12 + i;
                    out.dust(o.clone().add(r * Math.cos(a), (1 - p) * 2 * s, r * Math.sin(a)),
                            0.8f, DARK, 1, 0, -0.05, 0, 0);
                    out.point(Particle.PORTAL, o.clone().add(r * Math.cos(a), (1 - p) * 2 * s, r * Math.sin(a)), 1, 0, 0, 0, 0);
                }
                out.dustSphere(o, 0.35 * s, 1.6f, Color.fromRGB(10, 5, 20), 16, d);
            } else { // violent ejection
                double e = (f - 0.8) / 0.2;
                if (e < 0.15) out.boom(o, true);
                out.sphere(Particle.REVERSE_PORTAL, o, 1 + Geo.easeOut(e) * 5 * s, 40, d);
                out.ring(Particle.CRIT, o, 1 + Geo.easeOut(e) * 6 * s, 36, false, d);
            }
            return true;
        }
    }

    // 4. Galaxy collision: two clouds approach, arc burst on impact
    public static class GalaxyCollision extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public GalaxyCollision(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            if (f < 0.7) {
                double sep = 1 - f / 0.7; // 1 -> 0
                for (int k = -1; k <= 1; k += 2) {
                    Location c = o.clone().add(k * sep * 3.5 * s, 1.5 * s, 0);
                    for (int i = 0; i < out.count(18, d); i++) {
                        double u = RND01() * 2 - 1, phi = RND01() * Math.PI * 2, r = 1.2 * s;
                        double sq = Math.sqrt(1 - u * u);
                        out.dust(c.clone().add(r * sq * Math.cos(phi), r * u, r * sq * Math.sin(phi)),
                                1.0f, k < 0 ? BLUE : MAGENTA, 1, 0, 0, 0, 0.02);
                    }
                }
            } else { // impact: arcs of particles
                double e = (f - 0.7) / 0.3;
                if (e < 0.1) { out.flash(o.clone().add(0, 1.5 * s, 0)); out.boom(o.clone().add(0, 1.5 * s, 0), true); }
                for (int a = 0; a < 6; a++) {
                    double ang = Math.PI * a / 6 + e * 2;
                    for (int i = 0; i < 14; i++) {
                        double rr = 0.5 + i * 0.35;
                        out.dust(o.clone().add(rr * Math.cos(ang) * s, 1.5 * s + rr * Math.sin(ang) * s, rr * Math.sin(ang) * s * 0.4),
                                0.9f, i % 2 == 0 ? YELLOW : WHITE, 1, 0, 0, 0, 0);
                    }
                }
            }
            return true;
        }
    }

    // 5. Nebula: layered colored clouds with sequential flashes
    public static class Nebula extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        private final Color[] pal = {PURPLE, MAGENTA, BLUE, CYAN};
        public Nebula(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            int layer = (int) (f * 4) % 4;
            for (int i = 0; i < out.count(26, d); i++) {
                double u = RND01() * 2 - 1, phi = RND01() * Math.PI * 2, r = (2 + RND01() * 2.5) * s;
                double sq = Math.sqrt(1 - u * u);
                Color c = pal[(i + t / 20) % pal.length];
                out.dust(o.clone().add(r * sq * Math.cos(phi), (u + 1.5) * s, r * sq * Math.sin(phi)),
                        1.6f, c, 1, 0, 0.01, 0, 0.02);
            }
            int flashStage = (int) (f * 4);
            if (t % 20 == 0 && flashStage < 4) out.flash(o.clone().add(0, (1.5 + flashStage) * s, 0));
            if (f > 0.9) { out.boom(o, true); out.ring(Particle.GLOW, o, 4 * s, 40, false, d); }
            return true;
        }
    }

    // 6. Saturn rings: planet + rings expand, sudden tilt change
    public static class SaturnRings extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public SaturnRings(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double grow = 0.6 + 0.4 * Geo.easeOut(Math.min(1, f * 2));
            double tilt = f < 0.75 ? 0.35 : 1.1; // sudden tilt jump
            Location pc = o.clone().add(0, 2 * s, 0);
            out.dustSphere(pc, 1.1 * s * grow, 1.4f, Color.fromRGB(240, 210, 140), 26, d);
            double spin = f * 6;
            for (int ringI = 0; ringI < 3; ringI++) {
                double r = (1.8 + ringI * 0.5) * s * grow;
                int n = out.count(30 - ringI * 4, d);
                for (int i = 0; i < n; i++) {
                    double a = 2 * Math.PI * i / n + spin * (ringI % 2 == 0 ? 1 : -1);
                    Vector v = Geo.orbitPoint(r, a, tilt);
                    out.dust(pc.clone().add(v), 0.8f, ringI == 0 ? LIGHTBLUE : GOLD, 1, 0, 0, 0, 0);
                }
            }
            if (t % 15 == 0 && f > 0.7 && f < 0.9) out.flash(pc);
            return true;
        }
    }

    // 7. Meteor shower: streams with long trails, final big fall
    public static class MeteorShower extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public MeteorShower(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            for (int m = 0; m < 5; m++) {
                double p = Geo.frac(f * 1.4 + m * 0.19);
                Location head = o.clone().add((m - 2) * 1.4 * s + p * 2 * s, 7 * s - p * 7 * s, p * 1.5 * s);
                for (int k = 0; k < 6; k++) {
                    double tail = k * 0.5;
                    Location tl = head.clone().add(-tail * 0.6 * s, tail * 0.8 * s, -tail * 0.2 * s);
                    out.dust(tl, 0.9f, k < 2 ? ORANGE : RED, 1, 0, 0, 0, 0);
                }
                out.point(Particle.FLAME, head, 2, 0.1, 0.1, 0.1, 0.02);
            }
            if (f > 0.85) { // final heavy meteor
                Location g = o.clone().add(0, Math.max(0.2, (1 - f) / 0.15 * 4) * s, 0);
                out.point(Particle.LAVA, g, 3, 0.3, 0.1, 0.3, 0);
                if (f > 0.97) { out.boom(o, true); out.ring(Particle.FLAME, o, 3.5 * s, 40, false, d); }
            }
            return true;
        }
    }

    // 8. Comet: bright nucleus with rotating tail that suddenly unfolds
    public static class Comet extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Comet(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            Location c = o.clone().add(Math.cos(f * 6) * 2 * s, 2.5 * s + Math.sin(f * 6) * 0.8 * s, Math.sin(f * 6) * 2 * s);
            out.point(Particle.END_ROD, c, 2, 0.05, 0.05, 0.05, 0);
            double spread = f < 0.8 ? 0.5 : 2.2; // sudden tail unfold
            for (int i = 0; i < out.count(26, d); i++) {
                double a = i * 0.7 + f * 10, r = 0.3 + RND01() * spread * s;
                out.dust(c.clone().add(-r * Math.cos(a) * 0.7, r * Math.sin(a) * 0.4, -r * Math.sin(a) * 0.7),
                        0.7f, LIGHTBLUE, 1, 0, 0, 0, 0);
            }
            if (f > 0.8 && t % 8 == 0) out.flash(c);
            return true;
        }
    }

    // 9. Planetary system: planets on inclined orbits with a final pulse
    public static class PlanetarySystem extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public PlanetarySystem(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double pulse = f > 0.9 ? 1 + Math.sin((f - 0.9) * 30) * 0.4 : 1;
            for (int p = 0; p < 4; p++) {
                double r = (1.2 + p * 0.9) * s * pulse, w = 1.6 - p * 0.25;
                double a = f * w * 8 + p * 2.1;
                Vector v = Geo.orbitPoint(r, a, p * 0.55);
                Location pl = o.clone().add(0, 2 * s, 0).add(v);
                out.dust(pl, 1.2f, new Color[]{RED, GOLD, GREEN, BLUE}[p], 3, 0.05, 0.05, 0.05, 0);
                double a2 = a + 0.5;
                out.point(Particle.GLOW, o.clone().add(0, 2 * s, 0).add(Geo.orbitPoint(r, a2, p * 0.55)), 1, 0, 0, 0, 0);
            }
            out.dust(o.clone().add(0, 2 * s, 0), 1.6f, YELLOW, out.count(4, d), 0.1, 0.1, 0.1, 0);
            return true;
        }
    }

    // 10. Atom: nucleus + electrons on tilted orbits with glowing SOUL trails
    public static class Atom extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        private final Deque<Location>[] trails = new ArrayDeque[3];
        private final double[] incl = {0, Math.PI / 3, 2 * Math.PI / 3};
        private final double[] spin = {1.2, -1.0, 0.8};
        public Atom(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
            for (int i = 0; i < 3; i++) trails[i] = new ArrayDeque<>();
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double boost = (f > 0.7 && f < 0.85) ? 2.5 : 1; // sharp spin-up moment
            // nucleus: pulsing dense cluster
            double nr = (0.35 + Math.sin(f * 12) * 0.06) * s;
            out.dustSphere(o, nr, 1.5f, RED, 14, d);
            out.point(Particle.DUST_PLUME, o, out.count(3, d), 0.1, 0.1, 0.1, 0.02);
            // electrons on three mutually inclined orbits
            for (int e = 0; e < 3; e++) {
                double a = f * 8 * spin[e] * boost + e * 2.4;
                Vector v = Geo.orbitPoint(1.8 * s, a, incl[e]);
                v = Geo.rotY(v, e * 2.1);
                Location pos = o.clone().add(v);
                out.point(Particle.GLOW, pos, 2, 0.02, 0.02, 0.02, 0);
                // orbit guide ring (faint)
                if (t % 6 == 0) {
                    for (int i = 0; i < 20; i++) {
                        double ga = 2 * Math.PI * i / 20;
                        Vector gv = Geo.orbitPoint(1.8 * s, ga, incl[e]);
                        out.dust(o.clone().add(Geo.rotY(gv, e * 2.1)), 0.5f, new Color[]{LIGHTBLUE, CYAN, PURPLE}[e], 1, 0, 0, 0, 0);
                    }
                }
                // glowing trail
                trails[e].addFirst(pos.clone());
                if (trails[e].size() > 8) trails[e].removeLast();
                int k = 0;
                for (Location tl : trails[e]) {
                    out.dust(tl, 0.7f, new Color[]{CYAN, LIGHTBLUE, PURPLE}[e], 1, 0, 0, 0, 0);
                    if (++k > 5) break;
                }
            }
            return true;
        }
    }

    private static double RND01() { return java.util.concurrent.ThreadLocalRandom.current().nextDouble(); }
}
