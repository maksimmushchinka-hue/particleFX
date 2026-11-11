package dev.particlefx.core;

import org.bukkit.util.Vector;

/** Geometry helpers: easing, vectors, rotations. */
public final class Geo {
    private Geo() {}

    public static double easeOut(double x) { return 1 - (1 - x) * (1 - x); }
    public static double easeIn(double x) { return x * x; }
    public static double clamp01(double x) { return x < 0 ? 0 : (x > 1 ? 1 : x); }
    public static double frac(double x) { return x - Math.floor(x); }

    public static Vector rotY(Vector v, double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new Vector(v.getX() * c - v.getZ() * s, v.getY(), v.getX() * s + v.getZ() * c);
    }
    public static Vector rotX(Vector v, double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new Vector(v.getX(), v.getY() * c - v.getZ() * s, v.getY() * s + v.getZ() * c);
    }
    public static Vector rotZ(Vector v, double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new Vector(v.getX() * c - v.getY() * s, v.getX() * s + v.getY() * c, v.getZ());
    }
    /** Point on a circle with inclination: rotate (r,0,0) around Z by incl, then around Y by phase. */
    public static Vector orbitPoint(double r, double phase, double incl) {
        Vector v = new Vector(r * Math.cos(phase), r * Math.sin(phase), 0);
        return rotZ(v, incl).clone(); // tilt around Z, then caller may rotY for orientation
    }
}
