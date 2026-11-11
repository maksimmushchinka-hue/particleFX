package dev.particlefx.core;

import dev.particlefx.ParticleFXPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/** Reads limits and per-effect toggles from config.yml. */
public final class EffectConfig {
    public final double maxScale;
    public final int maxDuration;
    public final double maxSpeed;
    public final int maxParticlesPerTick;
    public final int maxActivePerPlayer;
    public final int maxActiveServer;
    public final boolean previewEnabled;
    public final Map<String, Boolean> enabled = new HashMap<>();

    public EffectConfig(ParticleFXPlugin plugin) {
        maxScale = clamp(plugin.getConfig().getDouble("limits.max-scale", 5.0), 1.0, 20.0);
        maxDuration = (int) clamp(plugin.getConfig().getDouble("limits.max-duration", 400), 40, 1200);
        maxSpeed = clamp(plugin.getConfig().getDouble("limits.max-speed", 3.0), 0.1, 10.0);
        maxParticlesPerTick = (int) clamp(plugin.getConfig().getDouble("limits.max-particles-per-tick", 4000), 100, 100000);
        maxActivePerPlayer = (int) clamp(plugin.getConfig().getDouble("limits.max-active-per-player", 2), 1, 20);
        maxActiveServer = (int) clamp(plugin.getConfig().getDouble("limits.max-active-server", 40), 1, 500);
        previewEnabled = plugin.getConfig().getBoolean("preview.enabled", true);
        ConfigurationSection fx = plugin.getConfig().getConfigurationSection("effects");
        if (fx != null) {
            for (String key : fx.getKeys(false)) enabled.put(key, fx.getBoolean(key + ".enabled", true));
        }
    }

    public boolean isEnabled(String id) { return enabled.getOrDefault(id, true); }

    public static double clamp(double v, double min, double max) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return min;
        return Math.max(min, Math.min(max, v));
    }
}
