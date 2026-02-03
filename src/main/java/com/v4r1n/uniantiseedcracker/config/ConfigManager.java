package com.v4r1n.uniantiseedcracker.config;

import com.v4r1n.uniantiseedcracker.UniAntiSeedCracker;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class ConfigManager {

    private final UniAntiSeedCracker plugin;
    private final int CURRENT_CONFIG_VERSION = 1;

    private FileConfiguration defaultConfig;
    private final Map<String, ConfigValidator> validators = new HashMap<>();

    public ConfigManager(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        loadDefaultConfig();
        registerDefaultValidators();
    }

    /**
     * CONFIG_DEFAULT_LOADER - Load default config from resource
     */
    private void loadDefaultConfig() {
        try (InputStream stream = plugin.getResource("config.yml")) {
            if (stream != null) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                defaultConfig = YamlConfiguration.loadConfiguration(reader);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load default config from resource", e);
        }
    }

    /**
     * Initialize and validate the config file
     */
    public void initialize() {
        plugin.saveDefaultConfig();

        // CONFIG_VERSION_CONTROL - Check and upgrade config version
        checkAndUpgradeVersion();

        // CONFIG_AUTO_REORGANIZATION - Add missing keys from default
        reorganizeConfig();

        // CONFIG_SECTION_VALIDATOR - Validate structure and data types
        validateConfig();
    }

    /**
     * CONFIG_VERSION_CONTROL - Check and upgrade config version automatically
     */
    private void checkAndUpgradeVersion() {
        FileConfiguration config = plugin.getConfig();
        int configVersion = config.getInt("config-version", 0);

        if (configVersion < CURRENT_CONFIG_VERSION) {
            plugin.getLogger().info("Upgrading config from version " + configVersion + " to " + CURRENT_CONFIG_VERSION);

            // Perform version migrations
            for (int version = configVersion; version < CURRENT_CONFIG_VERSION; version++) {
                migrateConfig(version, version + 1);
            }

            config.set("config-version", CURRENT_CONFIG_VERSION);
            plugin.saveConfig();
            plugin.getLogger().info("Config upgrade complete!");
        }
    }

    /**
     * Migrate config from one version to another
     */
    private void migrateConfig(int fromVersion, int toVersion) {
        FileConfiguration config = plugin.getConfig();

        switch (fromVersion) {
            case 0:
                // Migration from version 0 (no version) to version 1
                // Add config-version key
                if (!config.contains("config-version")) {
                    plugin.getLogger().info("Adding config-version key");
                }
                break;
            // Add more migrations here as versions increase
            // case 1:
            //     // Migration from version 1 to version 2
            //     break;
        }
    }

    /**
     * CONFIG_AUTO_REORGANIZATION - Refresh config automatically when new keys are added
     */
    private void reorganizeConfig() {
        if (defaultConfig == null) {
            plugin.getLogger().warning("Default config not loaded, skipping reorganization");
            return;
        }

        FileConfiguration config = plugin.getConfig();
        boolean modified = false;

        // Add missing keys from default config
        for (String key : getAllKeys(defaultConfig)) {
            if (!config.contains(key)) {
                Object defaultValue = defaultConfig.get(key);
                config.set(key, defaultValue);
                plugin.getLogger().info("Added missing config key: " + key);
                modified = true;
            }
        }

        // Remove obsolete keys (optional - commented out for safety)
        // for (String key : getAllKeys(config)) {
        //     if (!defaultConfig.contains(key) && !key.equals("config-version")) {
        //         config.set(key, null);
        //         plugin.getLogger().info("Removed obsolete config key: " + key);
        //         modified = true;
        //     }
        // }

        if (modified) {
            plugin.saveConfig();
            plugin.getLogger().info("Config reorganization complete - new keys added");
        }
    }

    /**
     * Get all keys including nested keys
     */
    private Set<String> getAllKeys(FileConfiguration config) {
        Set<String> keys = new LinkedHashSet<>();
        collectKeys(config, "", keys);
        return keys;
    }

    private void collectKeys(ConfigurationSection section, String prefix, Set<String> keys) {
        for (String key : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (section.isConfigurationSection(key)) {
                collectKeys(section.getConfigurationSection(key), fullKey, keys);
            } else {
                keys.add(fullKey);
            }
        }
    }

    /**
     * CONFIG_SECTION_VALIDATOR - Validate structure and data types in config
     */
    private void validateConfig() {
        FileConfiguration config = plugin.getConfig();
        List<String> errors = new ArrayList<>();

        for (Map.Entry<String, ConfigValidator> entry : validators.entrySet()) {
            String key = entry.getKey();
            ConfigValidator validator = entry.getValue();

            if (!config.contains(key)) {
                errors.add("Missing required key: " + key);
                continue;
            }

            Object value = config.get(key);
            ValidationResult result = validator.validate(value);

            if (!result.isValid()) {
                errors.add("Invalid value for '" + key + "': " + result.getMessage());

                // Auto-fix with default value if available
                if (defaultConfig != null && defaultConfig.contains(key)) {
                    Object defaultValue = defaultConfig.get(key);
                    config.set(key, defaultValue);
                    plugin.getLogger().warning("Reset '" + key + "' to default value: " + defaultValue);
                }
            }
        }

        if (!errors.isEmpty()) {
            plugin.getLogger().warning("Config validation found " + errors.size() + " issue(s):");
            for (String error : errors) {
                plugin.getLogger().warning("  - " + error);
            }
            plugin.saveConfig();
        } else {
            plugin.getLogger().info("Config validation passed successfully");
        }
    }

    /**
     * Register default validators for known config keys
     */
    private void registerDefaultValidators() {
        // Boolean validators
        registerValidator("randomize_hashed_seed.login", ConfigValidators.BOOLEAN);
        registerValidator("randomize_hashed_seed.respawn", ConfigValidators.BOOLEAN);
        registerValidator("modifiers.end_spikes.enabled", ConfigValidators.BOOLEAN);
        registerValidator("modifiers.end_cities.enabled", ConfigValidators.BOOLEAN);

        // String validators
        registerValidator("modifiers.end_spikes.mode",
            ConfigValidators.stringInList("swap", "move"));

        // List validators
        registerValidator("modifiers.end_spikes.worlds", ConfigValidators.STRING_LIST);
        registerValidator("modifiers.end_cities.worlds", ConfigValidators.STRING_LIST);
    }

    /**
     * Register a custom validator for a config key
     */
    public void registerValidator(String key, ConfigValidator validator) {
        validators.put(key, validator);
    }

    /**
     * Get current config version
     */
    public int getCurrentConfigVersion() {
        return CURRENT_CONFIG_VERSION;
    }

    /**
     * Get loaded config version
     */
    public int getLoadedConfigVersion() {
        return plugin.getConfig().getInt("config-version", 0);
    }

    /**
     * Reload and revalidate config
     */
    public void reload() {
        plugin.reloadConfig();
        loadDefaultConfig();
        reorganizeConfig();
        validateConfig();
    }

    /**
     * Get default config
     */
    public FileConfiguration getDefaultConfig() {
        return defaultConfig;
    }
}
