package com.v4r1n.uniantiseedcracker;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import com.v4r1n.uniantiseedcracker.commands.UniAntiSeedCrackerCommand;
import com.v4r1n.uniantiseedcracker.config.ConfigManager;
import com.v4r1n.uniantiseedcracker.listeners.BuriedTreasureModifier;
import com.v4r1n.uniantiseedcracker.listeners.DesertTempleModifier;
import com.v4r1n.uniantiseedcracker.listeners.DragonRespawnSpikeModifier;
import com.v4r1n.uniantiseedcracker.listeners.EndCityModifier;
import com.v4r1n.uniantiseedcracker.listeners.IglooModifier;
import com.v4r1n.uniantiseedcracker.listeners.JungleTempleModifier;
import com.v4r1n.uniantiseedcracker.listeners.OceanMonumentModifier;
import com.v4r1n.uniantiseedcracker.listeners.PillagerOutpostModifier;
import com.v4r1n.uniantiseedcracker.listeners.ShipwreckModifier;
import com.v4r1n.uniantiseedcracker.listeners.WitchHutModifier;
import com.v4r1n.uniantiseedcracker.messages.MessageManager;
import com.v4r1n.uniantiseedcracker.packets.ServerLogin;
import com.v4r1n.uniantiseedcracker.packets.ServerRespawn;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class UniAntiSeedCracker extends JavaPlugin implements CommandExecutor {

    private ProtocolManager protocolManager;
    private NamespacedKey modifiedSpike;
    private ConfigManager configManager;
    private MessageManager messageManager;

    private DragonRespawnSpikeModifier dragonRespawnspikeModifier;
    private EndCityModifier endCityModifier;
    private BuriedTreasureModifier buriedTreasureModifier;
    private DesertTempleModifier desertTempleModifier;
    private JungleTempleModifier jungleTempleModifier;
    private ShipwreckModifier shipwreckModifier;
    private WitchHutModifier witchHutModifier;
    private OceanMonumentModifier oceanMonumentModifier;
    private PillagerOutpostModifier pillagerOutpostModifier;
    private IglooModifier iglooModifier;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Config folder can not be written. Check read/write permissions.");
        }

        // Initialize ConfigManager with auto-reorganization, version control, and validation
        configManager = new ConfigManager(this);
        configManager.initialize();

        // Initialize MessageManager with MiniMessage + Legacy support
        messageManager = new MessageManager(this);
        messageManager.initialize();

        protocolManager = ProtocolLibrary.getProtocolManager();
        modifiedSpike = new NamespacedKey(this, "modified-spike");
        dragonRespawnspikeModifier = new DragonRespawnSpikeModifier(this);
        endCityModifier = new EndCityModifier(this);
        buriedTreasureModifier = new BuriedTreasureModifier(this);
        desertTempleModifier = new DesertTempleModifier(this);
        jungleTempleModifier = new JungleTempleModifier(this);
        shipwreckModifier = new ShipwreckModifier(this);
        witchHutModifier = new WitchHutModifier(this);
        oceanMonumentModifier = new OceanMonumentModifier(this);
        pillagerOutpostModifier = new PillagerOutpostModifier(this);
        iglooModifier = new IglooModifier(this);

        PluginCommand command = getCommand("uniantiseedcracker");
        if (command == null) {
            getLogger().severe("The uniantiseedcracker command is missing from plugin.yml.");
        } else {
            UniAntiSeedCrackerCommand commandExecutor = new UniAntiSeedCrackerCommand(this);
            command.setExecutor(commandExecutor);
            command.setTabCompleter(commandExecutor);
        }

        reload(true);
    }

    public void reload(boolean isOnEnable) {
        if (!isOnEnable) {
            protocolManager.removePacketListeners(this);
            dragonRespawnspikeModifier.unregister();
            endCityModifier.unregister();
            buriedTreasureModifier.unregister();
            desertTempleModifier.unregister();
            jungleTempleModifier.unregister();
            shipwreckModifier.unregister();
            witchHutModifier.unregister();
            oceanMonumentModifier.unregister();
            pillagerOutpostModifier.unregister();
            iglooModifier.unregister();
            configManager.reload();
        }

        if (getConfig().getBoolean("randomize_hashed_seed.login", true)) {
            protocolManager.addPacketListener(new ServerLogin(this));
        }

        if (getConfig().getBoolean("randomize_hashed_seed.respawn", true)) {
            protocolManager.addPacketListener(new ServerRespawn(this));
        }

        if (getConfig().getBoolean("modifiers.end_spikes.enabled", false)) {
            getServer().getWorlds().forEach(world -> {
                if (!getConfig().getStringList("modifiers.end_spikes.worlds").contains(world.getName())) {
                    return;
                }

                if (world.getEnvironment() != World.Environment.THE_END) {
                    getLogger().warning("The world '" + world.getName() + "' is not an end dimension, it will be ignored.");
                    return;
                }

                modifyEndSpikes(world);
            });
            getServer().getPluginManager().registerEvents(dragonRespawnspikeModifier, this);
        }

        if (getConfig().getBoolean("modifiers.end_cities.enabled", false)) {
            getServer().getPluginManager().registerEvents(endCityModifier, this);
        }

        if (getConfig().getBoolean("modifiers.buried_treasure.enabled", false)) {
            getServer().getPluginManager().registerEvents(buriedTreasureModifier, this);
        }

        if (getConfig().getBoolean("modifiers.desert_temple.enabled", false)) {
            getServer().getPluginManager().registerEvents(desertTempleModifier, this);
        }

        if (getConfig().getBoolean("modifiers.jungle_temple.enabled", false)) {
            getServer().getPluginManager().registerEvents(jungleTempleModifier, this);
        }

        if (getConfig().getBoolean("modifiers.shipwreck.enabled", false)) {
            getServer().getPluginManager().registerEvents(shipwreckModifier, this);
        }

        if (getConfig().getBoolean("modifiers.witch_hut.enabled", false)) {
            getServer().getPluginManager().registerEvents(witchHutModifier, this);
        }

        if (getConfig().getBoolean("modifiers.ocean_monument.enabled", false)) {
            getServer().getPluginManager().registerEvents(oceanMonumentModifier, this);
        }

        if (getConfig().getBoolean("modifiers.pillager_outpost.enabled", false)) {
            getServer().getPluginManager().registerEvents(pillagerOutpostModifier, this);
        }

        if (getConfig().getBoolean("modifiers.igloo.enabled", false)) {
            getServer().getPluginManager().registerEvents(iglooModifier, this);
        }
    }

    @Override
    public void onDisable() {
        protocolManager.removePacketListeners(this);
        dragonRespawnspikeModifier.unregister();
        endCityModifier.unregister();
        buriedTreasureModifier.unregister();
        desertTempleModifier.unregister();
        jungleTempleModifier.unregister();
        shipwreckModifier.unregister();
        witchHutModifier.unregister();
        oceanMonumentModifier.unregister();
        pillagerOutpostModifier.unregister();
        iglooModifier.unregister();
    }

    public long randomizeHashedSeed(long hashedSeed) {
        int length = Long.toString(hashedSeed).length();
        if (length > 18) {
            length = 18;
        }
        long min = (long) Math.pow(10, length - 1);
        long max = (long) (Math.pow(10, length) - 1);
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    // https://minecraft.wiki/w/End_spike
    private final List<Integer> spikeHeights = List.of(76, 79, 82, 85, 88, 91, 94, 97, 100, 103);

    public void modifyEndSpikes(World world) {
        if (world.getEnvironment() != World.Environment.THE_END
                || world.getPersistentDataContainer().getOrDefault(modifiedSpike, PersistentDataType.BOOLEAN, false)) {
            return;
        }

        Map<Integer, Block> bedrockBlocksByHeight = getBedrockBlocksByHeight(world);
        if (getConfig().getString("modify_end_spikes.mode", "swap").equalsIgnoreCase("swap")) {
            swapEndSpikes(world, bedrockBlocksByHeight);
        } else {
            moveEndSpike(world, bedrockBlocksByHeight);
        }
    }

    private void swapEndSpikes(World world, Map<Integer, Block> bedrockBlocksByHeight) {
        int randomSpikeIndex = ThreadLocalRandom.current().nextInt(spikeHeights.size());
        int nextSpikeIndex = randomSpikeIndex + 1 > spikeHeights.size() - 1 ? 0 : randomSpikeIndex + 1;
        Block spike_one = bedrockBlocksByHeight.get(spikeHeights.get(randomSpikeIndex));
        Block spike_two = bedrockBlocksByHeight.get(spikeHeights.get(nextSpikeIndex));

        spike_one.setType(Material.OBSIDIAN);
        new Location(world, spike_one.getX(), spike_two.getY(), spike_one.getZ()).getBlock().setType(Material.BEDROCK);

        spike_two.setType(Material.OBSIDIAN);
        new Location(world, spike_two.getX(), spike_one.getY(), spike_two.getZ()).getBlock().setType(Material.BEDROCK);

        world.getPersistentDataContainer().set(modifiedSpike, PersistentDataType.BOOLEAN, true);
    }

    private void moveEndSpike(World world, Map<Integer, Block> bedrockBlocksByHeight) {
        int randomSpikeIndex = ThreadLocalRandom.current().nextInt(spikeHeights.size());
        Block endSpike = bedrockBlocksByHeight.get(spikeHeights.get(randomSpikeIndex));

        endSpike.setType(Material.OBSIDIAN);
        endSpike.getRelative(BlockFace.DOWN).setType(Material.BEDROCK);

        world.getPersistentDataContainer().set(modifiedSpike, PersistentDataType.BOOLEAN, true);
    }

    public Map<Integer, Block> getBedrockBlocksByHeight(World world) {
        Map<Integer, Block> bedrockBlocksByHeight = new HashMap<>(10);

        for (int i = 0; i < 10; i++) {
            // Source: net.minecraft.world.level.levelgen.feature.SpikeFeature.SpikeCacheLoader
            double x = 42.0 * Math.cos(2.0 * (-Math.PI + 0.3141592653589793 * i));
            double z = 42.0 * Math.sin(2.0 * (-Math.PI + 0.3141592653589793 * i));

            Block block = world.getHighestBlockAt(new Location(world, x, 0, z));
            while (block.getType() != Material.BEDROCK && block.getY() > 0) {
                block = block.getRelative(BlockFace.DOWN);
            }
            bedrockBlocksByHeight.put(block.getY(), block);
        }

        return bedrockBlocksByHeight;
    }

    public NamespacedKey getModifiedSpike() {
        return modifiedSpike;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
