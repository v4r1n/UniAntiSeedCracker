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

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class BuriedTreasureModifier implements Listener {

    private final UniAntiSeedCracker plugin;
    private final NamespacedKey treasureModified;

    public BuriedTreasureModifier(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.treasureModified = new NamespacedKey(plugin, "buried-treasure-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                || !plugin.getConfig().getStringList("modifiers.buried_treasure.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.BURIED_TREASURE);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(treasureModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            // Search for the chest in the chunk
            // Buried treasure is always at chunk center (X=9, Z=9) at varying Y levels
            int chunkX = event.getChunk().getX() * 16;
            int chunkZ = event.getChunk().getZ() * 16;

            Block chestBlock = null;
            // Search from Y=0 to Y=90 for the chest (usually around Y=40-70)
            for (int y = 0; y <= 90; y++) {
                Block block = world.getBlockAt(chunkX + 9, y, chunkZ + 9);
                if (block.getType() == Material.CHEST) {
                    chestBlock = block;
                    break;
                }
            }

            if (chestBlock == null) {
                continue;
            }

            // Move the chest to a random offset position
            int offsetX = ThreadLocalRandom.current().nextInt(-2, 3); // -2 to 2
            int offsetZ = ThreadLocalRandom.current().nextInt(-2, 3); // -2 to 2
            int offsetY = ThreadLocalRandom.current().nextInt(-1, 2); // -1 to 1

            // Skip if no offset
            if (offsetX == 0 && offsetZ == 0 && offsetY == 0) {
                offsetX = 1; // Force at least some offset
            }

            Location newLocation = chestBlock.getLocation().add(offsetX, offsetY, offsetZ);
            Block newBlock = newLocation.getBlock();

            // Only move if the new location is suitable (not air or water above ground)
            if (newBlock.getType().isSolid() || newBlock.getType() == Material.AIR) {
                // Save chest contents
                BlockState state = chestBlock.getState();
                if (state instanceof Chest oldChest) {
                    Inventory oldInventory = oldChest.getBlockInventory();
                    ItemStack[] contents = oldInventory.getContents().clone();

                    // Remove old chest
                    chestBlock.setType(Material.SAND);

                    // Place new chest
                    newBlock.setType(Material.CHEST);
                    BlockState newState = newBlock.getState();
                    if (newState instanceof Chest newChest) {
                        newChest.getBlockInventory().setContents(contents);
                    }

                    structure.getPersistentDataContainer().set(treasureModified, PersistentDataType.BOOLEAN, true);
                    plugin.getLogger().info("Modified buried treasure chest at " + chestBlock.getLocation() + " -> " + newLocation);
                }
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}
