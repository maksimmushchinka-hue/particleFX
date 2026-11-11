package dev.particlefx.core;

/** Global per-tick particle budget shared by all running effects. */
public final class ParticleBudget {
    private final int maxPerTick;
    private int used;

    public ParticleBudget(int maxPerTick) { this.maxPerTick = maxPerTick; }

    public void resetTick() { used = 0; }
    public boolean canSpend(int n) { return used + n <= maxPerTick; }
    public void spend(int n) { used += n; }
    public boolean exhausted() { return used >= maxPerTick; }
}
