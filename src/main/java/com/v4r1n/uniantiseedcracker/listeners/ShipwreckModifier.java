package com.v4r1n.uniantiseedcracker.listeners;

import com.v4r1n.uniantiseedcracker.UniAntiSeedCracker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ShipwreckModifier implements Listener {

    private final UniAntiSeedCracker plugin;
    private final NamespacedKey shipwreckModified;

    public ShipwreckModifier(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.shipwreckModified = new NamespacedKey(plugin, "shipwreck-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (!plugin.getConfig().getStringList("modifiers.shipwreck.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.SHIPWRECK);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(shipwreckModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            BoundingBox boundingBox = structure.getBoundingBox();

            // Find chests within the shipwreck (can have up to 3 chests: supply, treasure, map)
            List<Block> chests = new ArrayList<>();
            for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
                for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++) {
                    for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.CHEST) {
                            chests.add(block);
                        }
                    }
                }
            }

            if (chests.isEmpty()) {
                continue;
            }

            int modifiedCount = 0;
            for (Block chestBlock : chests) {
                // Random chance to move each chest (60%)
                if (ThreadLocalRandom.current().nextInt(100) >= 60) {
                    continue;
                }

                // Move chest by random offset within the structure
                int offsetX = ThreadLocalRandom.current().nextInt(-2, 3);
                int offsetY = ThreadLocalRandom.current().nextInt(-1, 2);
                int offsetZ = ThreadLocalRandom.current().nextInt(-2, 3);

                if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
                    continue;
                }

                Location newLocation = chestBlock.getLocation().add(offsetX, offsetY, offsetZ);
                Block newBlock = newLocation.getBlock();

                // Only move if the new location is air or water (shipwrecks can be underwater)
                if (newBlock.getType() == Material.AIR || newBlock.getType() == Material.WATER || newBlock.getType() == Material.CAVE_AIR) {
                    BlockState state = chestBlock.getState();
                    if (state instanceof Chest oldChest) {
                        Inventory oldInventory = oldChest.getBlockInventory();
                        ItemStack[] contents = oldInventory.getContents().clone();

                        // Remove old chest (replace with what the new location had)
                        Material replacementMaterial = newBlock.getType();
                        chestBlock.setType(replacementMaterial);

                        // Place new chest
                        newBlock.setType(Material.CHEST);
                        BlockState newState = newBlock.getState();
                        if (newState instanceof Chest newChest) {
                            newChest.getBlockInventory().setContents(contents);
                        }

                        modifiedCount++;
                    }
                }
            }

            if (modifiedCount > 0) {
                structure.getPersistentDataContainer().set(shipwreckModified, PersistentDataType.BOOLEAN, true);
                plugin.getLogger().info("Modified " + modifiedCount + " chest(s) in shipwreck at " + boundingBox.getCenter());
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}
