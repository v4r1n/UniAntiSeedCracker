package com.v4r1n.uniantiseedcracker.commands;

import com.v4r1n.uniantiseedcracker.UniAntiSeedCracker;
import com.v4r1n.uniantiseedcracker.messages.MessageManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class UniAntiSeedCrackerCommand implements CommandExecutor, TabCompleter {

    private final UniAntiSeedCracker plugin;
    private final MessageManager messages;

    public UniAntiSeedCrackerCommand(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("uniantiseedcracker.admin")) {
            messages.sendWithPrefix(sender, "commands.no-permission");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender, label);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "version":
                handleVersion(sender);
                break;
            case "debug":
                handleDebug(sender);
                break;
            case "help":
                showHelp(sender, label);
                break;
            default:
                messages.sendWithPrefix(sender, "commands.unknown-subcommand",
                    Map.of("label", label));
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        messages.reload();
        plugin.reload(false);
        messages.sendWithPrefix(sender, "commands.reload.success");
    }

    private void handleVersion(CommandSender sender) {
        String version = plugin.getPluginMeta().getVersion();
        String authors = String.join(", ", plugin.getPluginMeta().getAuthors());
        String apiVersion = plugin.getPluginMeta().getAPIVersion();
        String description = plugin.getPluginMeta().getDescription();
        String credit = "Based on AntiSeedCracker by Gadse";
        int configVersion = plugin.getConfigManager().getCurrentConfigVersion();

        if (sender instanceof Player) {
            // Send formatted version to player (hardcoded, not customizable)
            MiniMessage mm = MiniMessage.miniMessage();
            sender.sendMessage(mm.deserialize("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>"));
            sender.sendMessage(mm.deserialize("<gradient:#00D4FF:#00FF88><bold>UniAntiSeedCracker</bold></gradient>"));
            sender.sendMessage(mm.deserialize("<gray>Version:</gray> <aqua>" + version + "</aqua>"));
            sender.sendMessage(mm.deserialize("<gray>Authors:</gray> <yellow>" + authors + "</yellow>"));
            sender.sendMessage(mm.deserialize("<gray>API Version:</gray> <aqua>" + (apiVersion != null ? apiVersion : "1.21") + "</aqua>"));
            sender.sendMessage(mm.deserialize("<gray>Description:</gray> <white>" + (description != null ? description : "N/A") + "</white>"));
            sender.sendMessage(mm.deserialize("<gray>Credit:</gray> <white>" + credit + "</white>"));
            sender.sendMessage(mm.deserialize("<gray>Config Version:</gray> <aqua>" + configVersion + "</aqua>"));
            sender.sendMessage(mm.deserialize("<gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gray>"));
        } else {
            // Send to console (hardcoded, not customizable) 
            plugin.getLogger().info("=========================================");
            plugin.getLogger().info("UniAntiSeedCracker Information");
            plugin.getLogger().info("Version: " + version);
            plugin.getLogger().info("Authors: " + authors);
            plugin.getLogger().info("API Version: " + (apiVersion != null ? apiVersion : "1.21"));
            plugin.getLogger().info("Description: " + (description != null ? description : "N/A"));
            plugin.getLogger().info("Credit: " + credit);
            plugin.getLogger().info("Config Version: " + configVersion);
            plugin.getLogger().info("=========================================");
        }
    }

    private void handleDebug(CommandSender sender) {
        // Gather debug information
        int worldsCount = plugin.getServer().getWorlds().size();
        boolean loginEnabled = plugin.getConfig().getBoolean("randomize_hashed_seed.login", true);
        boolean respawnEnabled = plugin.getConfig().getBoolean("randomize_hashed_seed.respawn", true);
        boolean endSpikesEnabled = plugin.getConfig().getBoolean("modifiers.end_spikes.enabled", false);
        String endSpikesMode = plugin.getConfig().getString("modifiers.end_spikes.mode", "move");
        List<String> endSpikesWorlds = plugin.getConfig().getStringList("modifiers.end_spikes.worlds");
        boolean endCitiesEnabled = plugin.getConfig().getBoolean("modifiers.end_cities.enabled", false);
        List<String> endCitiesWorlds = plugin.getConfig().getStringList("modifiers.end_cities.worlds");
        boolean buriedTreasureEnabled = plugin.getConfig().getBoolean("modifiers.buried_treasure.enabled", false);
        List<String> buriedTreasureWorlds = plugin.getConfig().getStringList("modifiers.buried_treasure.worlds");
        boolean desertTempleEnabled = plugin.getConfig().getBoolean("modifiers.desert_temple.enabled", false);
        List<String> desertTempleWorlds = plugin.getConfig().getStringList("modifiers.desert_temple.worlds");
        boolean jungleTempleEnabled = plugin.getConfig().getBoolean("modifiers.jungle_temple.enabled", false);
        List<String> jungleTempleWorlds = plugin.getConfig().getStringList("modifiers.jungle_temple.worlds");
        boolean shipwreckEnabled = plugin.getConfig().getBoolean("modifiers.shipwreck.enabled", false);
        List<String> shipwreckWorlds = plugin.getConfig().getStringList("modifiers.shipwreck.worlds");
        boolean witchHutEnabled = plugin.getConfig().getBoolean("modifiers.witch_hut.enabled", false);
        List<String> witchHutWorlds = plugin.getConfig().getStringList("modifiers.witch_hut.worlds");
        boolean oceanMonumentEnabled = plugin.getConfig().getBoolean("modifiers.ocean_monument.enabled", false);
        List<String> oceanMonumentWorlds = plugin.getConfig().getStringList("modifiers.ocean_monument.worlds");
        boolean pillagerOutpostEnabled = plugin.getConfig().getBoolean("modifiers.pillager_outpost.enabled", false);
        List<String> pillagerOutpostWorlds = plugin.getConfig().getStringList("modifiers.pillager_outpost.worlds");
        boolean iglooEnabled = plugin.getConfig().getBoolean("modifiers.igloo.enabled", false);
        List<String> iglooWorlds = plugin.getConfig().getStringList("modifiers.igloo.worlds");

        if (sender instanceof Player) {
            // Send formatted debug to player
            messages.send(sender, "commands.debug.header");
            messages.send(sender, "commands.debug.title");

            // General status
            messages.send(sender, "commands.debug.general.title");
            messages.send(sender, "commands.debug.general.plugin-enabled");
            messages.send(sender, "commands.debug.general.protocol-lib");
            messages.send(sender, "commands.debug.general.worlds-count",
                Map.of("count", String.valueOf(worldsCount)));

            // Hashed seed module
            messages.send(sender, "commands.debug.hashed-seed.title");
            String loginStatus = loginEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            String respawnStatus = respawnEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.hashed-seed.login-enabled",
                Map.of("status", loginStatus));
            messages.send(sender, "commands.debug.hashed-seed.respawn-enabled",
                Map.of("status", respawnStatus));

            // End spike modifier
            messages.send(sender, "commands.debug.end-spikes.title");
            String endSpikesStatus = endSpikesEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.end-spikes.enabled",
                Map.of("status", endSpikesStatus));
            messages.send(sender, "commands.debug.end-spikes.mode",
                Map.of("mode", endSpikesMode));
            messages.send(sender, "commands.debug.end-spikes.worlds",
                Map.of("worlds", String.join(", ", endSpikesWorlds)));

            // End city modifier
            messages.send(sender, "commands.debug.end-cities.title");
            String endCitiesStatus = endCitiesEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.end-cities.enabled",
                Map.of("status", endCitiesStatus));
            messages.send(sender, "commands.debug.end-cities.worlds",
                Map.of("worlds", String.join(", ", endCitiesWorlds)));

            // Buried treasure modifier
            messages.send(sender, "commands.debug.buried-treasure.title");
            String buriedTreasureStatus = buriedTreasureEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.buried-treasure.enabled",
                Map.of("status", buriedTreasureStatus));
            messages.send(sender, "commands.debug.buried-treasure.worlds",
                Map.of("worlds", String.join(", ", buriedTreasureWorlds)));

            // Desert temple modifier
            messages.send(sender, "commands.debug.desert-temple.title");
            String desertTempleStatus = desertTempleEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.desert-temple.enabled",
                Map.of("status", desertTempleStatus));
            messages.send(sender, "commands.debug.desert-temple.worlds",
                Map.of("worlds", String.join(", ", desertTempleWorlds)));

            // Jungle temple modifier
            messages.send(sender, "commands.debug.jungle-temple.title");
            String jungleTempleStatus = jungleTempleEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.jungle-temple.enabled",
                Map.of("status", jungleTempleStatus));
            messages.send(sender, "commands.debug.jungle-temple.worlds",
                Map.of("worlds", String.join(", ", jungleTempleWorlds)));

            // Shipwreck modifier
            messages.send(sender, "commands.debug.shipwreck.title");
            String shipwreckStatus = shipwreckEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.shipwreck.enabled",
                Map.of("status", shipwreckStatus));
            messages.send(sender, "commands.debug.shipwreck.worlds",
                Map.of("worlds", String.join(", ", shipwreckWorlds)));

            // Witch hut modifier
            messages.send(sender, "commands.debug.witch-hut.title");
            String witchHutStatus = witchHutEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.witch-hut.enabled",
                Map.of("status", witchHutStatus));
            messages.send(sender, "commands.debug.witch-hut.worlds",
                Map.of("worlds", String.join(", ", witchHutWorlds)));

            // Ocean monument modifier
            messages.send(sender, "commands.debug.ocean-monument.title");
            String oceanMonumentStatus = oceanMonumentEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.ocean-monument.enabled",
                Map.of("status", oceanMonumentStatus));
            messages.send(sender, "commands.debug.ocean-monument.worlds",
                Map.of("worlds", String.join(", ", oceanMonumentWorlds)));

            // Pillager outpost modifier
            messages.send(sender, "commands.debug.pillager-outpost.title");
            String pillagerOutpostStatus = pillagerOutpostEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.pillager-outpost.enabled",
                Map.of("status", pillagerOutpostStatus));
            messages.send(sender, "commands.debug.pillager-outpost.worlds",
                Map.of("worlds", String.join(", ", pillagerOutpostWorlds)));

            // Igloo modifier
            messages.send(sender, "commands.debug.igloo.title");
            String iglooStatus = iglooEnabled ?
                messages.getRaw("commands.debug.hashed-seed.status-enabled") :
                messages.getRaw("commands.debug.hashed-seed.status-disabled");
            messages.send(sender, "commands.debug.igloo.enabled",
                Map.of("status", iglooStatus));
            messages.send(sender, "commands.debug.igloo.worlds",
                Map.of("worlds", String.join(", ", iglooWorlds)));

            messages.send(sender, "commands.debug.footer");
        } else {
            // Send to console (hardcoded, not customizable)
            plugin.getLogger().info("=========================================");
            plugin.getLogger().info("UniAntiSeedCracker Debug Information");

            // General status
            plugin.getLogger().info("▸ General Status");
            plugin.getLogger().info("  Plugin Enabled: Yes");
            plugin.getLogger().info("  ProtocolLib: Loaded");
            plugin.getLogger().info("  Worlds Loaded: " + worldsCount);

            // Hashed seed module
            plugin.getLogger().info("▸ Hashed Seed Randomizer");
            plugin.getLogger().info("  Login Packet: " + (loginEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Respawn Packet: " + (respawnEnabled ? "Enabled" : "Disabled"));

            // End spike modifier
            plugin.getLogger().info("▸ End Spike Modifier");
            plugin.getLogger().info("  Enabled: " + (endSpikesEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Mode: " + endSpikesMode);
            plugin.getLogger().info("  Worlds: " + String.join(", ", endSpikesWorlds));

            // End city modifier
            plugin.getLogger().info("▸ End City Modifier");
            plugin.getLogger().info("  Enabled: " + (endCitiesEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", endCitiesWorlds));

            // Buried treasure modifier
            plugin.getLogger().info("▸ Buried Treasure Modifier");
            plugin.getLogger().info("  Enabled: " + (buriedTreasureEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", buriedTreasureWorlds));

            // Desert temple modifier
            plugin.getLogger().info("▸ Desert Temple Modifier");
            plugin.getLogger().info("  Enabled: " + (desertTempleEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", desertTempleWorlds));

            // Jungle temple modifier
            plugin.getLogger().info("▸ Jungle Temple Modifier");
            plugin.getLogger().info("  Enabled: " + (jungleTempleEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", jungleTempleWorlds));

            // Shipwreck modifier
            plugin.getLogger().info("▸ Shipwreck Modifier");
            plugin.getLogger().info("  Enabled: " + (shipwreckEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", shipwreckWorlds));

            // Witch hut modifier
            plugin.getLogger().info("▸ Witch Hut Modifier");
            plugin.getLogger().info("  Enabled: " + (witchHutEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", witchHutWorlds));

            // Ocean monument modifier
            plugin.getLogger().info("▸ Ocean Monument Modifier");
            plugin.getLogger().info("  Enabled: " + (oceanMonumentEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", oceanMonumentWorlds));

            // Pillager outpost modifier
            plugin.getLogger().info("▸ Pillager Outpost Modifier");
            plugin.getLogger().info("  Enabled: " + (pillagerOutpostEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", pillagerOutpostWorlds));

            // Igloo modifier
            plugin.getLogger().info("▸ Igloo Modifier");
            plugin.getLogger().info("  Enabled: " + (iglooEnabled ? "Enabled" : "Disabled"));
            plugin.getLogger().info("  Worlds: " + String.join(", ", iglooWorlds));

            plugin.getLogger().info("=========================================");
        }
    }

    private void showHelp(CommandSender sender, String label) {
        Map<String, String> placeholders = Map.of("label", label);

        messages.send(sender, "commands.help.header");
        messages.send(sender, "commands.help.title");
        messages.send(sender, "commands.help.reload", placeholders);
        messages.send(sender, "commands.help.version", placeholders);
        messages.send(sender, "commands.help.debug", placeholders);
        messages.send(sender, "commands.help.footer");
    }

    private final Set<String> subCommands = Set.of("reload", "version", "debug", "help");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("uniantiseedcracker.admin")) {
            return StringUtil.copyPartialMatches(args[0], subCommands, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
