package com.v4r1n.uniantiseedcracker.listeners;

import com.v4r1n.uniantiseedcracker.UniAntiSeedCracker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class WitchHutModifier implements Listener {

    private final UniAntiSeedCracker plugin;
    private final NamespacedKey hutModified;

    public WitchHutModifier(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.hutModified = new NamespacedKey(plugin, "witch-hut-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                || !plugin.getConfig().getStringList("modifiers.witch_hut.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.SWAMP_HUT);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(hutModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            BoundingBox boundingBox = structure.getBoundingBox();

            // Find cauldron and crafting table within the hut
            Block cauldronBlock = null;
            Block craftingTableBlock = null;
            Block flowerPotBlock = null;

            for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
                for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++) {
                    for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.CAULDRON) {
                            cauldronBlock = block;
                        } else if (block.getType() == Material.CRAFTING_TABLE) {
                            craftingTableBlock = block;
                        } else if (block.getType() == Material.POTTED_RED_MUSHROOM) {
                            flowerPotBlock = block;
                        }
                    }
                }
            }

            int modifiedCount = 0;

            // Modify cauldron position
            if (cauldronBlock != null && ThreadLocalRandom.current().nextBoolean()) {
                int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
                int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);

                if (offsetX != 0 || offsetZ != 0) {
                    Location newLocation = cauldronBlock.getLocation().add(offsetX, 0, offsetZ);
                    Block newBlock = newLocation.getBlock();

                    if (newBlock.getType() == Material.OAK_PLANKS || newBlock.getType() == Material.AIR) {
                        cauldronBlock.setType(Material.OAK_PLANKS);
                        newBlock.setType(Material.CAULDRON);
                        modifiedCount++;
                    }
                }
            }

            // Modify crafting table position
            if (craftingTableBlock != null && ThreadLocalRandom.current().nextBoolean()) {
                int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
                int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);

                if (offsetX != 0 || offsetZ != 0) {
                    Location newLocation = craftingTableBlock.getLocation().add(offsetX, 0, offsetZ);
                    Block newBlock = newLocation.getBlock();

                    if (newBlock.getType() == Material.OAK_PLANKS || newBlock.getType() == Material.AIR) {
                        craftingTableBlock.setType(Material.OAK_PLANKS);
                        newBlock.setType(Material.CRAFTING_TABLE);
                        modifiedCount++;
                    }
                }
            }

            // Modify flower pot position
            if (flowerPotBlock != null && ThreadLocalRandom.current().nextBoolean()) {
                int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
                int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);

                if (offsetX != 0 || offsetZ != 0) {
                    Location newLocation = flowerPotBlock.getLocation().add(offsetX, 0, offsetZ);
                    Block newBlock = newLocation.getBlock();

                    if (newBlock.getType() == Material.AIR) {
                        flowerPotBlock.setType(Material.AIR);
                        newBlock.setType(Material.POTTED_RED_MUSHROOM);
                        modifiedCount++;
                    }
                }
            }

            if (modifiedCount > 0) {
                structure.getPersistentDataContainer().set(hutModified, PersistentDataType.BOOLEAN, true);
                plugin.getLogger().info("Modified " + modifiedCount + " block(s) in witch hut at " + boundingBox.getCenter());
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}
