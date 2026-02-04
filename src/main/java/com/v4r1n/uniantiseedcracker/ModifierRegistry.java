package com.v4r1n.uniantiseedcracker;

import com.v4r1n.uniantiseedcracker.listeners.StructureModifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for managing structure modifiers.
 * Implements the Registry pattern for centralized modifier management.
 */
public class ModifierRegistry {

    private final UniAntiSeedCracker plugin;
    private final List<StructureModifier> modifiers = new ArrayList<>();

    public ModifierRegistry(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a modifier to be managed by this registry.
     *
     * @param modifier The modifier to register
     */
    public void register(StructureModifier modifier) {
        modifiers.add(modifier);
    }

    /**
     * Register all enabled modifiers with the server's event system.
     *
     * @return The number of modifiers that were enabled
     */
    public int registerAllEnabled() {
        int enabledCount = 0;
        for (StructureModifier modifier : modifiers) {
            if (modifier.isEnabled()) {
                plugin.getServer().getPluginManager().registerEvents(modifier, plugin);
                enabledCount++;
                debug("Registered modifier: " + modifier.getDisplayName());
            }
        }
        return enabledCount;
    }

    /**
     * Unregister all modifiers from the event system.
     */
    public void unregisterAll() {
        for (StructureModifier modifier : modifiers) {
            modifier.unregister();
        }
        debug("Unregistered all " + modifiers.size() + " modifiers");
    }

    /**
     * Get the count of currently enabled modifiers.
     *
     * @return The number of enabled modifiers
     */
    public int getEnabledCount() {
        int count = 0;
        for (StructureModifier modifier : modifiers) {
            if (modifier.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the total number of registered modifiers.
     *
     * @return The total modifier count
     */
    public int getTotalCount() {
        return modifiers.size();
    }

    /**
     * Get an unmodifiable list of all registered modifiers.
     *
     * @return List of modifiers
     */
    public List<StructureModifier> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    /**
     * Log debug message if debug mode is enabled.
     */
    private void debug(String message) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
}
