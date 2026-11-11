package dev.particlefx.gui;

import dev.particlefx.ParticleFXPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

/** Click safety: only FxHolder inventories are handled; everything else is cancelled. */
public final class GuiListener implements Listener {
    private final ParticleFXPlugin plugin;

    public GuiListener(ParticleFXPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        InventoryHolder top = e.getView().getTopInventory().getHolder();
        if (!(top instanceof FxHolder)) return; // not our menu: vanilla behaviour
        e.setCancelled(true); // block all moves, shift-clicks, pickups inside menu
        if (e.getClickedInventory() != e.getView().getTopInventory()) return; // ignore bottom clicks
        if (e.getAction() != InventoryAction.PICKUP_ALL && e.getAction() != InventoryAction.NOTHING) return;
        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) return;
        plugin.gui().handleClick(p, e.getSlot());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        InventoryHolder top = e.getView().getTopInventory().getHolder();
        if (top instanceof FxHolder) e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.gui().forget(e.getPlayer());
        plugin.manager().stopAll(e.getPlayer());
    }
}
