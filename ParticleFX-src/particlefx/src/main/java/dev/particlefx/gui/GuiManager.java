package dev.particlefx.gui;

import dev.particlefx.ParticleFXPlugin;
import dev.particlefx.core.AnimationRegistry;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.Emitter;
import dev.particlefx.core.Fx;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Main menu (paginated) and per-effect settings menu. */
public final class GuiManager {
    public static final int MAIN_SIZE = 54;
    public static final int SETTINGS_SIZE = 54;
    // main menu: slots 0..44 items, bottom row 45..53 nav
    public static final int NAV_BACK = 45, NAV_PAGE = 49, NAV_NEXT = 53, NAV_CLOSE = 8;
    // settings menu slots
    public static final int S_BACK = 0, S_SCALE_M = 10, S_SCALE_INFO = 11, S_SCALE_P = 12;
    public static final int S_DUR_M = 14, S_DUR_INFO = 15, S_DUR_P = 16;
    public static final int S_SPD_M = 19, S_SPD_INFO = 20, S_SPD_P = 21;
    public static final int S_DEN_M = 23, S_DEN_INFO = 24, S_DEN_P = 25;
    public static final int S_MODE = 29, S_LAUNCH = 31, S_PREVIEW = 33;

    public static final class Session {
        public int page;
        public String fxId;
        public double scale = 1.0, speed = 1.0, density = 1.0;
        public int duration = 160;
        public boolean ahead = false;
    }

    private final ParticleFXPlugin plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public GuiManager(ParticleFXPlugin plugin) { this.plugin = plugin; }

    public Session session(Player p) { return sessions.get(p.getUniqueId()); }
    public void forget(Player p) { sessions.remove(p.getUniqueId()); }
    public void invalidate() { sessions.clear(); }

