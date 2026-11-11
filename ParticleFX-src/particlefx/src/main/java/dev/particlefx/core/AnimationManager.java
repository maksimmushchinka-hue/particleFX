package dev.particlefx.core;

import dev.particlefx.ParticleFXPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Runs animations: one BukkitTask per effect, budget per tick, limits per player and per server. */
public final class AnimationManager {
    public static final class Running {
        public final UUID player;
        public final Fx fx;
        public final String id;
        public final int duration;
        public final List<BukkitTask> tasks = new ArrayList<>();
        public Running(UUID player, Fx fx, String id, int duration) {
            this.player = player; this.fx = fx; this.id = id; this.duration = duration;
        }
    }

    private final ParticleFXPlugin plugin;
    private final ParticleBudget budget;
    private final int maxPerPlayer;
    private final int maxPerServer;
    private final Map<UUID, List<Running>> byPlayer = new ConcurrentHashMap<>();
    private final List<Running> all = new ArrayList<>();

    public AnimationManager(ParticleFXPlugin plugin, ParticleBudget budget, int maxPerPlayer, int maxPerServer) {
        this.plugin = plugin;
        this.budget = budget;
        this.maxPerPlayer = maxPerPlayer;
        this.maxPerServer = maxPerServer;
    }

    public boolean start(Player p, Fx fx, String id, EffectSettings s) {
        List<Running> mine = byPlayer.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>());
        if (mine.size() >= maxPerPlayer) return false;
        if (all.size() >= maxPerServer) return false;
        Running run = new Running(p.getUniqueId(), fx, id, s.duration);
        BukkitTask task = new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                budget.resetTick();
                try {
                    boolean alive = p.isOnline() && fx.tick(t);
                    t++;
                    if (!alive || t >= s.duration) stop(run);
                } catch (Exception ex) {
                    plugin.getLogger().warning("Effect " + id + " failed: " + ex);
                    stop(run);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
        run.tasks.add(task);
        all.add(run);
        mine.add(run);
        return true;
    }

    public void stop(Running run) {
        for (BukkitTask t : run.tasks) t.cancel();
        all.remove(run);
        List<Running> mine = byPlayer.get(run.player);
        if (mine != null) mine.remove(run);
    }

    public int stopAll(Player p) {
        List<Running> mine = byPlayer.remove(p.getUniqueId());
        if (mine == null) return 0;
        int n = mine.size();
        for (Running r : new ArrayList<>(mine)) { for (BukkitTask t : r.tasks) t.cancel(); all.remove(r); }
        return n;
    }

    public void stopEverything() {
        for (Running r : new ArrayList<>(all)) { for (BukkitTask t : r.tasks) t.cancel(); }
        all.clear();
        byPlayer.clear();
    }

    public int activeCount() { return all.size(); }
}
