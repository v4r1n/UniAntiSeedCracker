package com.v4r1n.uniantiseedcracker.listeners;

import com.v4r1n.uniantiseedcracker.UniAntiSeedCracker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
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

public class JungleTempleModifier implements Listener {

    private final UniAntiSeedCracker plugin;
    private final NamespacedKey templeModified;

    public JungleTempleModifier(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.templeModified = new NamespacedKey(plugin, "jungle-temple-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                || !plugin.getConfig().getStringList("modifiers.jungle_temple.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.JUNGLE_PYRAMID);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(templeModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            // Get the bounding box of the structure
            BoundingBox boundingBox = structure.getBoundingBox();

            // Find chests and dispensers within the structure
            List<Block> chests = new ArrayList<>();
            List<Block> dispensers = new ArrayList<>();

            for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
                for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++) {
                    for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.CHEST) {
                            chests.add(block);
                        } else if (block.getType() == Material.DISPENSER) {
                            dispensers.add(block);
                        }
                    }
                }
            }

            int modifiedCount = 0;

            // Modify chests (jungle temple has 2 chests)
            for (Block chestBlock : chests) {
                // Random chance to move each chest (50%)
                if (ThreadLocalRandom.current().nextBoolean()) {
                    continue;
                }

                // Move chest by random offset
                int offsetX = ThreadLocalRandom.current().nextInt(-1, 2); // -1 to 1
                int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2); // -1 to 1

                if (offsetX == 0 && offsetZ == 0) {
                    continue;
                }

                Location newLocation = chestBlock.getLocation().add(offsetX, 0, offsetZ);
                Block newBlock = newLocation.getBlock();

                // Only move if the new location has cobblestone (floor of temple)
                if (newBlock.getType() == Material.COBBLESTONE || newBlock.getType() == Material.MOSSY_COBBLESTONE) {
                    BlockState state = chestBlock.getState();
                    if (state instanceof Chest oldChest) {
                        Inventory oldInventory = oldChest.getBlockInventory();
                        ItemStack[] contents = oldInventory.getContents().clone();

                        // Remove old chest
                        chestBlock.setType(Material.COBBLESTONE);

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

            // Optionally modify dispenser positions (trap mechanism)
            if (plugin.getConfig().getBoolean("modifiers.jungle_temple.modify_traps", false)) {
                for (Block dispenserBlock : dispensers) {
                    // Move dispenser slightly to break the trap pattern
                    int offsetY = ThreadLocalRandom.current().nextInt(-1, 2);
                    if (offsetY == 0) {
                        continue;
                    }

                    Location newLocation = dispenserBlock.getLocation().add(0, offsetY, 0);
                    Block newBlock = newLocation.getBlock();

                    if (newBlock.getType() == Material.COBBLESTONE || newBlock.getType() == Material.MOSSY_COBBLESTONE) {
                        BlockState state = dispenserBlock.getState();
                        if (state instanceof Dispenser oldDispenser) {
                            Inventory oldInventory = oldDispenser.getInventory();
                            ItemStack[] contents = oldInventory.getContents().clone();

                            // Remove old dispenser
                            dispenserBlock.setType(Material.COBBLESTONE);

                            // Place new dispenser
                            newBlock.setType(Material.DISPENSER);
                            BlockState newState = newBlock.getState();
                            if (newState instanceof Dispenser newDispenser) {
                                newDispenser.getInventory().setContents(contents);
                            }

                            modifiedCount++;
                        }
                    }
                }
            }

            if (modifiedCount > 0) {
                structure.getPersistentDataContainer().set(templeModified, PersistentDataType.BOOLEAN, true);
                plugin.getLogger().info("Modified " + modifiedCount + " block(s) in jungle temple at " + boundingBox.getCenter());
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}