    private ItemStack item(Material m, String name, List<String> lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Component.text(name));
        if (lore != null && !lore.isEmpty()) {
            List<Component> ls = new ArrayList<>();
            for (String s : lore) ls.add(Component.text(s));
            meta.lore(ls);
        }
        it.setItemMeta(meta);
        return it;
    }

    public void openMainMenu(Player p) {
        Session s = sessions.computeIfAbsent(p.getUniqueId(), k -> new Session());
        List<AnimationRegistry.FxInfo> all = plugin.registry().all();
        List<AnimationRegistry.FxInfo> visible = new ArrayList<>();
        for (AnimationRegistry.FxInfo i : all) if (plugin.fxConfig().isEnabled(i.id())) visible.add(i);
        int pages = Math.max(1, (visible.size() + 44) / 45);
        if (s.page >= pages) s.page = pages - 1;

        FxHolder holder = new FxHolder(FxHolder.Kind.MAIN);
        Inventory inv = Bukkit.createInventory(holder, MAIN_SIZE,
                Component.text("ParticleFX — эффекты (стр. " + (s.page + 1) + "/" + pages + ")"));
        holder.bind(inv);

        int start = s.page * 45;
        for (int i = 0; i < 45 && start + i < visible.size(); i++) {
            AnimationRegistry.FxInfo info = visible.get(start + i);
            inv.setItem(i, item(info.icon(), info.name(),
                    List.of("§7" + info.desc(), "§8Частицы: §f" + info.particles(), "§8ID: §f" + info.id(),
                            "§eЛКМ — настройки и запуск")));
        }
        inv.setItem(NAV_BACK, item(Material.ARROW, "← Назад", List.of("§7Предыдущая страница")));
        inv.setItem(NAV_PAGE, item(Material.PAPER, "Страница " + (s.page + 1) + " / " + pages, null));
        inv.setItem(NAV_NEXT, item(Material.SPECTRAL_ARROW, "Вперёд →", List.of("§7Следующая страница")));
        inv.setItem(NAV_CLOSE, item(Material.BARRIER, "Закрыть", null));
        p.openInventory(inv);
    }

    public void openSettings(Player p) {
        Session s = session(p);
        AnimationRegistry.FxInfo info = plugin.registry().get(s.fxId);
        if (info == null) { openMainMenu(p); return; }
        FxHolder holder = new FxHolder(FxHolder.Kind.SETTINGS);
        Inventory inv = Bukkit.createInventory(holder, SETTINGS_SIZE, Component.text("Настройки: " + info.name()));
        holder.bind(inv);
        inv.setItem(S_BACK, item(Material.ARROW, "← К выбору анимаций", null));
        inv.setItem(S_SCALE_M, item(Material.RED_CONCRETE, "− Масштаб", null));
        inv.setItem(S_SCALE_INFO, item(Material.EYE_OF_ENDER, "Масштаб: " + fmt(s.scale), List.of("§7Размер формы эффекта")));
        inv.setItem(S_SCALE_P, item(Material.LIME_CONCRETE, "+ Масштаб", null));
        inv.setItem(S_DUR_M, item(Material.RED_CONCRETE, "− Длительность", null));
        inv.setItem(S_DUR_INFO, item(Material.CLOCK, "Длительность: " + s.duration + " тиков",
                List.of("§7" + (s.duration / 20) + " сек")));
        inv.setItem(S_DUR_P, item(Material.LIME_CONCRETE, "+ Длительность", null));
        inv.setItem(S_SPD_M, item(Material.RED_CONCRETE, "− Скорость", null));
        inv.setItem(S_SPD_INFO, item(Material.SUGAR, "Скорость: " + fmt(s.speed), List.of("§7Темп анимации")));
        inv.setItem(S_SPD_P, item(Material.LIME_CONCRETE, "+ Скорость", null));
        inv.setItem(S_DEN_M, item(Material.RED_CONCRETE, "− Плотность частиц", null));
        inv.setItem(S_DEN_INFO, item(Material.GLOWSTONE_DUST, "Плотность: " + fmt(s.density), null));
        inv.setItem(S_DEN_P, item(Material.LIME_CONCRETE, "+ Плотность частиц", null));
        inv.setItem(S_MODE, item(s.ahead ? Material.ENDER_PEARL : Material.BONE_MEAL,
                s.ahead ? "Режим: перед игроком" : "Режим: на месте игрока", List.of("§7ЛКМ — переключить")));
        inv.setItem(S_LAUNCH, item(Material.NETHER_STAR, "▶ Запустить эффект", List.of("§a" + info.name())));
        inv.setItem(S_PREVIEW, item(Material.ENDER_EYE, "Предпросмотр",
                plugin.fxConfig().previewEnabled ? List.of("§7Короткая версия эффекта") : List.of("§cВыключен в конфиге")));
        p.openInventory(inv);
    }

    private static String fmt(double v) {
        return (v == Math.rint(v)) ? String.valueOf((int) v) : String.format("%.1f", v);
    }

    public void handleClick(Player p, int slot) {
        Session s = session(p);
        if (s == null) return;
        AnimationRegistry.FxInfo info = plugin.registry().get(s.fxId);
        switch (slot) {
            case NAV_BACK -> { s.page = Math.max(0, s.page - 1); openMainMenu(p); }
            case NAV_NEXT -> { s.page++; openMainMenu(p); }
            case NAV_CLOSE -> p.closeInventory();
            case S_BACK -> openMainMenu(p);
            case S_SCALE_M -> { s.scale = dec(s.scale, 0.25); openSettings(p); }
            case S_SCALE_P -> { s.scale = inc(s.scale, 0.25, plugin.fxConfig().maxScale); openSettings(p); }
            case S_DUR_M -> { s.duration = (int) Math.max(20, s.duration - 20); openSettings(p); }
            case S_DUR_P -> { s.duration = (int) Math.min(plugin.fxConfig().maxDuration, s.duration + 20); openSettings(p); }
            case S_SPD_M -> { s.speed = Math.max(0.25, Math.round((s.speed - 0.25) * 100) / 100.0); openSettings(p); }
            case S_SPD_P -> { s.speed = Math.min(plugin.fxConfig().maxSpeed, Math.round((s.speed + 0.25) * 100) / 100.0); openSettings(p); }
            case S_DEN_M -> { s.density = Math.max(0.25, Math.round((s.density - 0.25) * 100) / 100.0); openSettings(p); }
            case S_DEN_P -> { s.density = Math.min(3.0, Math.round((s.density + 0.25) * 100) / 100.0); openSettings(p); }
            case S_MODE -> { s.ahead = !s.ahead; openSettings(p); }
            case S_LAUNCH -> {
                if (info == null) { openMainMenu(p); return; }
                launch(p, info, s, false);
            }
            case S_PREVIEW -> {
                if (info == null || !plugin.fxConfig().previewEnabled) { openSettings(p); return; }
                launch(p, info, s, true);
            }
            default -> { /* decorative or empty slot: nothing */ }
        }
    }

    public void launch(Player p, AnimationRegistry.FxInfo info, Session s, boolean preview) {
        double scale = Math.min(s.scale, plugin.fxConfig().maxScale);
        int duration = preview ? Math.min(60, s.duration) : s.duration;
        EffectSettings set = EffectSettings.safe(scale, duration, s.speed, s.density, s.ahead);
        Location origin = p.getLocation().clone().add(0, 1.0, 0);
        if (set.ahead) {
            Vector dir = p.getLocation().getDirection().setY(0);
            if (dir.lengthSquared() < 0.001) dir = new Vector(0, 0, 1);
            dir = dir.normalize().multiply(3.0);
            origin = origin.add(dir);
        }
        Emitter out = new Emitter(origin.getWorld(), plugin.budget());
        Fx fx = info.factory().create(origin, set, out);
        if (plugin.manager().start(p, fx, info.id(), set)) {
            p.sendMessage(Component.text(preview ? "Предпросмотр: " + info.name() : "Запущен: " + info.name()));
            p.closeInventory();
        } else {
            p.sendMessage(Component.text("Достигнут лимит активных эффектов — попробуйте позже."));
        }
    }

    private static double inc(double v, double step, double max) { return Math.min(max, Math.round((v + step) * 100) / 100.0); }
    private static double dec(double v, double step) { return Math.max(0.25, Math.round((v - step) * 100) / 100.0); }
}
