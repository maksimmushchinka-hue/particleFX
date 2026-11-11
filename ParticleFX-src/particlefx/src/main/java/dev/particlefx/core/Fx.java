package dev.particlefx.core;

/** One animation implementation. tick() is called once per server tick from AnimationManager. */
public interface Fx {
    /** @return false when the animation is finished (task is then cancelled). */
    boolean tick(int t);
}
