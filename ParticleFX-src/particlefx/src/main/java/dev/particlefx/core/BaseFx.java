package dev.particlefx.core;

import org.bukkit.Color;

/** Shared helpers for animations: palettes and stage math. */
public abstract class BaseFx implements Fx {
    protected static final Color WHITE = Color.fromRGB(255, 255, 255);
    protected static final Color RED = Color.fromRGB(255, 60, 40);
    protected static final Color ORANGE = Color.fromRGB(255, 140, 40);
    protected static final Color YELLOW = Color.fromRGB(255, 220, 60);
    protected static final Color GREEN = Color.fromRGB(80, 230, 120);
    protected static final Color BLUE = Color.fromRGB(80, 150, 255);
    protected static final Color LIGHTBLUE = Color.fromRGB(150, 220, 255);
    protected static final Color PURPLE = Color.fromRGB(180, 90, 255);
    protected static final Color MAGENTA = Color.fromRGB(255, 90, 200);
    protected static final Color CYAN = Color.fromRGB(60, 230, 230);
    protected static final Color DARK = Color.fromRGB(30, 20, 40);
    protected static final Color GOLD = Color.fromRGB(255, 200, 80);

    protected static double phase(int t, int dur) { return Geo.clamp01((double) t / dur); }
}
