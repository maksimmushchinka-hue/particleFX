package dev.particlefx.fx;

import dev.particlefx.core.BaseFx;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.Emitter;
import dev.particlefx.core.Geo;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/** Effects 35-43: shapes and nature. */
public final class ShapeFx {
    private ShapeFx() {}
    private static double RND01() { return java.util.concurrent.ThreadLocalRandom.current().nextDouble(); }

    // 35. Star rain: particles rise, turn and fall on several paths
    public static class StarRain extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public StarRain(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            for (int k = 0; k < 5; k++) {
                double p = Geo.frac(f * 1.3 + k * 0.2);
                double a = k * 1.26;
                double x = Math.cos(a) * 2.5 * s, z = Math.sin(a) * 2.5 * s;
                double y;
                if (p < 0.35) y = p / 0.35 * 5 * s;           // rise
                else if (p < 0.45) y = 5 * s;                  // turn
                else y = Math.max(0, 5 - (p - 0.45) / 0.55 * 5.5) * s; // fall
                Location pt = o.clone().add(x, y, z);
                out.point(Particle.FIREWORK, pt, 1, 0, 0, 0, 0);
                out.dust(pt, 0.8f, GOLD, 1, 0, 0, 0, 0);
                if (p > 0.97) out.flash(o.clone().add(x, 0.2, z));
            }
            if (f > 0.9) out.ring(Particle.GLOW, o, 3 * s, 36, false, d);
            return true;
        }
    }

    // 36. Spiral galaxy: arms rotate and slowly unfold
    public static class SpiralGalaxy extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public SpiralGalaxy(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double unfold = 0.5 + 1.5 * Geo.easeOut(Math.min(1, f * 1.5));
            for (int arm = 0; arm < 2; arm++) {
                for (int i = 0; i < 26; i++) {
                    double p = i / 25.0;
                    double r = (0.3 + p * 3) * unfold * s;
                    double a = arm * Math.PI + p * 4.5 + f * 5;
                    out.dust(o.clone().add(r * Math.cos(a), p * 0.6 * s, r * Math.sin(a)),
                            1.0f, p < 0.3 ? WHITE : (arm == 0 ? BLUE : MAGENTA), 1, 0, 0, 0, 0);
                }
            }
            out.dustSphere(o.clone().add(0, 0.3 * s, 0), 0.5 * s, 1.6f, GOLD, 18, d);
            if (t % 14 == 0) out.flash(o.clone().add(0, 0.3 * s, 0));
            return true;
        }
    }

    // 37. Geometric cube: edges are drawn, then the cube breaks into faces
    public static class GeometricCube extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        private static final int[][] EDGES = {{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}};
        public GeometricCube(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        private Vector corner(int c, double half) {
            return new Vector(((c & 1) == 0 ? -1 : 1) * half, ((c & 2) == 0 ? -1 : 1) * half, ((c & 4) == 0 ? -1 : 1) * half);
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double half = 1.5 * s;
            int edgesToDraw = (int) Math.min(EDGES.length, f * 1.6 * EDGES.length);
            Location c = o.clone().add(0, 2.4 * s, 0);
            double spin = f * 3;
            for (int i = 0; i < edgesToDraw; i++) {
                Vector a = Geo.rotY(corner(EDGES[i][0], half), spin), b = Geo.rotY(corner(EDGES[i][1], half), spin);
                out.line(Particle.WAX_ON, c.clone().add(a), c.clone().add(b), 0.35, d);
            }
            if (f > 0.7) { // faces fly apart
                double e = (f - 0.7) / 0.3;
                int[][] faces = {{0,1,2,3},{4,5,6,7},{0,1,5,4},{2,3,7,6},{1,2,6,5},{0,3,7,4}};
                for (int fi = 0; fi < 6; fi++) {
                    Vector dir = Geo.rotY(new Vector(faces[fi][0] == 0 ? -1 : 1, fi == 0 ? 1 : (fi == 1 ? -1 : 0.3), fi < 2 ? 0 : 1), spin)
                            .normalize().multiply(e * 3 * s);
                    for (int i = 0; i < 6; i++) {
                        Vector v = Geo.rotY(corner(faces[fi][i], half), spin).add(dir);
                        out.dust(c.clone().add(v), 0.9f, CYAN, 1, 0, 0, 0, 0);
                    }
                }
            }
            return true;
        }
    }

    // 38. Tesseract: two counter-rotating cubes suddenly split apart
    public static class Tesseract extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        private static final int[][] EDGES = {{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}};
        public Tesseract(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        private Vector corner(int c, double half) {
            return new Vector(((c & 1) == 0 ? -1 : 1) * half, ((c & 2) == 0 ? -1 : 1) * half, ((c & 4) == 0 ? -1 : 1) * half);
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double gap = f < 0.8 ? 0.5 : 0.5 + (f - 0.8) * 14; // sudden split
            Location c = o.clone().add(0, 2.4 * s, 0);
            for (int cube = 0; cube < 2; cube++) {
                double half = cube == 0 ? 1.2 * s : 0.7 * s;
                double spin = f * (cube == 0 ? 3 : -4);
                double off = (cube == 0 ? -1 : 1) * gap * s;
                for (int[] ed : EDGES) {
                    Vector a = Geo.rotY(corner(ed[0], half), spin), b = Geo.rotY(corner(ed[1], half), spin);
                    out.line(cube == 0 ? Particle.GLOW : Particle.END_ROD,
                            c.clone().add(a.getX(), a.getY() + off, a.getZ()),
                            c.clone().add(b.getX(), b.getY() + off, b.getZ()), 0.3, d);
                }
            }
            if (f > 0.8 && t % 4 == 0) out.flash(c);
            return true;
        }
    }

    // 39. Hourglass: sand flows between two volumetric halves, then flips
    public static class Hourglass extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Hourglass(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        private void frame(Location c, double half, double h) {
            for (int i = -1; i <= 1; i += 2) {
                Location top = c.clone().add(i * half, h, 0), bot = c.clone().add(i * half, -h, 0);
                out.line(Particle.WAX_ON, top, c, 0.3, d);
                out.line(Particle.WAX_ON, bot, c, 0.3, d);
            }
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            Location c = o.clone().add(0, 2.6 * s, 0);
            double spin = f < 0.8 ? 0 : (f - 0.8) * 16; // flip at the end
            double half = 1.3 * s, h = 2.2 * s;
            for (int i = 0; i < 4; i++) {
                double a = i * Math.PI / 2;
                Location top = c.clone().add(Geo.rotY(new Vector(half, h, 0), a + spin).add(new Vector(0, offY(spin), 0)));
                out.line(Particle.WAX_ON, c.clone().add(Geo.rotY(new Vector(half, 0, 0), a + spin)), top, 0.3, d);
                out.line(Particle.WAX_ON, c.clone().add(Geo.rotY(new Vector(half, 0, 0), a + spin)),
                        c.clone().add(Geo.rotY(new Vector(half, -h, 0), a + spin)), 0.3, d);
            }
            double fill = Math.min(1, f / 0.7); // sand flows
            for (int i = 0; i < out.count(30, d); i++) {
                double p = RND01();
                double y = (1 - fill) * 2 * s * p - (fill * p * 2 - p * 2) * 0; // upper pile drains
                double topY = (1 - fill) * h * p;
                double botY = -h + fill * h * p;
                out.dust(c.clone().add(RND01() * 0.3 * s - 0.15 * s, topY, RND01() * 0.3 * s - 0.15 * s), 0.8f, GOLD, 1, 0, -0.02, 0, 0);
                out.dust(c.clone().add(RND01() * 0.35 * s - 0.17 * s, botY, RND01() * 0.35 * s - 0.17 * s), 0.8f, GOLD, 1, 0, 0.02, 0, 0);
            }
            if (f > 0.8) out.flash(c);
            return true;
        }
        private double offY(double spin) { return Math.sin(spin) * 0; }
    }

    // 40. DNA spiral: two helical lines with rungs, fast unwinding
    public static class DnaSpiral extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public DnaSpiral(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double w = 1 + (f > 0.7 ? 2.2 : 0); // fast unwinding moment
            for (int i = 0; i < 40; i++) {
                double p = i / 39.0;
                double y = p * 5 * s, a = p * 10 + f * 8 * w;
                double r = 0.9 * s;
                out.dust(o.clone().add(r * Math.cos(a), y, r * Math.sin(a)), 0.9f, GREEN, 1, 0, 0, 0, 0);
                out.dust(o.clone().add(-r * Math.cos(a), y, -r * Math.sin(a)), 0.9f, MAGENTA, 1, 0, 0, 0, 0);
                if (i % 5 == 0) { // rung
                    out.line(Particle.END_ROD,
                            o.clone().add(r * Math.cos(a), y, r * Math.sin(a)),
                            o.clone().add(-r * Math.cos(a), y, -r * Math.sin(a)), 0.4, d);
                }
            }
            return true;
        }
    }

    // 41. Heart: outline forms, pulses, becomes expanding rings
    public static class Heart extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public Heart(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        private Vector heartPoint(double u, double k) {
            double x = 16 * Math.pow(Math.sin(u), 3);
            double y = 13 * Math.cos(u) - 5 * Math.cos(2 * u) - 2 * Math.cos(3 * u) - Math.cos(4 * u);
            return new Vector(x / 16, y / 16 + 2.4, 0).multiply(k);
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double grow = Geo.easeOut(Math.min(1, f * 1.8)) * s;
            double pulse = f > 0.4 && f < 0.8 ? 1 + 0.15 * Math.sin(t * 0.8) : 1;
            int n = out.count(60, d);
            for (int i = 0; i < n; i++) {
                double u = 2 * Math.PI * i / n;
                Vector v = Geo.rotY(heartPoint(u, grow * pulse), f * 1.5);
                out.dust(o.clone().add(v), 1.1f, RED, 1, 0, 0, 0, 0);
                if (i % 6 == 0) out.point(Particle.HEART, o.clone().add(v.getX(), v.getY(), v.getZ()), 1, 0, 0, 0, 0);
            }
            if (f > 0.8) { // expanding rings
                double e = (f - 0.8) / 0.2;
                out.ring(Particle.GLOW, o.clone().add(0, 2.4 * s, 0), Geo.easeOut(e) * 4 * s, 40, false, d);
                out.ring(Particle.HEART, o.clone().add(0, 2.4 * s, 0), Geo.easeOut(e) * 3 * s, 30, false, d);
            }
            return true;
        }
    }

    // 42. Flower burst: petals unfold in several layers
    public static class FlowerBurst extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        private final Color[] petal = {MAGENTA, RED, YELLOW};
        public FlowerBurst(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            for (int layer = 0; layer < 3; layer++) {
                double start = layer * 0.25;
                if (f < start) continue;
                double open = Geo.easeOut(Math.min(1, (f - start) / (1 - start))) * (1.4 + layer * 0.5) * s;
                int petals = 6 + layer * 2;
                for (int i = 0; i < petals; i++) {
                    double a = 2 * Math.PI * i / petals + layer * 0.4;
                    for (int k = 0; k <= 6; k++) {
                        double p = k / 6.0, curve = Math.sin(p * Math.PI) * 0.5;
                        Location l = o.clone().add(open * p * Math.cos(a), 1.5 * s + curve * s - layer * 0.4 * s, open * p * Math.sin(a));
                        out.dust(l, 1.0f, petal[layer], 1, 0, 0, 0, 0);
                    }
                }
            }
            out.dust(o.clone().add(0, 1.5 * s, 0), 1.4f, GOLD, out.count(5, d), 0.2, 0.2, 0.2, 0);
            if (f > 0.9) out.ring(Particle.CHERRY_LEAVES, o.clone().add(0, 1.5 * s, 0), 3 * s, 34, false, d);
            return true;
        }
    }

    // 43. Tree of life: trunk and branches grow, light wave runs through the crown
    public static class TreeOfLife extends BaseFx {
        private final Location o; private final Emitter out;
        private final double s, d; private final int dur;
        public TreeOfLife(Location o, EffectSettings set, Emitter out) {
            this.o = o; this.out = out; this.s = set.scale; this.d = set.density; this.dur = set.duration;
        }
        @Override public boolean tick(int t) {
            double f = phase(t, dur);
            double grow = Geo.easeOut(Math.min(1, f * 1.4));
            // trunk
            out.line(Particle.DUST_PLUME, o.clone().add(0, 0.2, 0), o.clone().add(0, grow * 3 * s, 0), 0.35, d);
            out.dust(o.clone().add(0, grow * 3 * s, 0), 1.0f, Color.fromRGB(120, 80, 40), 2, 0.1, 0.1, 0.1, 0);
            // branches
            for (int b = 0; b < 5; b++) {
                double a = b * 2 * Math.PI / 5;
                double bl = grow * 1.8 * s;
                out.line(Particle.DUST_PLUME,
                        o.clone().add(0, grow * 3 * s, 0),
                        o.clone().add(bl * Math.cos(a), grow * 3 * s + bl * 0.6, bl * Math.sin(a)), 0.3, d);
            }
            // crown sphere
            double cr = grow * 1.6 * s;
            out.dustSphere(o.clone().add(0, grow * 3.6 * s, 0), cr, 1.2f, GREEN, 30, d);
            // light wave through the crown
            if (f > 0.75) {
                double e = (f - 0.75) / 0.25;
                out.dustRing(o.clone().add(0, grow * 3.6 * s - e * 1.5 * s, 0), cr * (1 - e * 0.4), 1.4f, GOLD, 30, false, d);
                if (f > 0.97) out.flash(o.clone().add(0, grow * 3.6 * s, 0));
            }
            return true;
        }
    }
}
