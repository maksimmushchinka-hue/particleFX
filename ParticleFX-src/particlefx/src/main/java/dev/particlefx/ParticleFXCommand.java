package dev.particlefx;

import dev.particlefx.core.AnimationRegistry;
import dev.particlefx.core.EffectSettings;
import dev.particlefx.core.Emitter;
import dev.particlefx.core.Fx;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ParticleFXCommand implements TabExecutor {
    private final ParticleFXPlugin plugin;

    public ParticleFXCommand(ParticleFXPlugin plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // console: only play <id> <player> makes sense
            if (args.length >= 3 && args[0].equalsIgnoreCase("play")) {
                Player target = plugin.getServer().getPlayerExact(args[2]);
                if (target == null) { sender.sendMessage("Игрок не найден: " + args[2]); return true; }
                return playFor(sender, target, args[1]);
            }
            sender.sendMessage("Из консоли: /particlefx play <эффект> <игрок>");
            return true;
        }
        Player p = (Player) sender;
        String sub = args.length == 0 ? "menu" : args[0].toLowerCase();
        switch (sub) {
            case "menu", "m" -> { if (check(p, "particlefx.use")) plugin.gui().openMainMenu(p); }
            case "list", "l" -> {
                if (!check(p, "particlefx.use")) return true;
                StringBuilder sb = new StringBuilder("ParticleFX (" + plugin.registry().size() + "): ");
                for (AnimationRegistry.FxInfo i : plugin.registry().all()) sb.append(i.id()).append(", ");
                p.sendMessage(net.kyori.adventure.text.Component.text(sb.substring(0, sb.length() - 2)));
            }
            case "play", "p" -> {
                if (!check(p, "particlefx.play")) return true;
                if (args.length < 2) { p.sendMessage(net.kyori.adventure.text.Component.text("Использование: /particlefx play <эффект>")); return true; }
                playFor(p, p, args[1]);
            }
            case "stop", "s" -> p.sendMessage(net.kyori.adventure.text.Component.text("Остановлено эффектов: " + plugin.manager().stopAll(p)));
            case "reload", "r" -> {
                if (!check(p, "particlefx.admin")) return true;
                plugin.reloadAll();
                p.sendMessage(net.kyori.adventure.text.Component.text("ParticleFX: конфигурация перезагружена."));
            }
            default -> p.sendMessage(net.kyori.adventure.text.Component.text("Подкоманды: menu, play, stop, list, reload"));
        }
        return true;
    }

    private boolean playFor(CommandSender reportTo, Player target, String id) {
        AnimationRegistry.FxInfo info = plugin.registry().get(id);
        if (info == null) {
            reportTo.sendMessage(net.kyori.adventure.text.Component.text("Неизвестный эффект: " + id + ". См. /particlefx list"));
            return true;
        }
        if (!plugin.fxConfig().isEnabled(id)) {
            reportTo.sendMessage(net.kyori.adventure.text.Component.text("Этот эффект отключён в конфигурации сервера."));
            return true;
        }
        EffectSettings def = EffectSettings.safe(1.0, 160, 1.0, 1.0, false);
        Location origin = target.getLocation().clone().add(0, 1.0, 0);
        Emitter out = new Emitter(origin.getWorld(), plugin.budget());
        Fx fx = info.factory().create(origin, def, out);
        boolean ok = plugin.manager().start(target, fx, id, def);
        reportTo.sendMessage(net.kyori.adventure.text.Component.text(
                ok ? "Запущен эффект: " + info.name() : "Достигнут лимит активных эффектов."));
        return true;
    }

    private boolean check(Player p, String perm) {
        if (p.hasPermission(perm)) return true;
        p.sendMessage(net.kyori.adventure.text.Component.text("Нет прав: " + perm));
        return false;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("menu", "play", "stop", "list", "reload"))
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("play")) {
            for (AnimationRegistry.FxInfo i : plugin.registry().all())
                if (i.id().startsWith(args[1].toLowerCase())) out.add(i.id());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("play")) {
            for (Player pl : plugin.getServer().getOnlinePlayers())
                if (pl.getName().toLowerCase().startsWith(args[2].toLowerCase())) out.add(pl.getName());
        }
        return out;
    }
}
