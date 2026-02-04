package com.v4r1n.uniantiseedcracker.listeners;

import org.bukkit.event.Listener;

/**
 * Interface for structure modifiers that can be registered/unregistered dynamically.
 * Implements the Strategy pattern for different structure modification behaviors.
 */
public interface StructureModifier extends Listener {

    /**
     * Get the config path for this modifier (e.g., "modifiers.buried_treasure").
     *
     * @return The configuration path
     */
    String getConfigPath();

    /**
     * Unregister this modifier from all event handlers.
     */
    void unregister();

    /**
     * Check if this modifier is enabled in the configuration.
     *
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Get a display name for this modifier (used in logging).
     *
     * @return The display name
     */
    default String getDisplayName() {
        return getClass().getSimpleName();
    }
}
