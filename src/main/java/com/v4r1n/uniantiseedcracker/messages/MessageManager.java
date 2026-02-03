package com.v4r1n.uniantiseedcracker.messages;

import com.v4r1n.uniantiseedcracker.UniAntiSeedCracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.logging.Level;

/**
 * Message manager with MiniMessage + Legacy color code support
 */
public class MessageManager {

    private final UniAntiSeedCracker plugin;
    private final MiniMessage miniMessage;

    private File messagesFile;
    private FileConfiguration messages;
    private FileConfiguration defaultMessages;

    public MessageManager(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Initialize the message manager
     */
    public void initialize() {
        loadDefaultMessages();

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            saveDefaultMessages();
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Check version and reorganize
        checkAndMigrate();
        reorganize();
    }

    /**
     * Reload messages from file
     */
    public void reload() {
        if (messagesFile != null && messagesFile.exists()) {
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            reorganize();
        }
    }

    /**
     * Load default messages from resources
     */
    private void loadDefaultMessages() {
        try (InputStream stream = plugin.getResource("messages.yml")) {
            if (stream != null) {
                defaultMessages = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
                );
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load default messages", e);
        }
    }

    /**
     * Save default messages file
     */
    private void saveDefaultMessages() {
        try (InputStream stream = plugin.getResource("messages.yml")) {
            if (stream != null) {
                Files.copy(stream, messagesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Created default messages.yml");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create messages.yml", e);
        }
    }

    /**
     * Check and migrate messages version
     */
    private void checkAndMigrate() {
        int currentVersion = 1;
        int fileVersion = messages.getInt("messages-version", 0);

        if (fileVersion < currentVersion) {
            plugin.getLogger().info("Migrating messages.yml to version " + currentVersion);
            messages.set("messages-version", currentVersion);
            save();
        }
    }

    /**
     * Add missing keys from default messages
     */
    private void reorganize() {
        if (defaultMessages == null) {
            return;
        }

        boolean modified = false;

        for (String key : defaultMessages.getKeys(true)) {
            if (!messages.contains(key) && !defaultMessages.isConfigurationSection(key)) {
                messages.set(key, defaultMessages.get(key));
                plugin.getLogger().info("Added missing message key: " + key);
                modified = true;
            }
        }

        if (modified) {
            save();
        }
    }

    /**
     * Save messages file
     */
    private void save() {
        try {
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save messages.yml", e);
        }
    }

    /**
     * Get raw message string from config
     * @param path Message path
     * @return Raw message string
     */
    public String getRaw(String path) {
        return messages.getString(path, "Missing: " + path);
    }

    /**
     * Get raw message with placeholders replaced
     * @param path Message path
     * @param placeholders Map of placeholder -> value
     * @return Raw message with replacements
     */
    public String getRaw(String path, Map<String, String> placeholders) {
        String message = getRaw(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    /**
     * Parse message with MiniMessage + Legacy support
     * @param rawMessage Raw message string
     * @return Parsed Component
     */
    public Component parse(String rawMessage) {
        // First convert legacy codes (&a, &b, etc.) to MiniMessage format
        String converted = convertLegacyToMiniMessage(rawMessage);
        // Then parse with MiniMessage
        return miniMessage.deserialize(converted);
    }

    /**
     * Convert legacy color codes to MiniMessage tags
     * @param input Input string with legacy codes
     * @return Converted string
     */
    private String convertLegacyToMiniMessage(String input) {
        // Convert & codes to section symbol first for processing
        String processed = input.replace("&", "§");

        // Convert section codes to MiniMessage
        processed = processed
            .replace("§0", "<black>")
            .replace("§1", "<dark_blue>")
            .replace("§2", "<dark_green>")
            .replace("§3", "<dark_aqua>")
            .replace("§4", "<dark_red>")
            .replace("§5", "<dark_purple>")
            .replace("§6", "<gold>")
            .replace("§7", "<gray>")
            .replace("§8", "<dark_gray>")
            .replace("§9", "<blue>")
            .replace("§a", "<green>")
            .replace("§b", "<aqua>")
            .replace("§c", "<red>")
            .replace("§d", "<light_purple>")
            .replace("§e", "<yellow>")
            .replace("§f", "<white>")
            .replace("§k", "<obfuscated>")
            .replace("§l", "<bold>")
            .replace("§m", "<strikethrough>")
            .replace("§n", "<underlined>")
            .replace("§o", "<italic>")
            .replace("§r", "<reset>");

        return processed;
    }

    /**
     * Get Component from message path
     * @param path Message path
     * @return Parsed Component
     */
    public Component get(String path) {
        return parse(getRaw(path));
    }

    /**
     * Get Component from message path with placeholders
     * @param path Message path
     * @param placeholders Map of placeholder -> value
     * @return Parsed Component with replacements
     */
    public Component get(String path, Map<String, String> placeholders) {
        return parse(getRaw(path, placeholders));
    }

    /**
     * Get Component with prefix prepended
     * @param path Message path
     * @return Component with prefix
     */
    public Component getWithPrefix(String path) {
        Component prefix = get("prefix");
        Component message = get(path);
        return prefix.append(message);
    }

    /**
     * Get Component with prefix and placeholders
     * @param path Message path
     * @param placeholders Map of placeholder -> value
     * @return Component with prefix and replacements
     */
    public Component getWithPrefix(String path, Map<String, String> placeholders) {
        Component prefix = get("prefix");
        Component message = get(path, placeholders);
        return prefix.append(message);
    }

    /**
     * Send message to command sender
     * @param sender Command sender
     * @param path Message path
     */
    public void send(CommandSender sender, String path) {
        if (sender instanceof Player) {
            sender.sendMessage(get(path));
        } else {
            // Console - use plain text from console section if available
            String consolePath = path.replace("commands.", "console.");
            if (messages.contains(consolePath)) {
                sender.sendMessage(getRaw(consolePath));
            } else {
                sender.sendMessage(stripTags(getRaw(path)));
            }
        }
    }

    /**
     * Send message with placeholders
     * @param sender Command sender
     * @param path Message path
     * @param placeholders Placeholders map
     */
    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        if (sender instanceof Player) {
            sender.sendMessage(get(path, placeholders));
        } else {
            String consolePath = path.replace("commands.", "console.");
            if (messages.contains(consolePath)) {
                sender.sendMessage(getRaw(consolePath, placeholders));
            } else {
                sender.sendMessage(stripTags(getRaw(path, placeholders)));
            }
        }
    }

    /**
     * Send message with prefix
     * @param sender Command sender
     * @param path Message path
     */
    public void sendWithPrefix(CommandSender sender, String path) {
        if (sender instanceof Player) {
            sender.sendMessage(getWithPrefix(path));
        } else {
            String prefix = stripTags(getRaw("prefix"));
            String consolePath = path.replace("commands.", "console.");
            if (messages.contains(consolePath)) {
                sender.sendMessage(prefix + getRaw(consolePath));
            } else {
                sender.sendMessage(prefix + stripTags(getRaw(path)));
            }
        }
    }

    /**
     * Send message with prefix and placeholders
     * @param sender Command sender
     * @param path Message path
     * @param placeholders Placeholders map
     */
    public void sendWithPrefix(CommandSender sender, String path, Map<String, String> placeholders) {
        if (sender instanceof Player) {
            sender.sendMessage(getWithPrefix(path, placeholders));
        } else {
            String prefix = stripTags(getRaw("prefix"));
            String consolePath = path.replace("commands.", "console.");
            if (messages.contains(consolePath)) {
                sender.sendMessage(prefix + getRaw(consolePath, placeholders));
            } else {
                sender.sendMessage(prefix + stripTags(getRaw(path, placeholders)));
            }
        }
    }

    /**
     * Strip MiniMessage tags from string
     * @param input Input string
     * @return String without tags
     */
    private String stripTags(String input) {
        return miniMessage.stripTags(input);
    }

    /**
     * Get the messages configuration
     * @return FileConfiguration
     */
    public FileConfiguration getConfig() {
        return messages;
    }
}
