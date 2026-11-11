package dev.particlefx;

import dev.particlefx.core.AnimationManager;
import dev.particlefx.core.AnimationRegistry;
import dev.particlefx.core.EffectConfig;
import dev.particlefx.core.ParticleBudget;
import dev.particlefx.gui.GuiListener;
import dev.particlefx.gui.GuiManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParticleFXPlugin extends JavaPlugin {
    private ParticleBudget budget;
    private EffectConfig config;
    private AnimationManager manager;
    private AnimationRegistry registry;
    private GuiManager gui;

    @Override public void onEnable() {
        saveDefaultConfig();
        reloadAll();
        getCommand("particlefx").setExecutor(new ParticleFXCommand(this));
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getLogger().info("ParticleFX включён: " + registry.size() + " эффектов");
    }

    @Override public void onDisable() {
        if (manager != null) manager.stopEverything();
    }

    public void reloadAll() {
        reloadConfig();
        config = new EffectConfig(this);
        if (budget == null) budget = new ParticleBudget(config.maxParticlesPerTick);
        else budget = new ParticleBudget(config.maxParticlesPerTick);
        if (manager != null) manager.stopEverything();
        manager = new AnimationManager(this, budget, config.maxActivePerPlayer, config.maxActiveServer);
        if (registry == null) registry = new AnimationRegistry();
        if (gui == null) gui = new GuiManager(this);
        else gui.invalidate();
    }

    public ParticleBudget budget() { return budget; }
    public EffectConfig fxConfig() { return config; }
    public AnimationManager manager() { return manager; }
    public AnimationRegistry registry() { return registry; }
    public GuiManager gui() { return gui; }
}
