package dev.particlefx.core;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

/** Context passed to every animation tick: origin, limits and a particle emitter. */
public final class FxTick {
    public final Location origin;
    public final double scale;
    public final double density;
    public final int duration;
    private final Emitter out;

    public FxTick(Location origin, double scale, double density, int duration, Emitter out) {
        this.origin = origin; this.scale = scale; this.density = density;
        this.duration = duration; this.out = out;
    }

    public int n(int base) { return Emitter.count(base, density); }

    public void point(Particle p, Location l, int count, double dx, double dy, double dz, double speed) {
        out.point(p, l, count, dx, dy, dz, speed);
    }
    public void dust(Location l, double size, Color c, int count, double dx, double dy, double dz, double speed) {
        out.dust(l, size, c, count, dx, dy, dz, speed);
    }
    public void sphere(Particle p, Location c, double r, int points) { out.sphere(p, c, r, points, density); }
    public void dustSphere(Location c, double r, double size, Color col, int points) { out.dustSphere(c, r, size, col, points, density); }
    public void ring(Particle p, Location c, double r, int points, boolean vertical) { out.ring(p, c, r, points, vertical, density); }
    public void dustRing(Location c, double r, double size, Color col, int points, boolean vertical) { out.dustRing(c, r, size, col, points, vertical, density); }
    public void flash(Location c) { out.flash(c); }
    public void boom(Location c, boolean big) { out.boom(c, big); }
    public void line(Particle p, Location a, Location b, double step) { out.line(p, a, b, step, density); }
    public Location at(double x, double y, double z) { return origin.clone().add(x * scale, y * scale, z * scale); }
}
