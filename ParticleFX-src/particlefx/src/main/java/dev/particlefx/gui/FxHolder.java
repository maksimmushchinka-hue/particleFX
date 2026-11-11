package dev.particlefx.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Unique holder: marks inventories that belong to ParticleFX GUI. */
public final class FxHolder implements InventoryHolder {
    public enum Kind { MAIN, SETTINGS }
    private final Kind kind;
    private Inventory inv;

    public FxHolder(Kind kind) { this.kind = kind; }
    public Kind kind() { return kind; }
    @Override public Inventory getInventory() { return inv; }
    public void bind(Inventory inv) { this.inv = inv; }
}
