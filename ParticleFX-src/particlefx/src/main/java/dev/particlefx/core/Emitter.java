package dev.particlefx.core;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.Random;

/** All particle spawning goes through here. Enforces the global per-tick budget. */
public final class Emitter {
    private static final Random RND = new Random();
    private final ParticleBudget budget;
    private final World world;
    private final Particle.DustOptions defaultDust;

    public Emitter(World world, ParticleBudget budget) {
        this.world = world;
        this.budget = budget;
        this.defaultDust = new Particle.DustOptions(Color.fromRGB(255, 255, 255), 1.0f);
    }

    public World world() { return world; }

    public static int count(int base, double density) {
        return Math.max(1, (int) Math.round(base * Math.max(0.1, Math.min(3.0, density))));
    }

    private boolean ok(Location l) {
        return l != null && l.getWorld() != null && !Double.isNaN(l.getX()) && !Double.isInfinite(l.getX());
    }

    public void point(Particle p, Location l, int count, double dx, double dy, double dz, double speed) {
        if (!ok(l) || budget.exhausted()) return;
        int n = Math.min(count, budget.maxPerTick - (budget.used));
        if (n <= 0) return;
        budget.spend(n);
        world.spawnParticle(p, l, Math.max(1, n), dx, dy, dz, speed, null);
    }

    public void dust(Location l, double size, Color c, int count, double dx, double dy, double dz, double speed) {
        if (!ok(l) || budget.exhausted()) return;
        int n = Math.min(count, budget.maxPerTick - budget.used);
        if (n <= 0) return;
        budget.spend(n);
        world.spawnParticle(Particle.DUST, l, Math.max(1, n), dx, dy, dz, speed, new Particle.DustOptions(c, (float) size));
    }

    public void sphere(Particle p, Location c, double r, int points, double density) {
        int n = count(points, density);
        for (int i = 0; i < n; i++) {
            double u = RND.nextDouble() * 2 - 1, phi = RND.nextDouble() * Math.PI * 2;
            double s = Math.sqrt(1 - u * u);
            point(p, c.clone().add(r * s * Math.cos(phi), r * u, r * s * Math.sin(phi)), 1, 0, 0, 0, 0);
        }
    }

    public void dustSphere(Location c, double r, double size, Color col, int points, double density) {
        int n = count(points, density);
        for (int i = 0; i < n; i++) {
            double u = RND.nextDouble() * 2 - 1, phi = RND.nextDouble() * Math.PI * 2;
            double s = Math.sqrt(1 - u * u);
            dust(c.clone().add(r * s * Math.cos(phi), r * u, r * s * Math.sin(phi)), size, col, 1, 0, 0, 0, 0);
        }
    }

    public void ring(Particle p, Location c, double r, int points, boolean vertical, double density) {
        int n = count(points, density);
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            Location l = vertical ? c.clone().add(r * Math.cos(a), r * Math.sin(a), 0)
                                  : c.clone().add(r * Math.cos(a), 0, r * Math.sin(a));
            point(p, l, 1, 0, 0, 0, 0);
        }
    }

    public void dustRing(Location c, double r, double size, Color col, int points, boolean vertical, double density) {
        int n = count(points, density);
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            Location l = vertical ? c.clone().add(r * Math.cos(a), r * Math.sin(a), 0)
                                  : c.clone().add(r * Math.cos(a), 0, r * Math.sin(a));
            dust(l, size, col, 1, 0, 0, 0, 0);
        }
    }

    public void flash(Location c) {
        if (ok(c)) world.spawnParticle(Particle.FLASH, c, 1, 0, 0, 0, 0, null);
    }

    public void boom(Location c, boolean big) {
        if (!ok(c)) return;
        budget.spend(2);
        world.spawnParticle(big ? Particle.EXPLOSION_EMITTER : Particle.EXPLOSION, c, 1, 0, 0, 0, 0, null);
    }

    public void line(Particle p, Location a, Location b, double step, double density) {
        if (!ok(a) || !ok(b)) return;
        double d = a.toVector().distance(b.toVector());
        int n = count(Math.max(1, (int) (d / step)), density);
        for (int i = 0; i <= n; i++) {
            double f = (double) i / n;
            point(p, a.clone().add(b.clone().subtract(a).multiply(f)), 1, 0, 0, 0, 0);
        }
    }
}
