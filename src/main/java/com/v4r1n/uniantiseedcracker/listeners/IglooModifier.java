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

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class IglooModifier implements Listener {

    private final UniAntiSeedCracker plugin;
    private final NamespacedKey iglooModified;

    public IglooModifier(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.iglooModified = new NamespacedKey(plugin, "igloo-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                || !plugin.getConfig().getStringList("modifiers.igloo.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.IGLOO);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(iglooModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            BoundingBox boundingBox = structure.getBoundingBox();

            // Find brewing stand, cauldron, and chest in basement
            Block brewingStandBlock = null;
            Block cauldronBlock = null;
            Block chestBlock = null;

            for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
                for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++) {
                    for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        Material type = block.getType();
                        if (type == Material.BREWING_STAND) {
                            brewingStandBlock = block;
                        } else if (type == Material.CAULDRON) {
                            cauldronBlock = block;
                        } else if (type == Material.CHEST) {
                            chestBlock = block;
                        }
                    }
                }
            }

            int modifiedCount = 0;

            // Modify brewing stand position (if present - only in igloos with basements)
            if (brewingStandBlock != null && ThreadLocalRandom.current().nextBoolean()) {
                int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
                int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);

                if (offsetX != 0 || offsetZ != 0) {
                    Location newLocation = brewingStandBlock.getLocation().add(offsetX, 0, offsetZ);
                    Block newBlock = newLocation.getBlock();

                    if (newBlock.getType() == Material.STONE_BRICKS || newBlock.getType() == Material.AIR) {
                        brewingStandBlock.setType(Material.STONE_BRICKS);
                        newBlock.setType(Material.BREWING_STAND);
                        modifiedCount++;
                    }
                }
            }

            // Modify cauldron position
            if (cauldronBlock != null && ThreadLocalRandom.current().nextBoolean()) {
                int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
                int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);

                if (offsetX != 0 || offsetZ != 0) {
                    Location newLocation = cauldronBlock.getLocation().add(offsetX, 0, offsetZ);
                    Block newBlock = newLocation.getBlock();

                    if (newBlock.getType() == Material.STONE_BRICKS || newBlock.getType() == Material.AIR) {
                        cauldronBlock.setType(Material.STONE_BRICKS);
                        newBlock.setType(Material.CAULDRON);
                        modifiedCount++;
                    }
                }
            }

            // Modify chest position
            if (chestBlock != null && ThreadLocalRandom.current().nextBoolean()) {
                int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
                int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);

                if (offsetX != 0 || offsetZ != 0) {
                    Location newLocation = chestBlock.getLocation().add(offsetX, 0, offsetZ);
                    Block newBlock = newLocation.getBlock();

                    if (newBlock.getType() == Material.STONE_BRICKS || newBlock.getType() == Material.AIR) {
                        BlockState state = chestBlock.getState();
                        if (state instanceof Chest oldChest) {
                            Inventory oldInventory = oldChest.getBlockInventory();
                            ItemStack[] contents = oldInventory.getContents().clone();

                            chestBlock.setType(Material.STONE_BRICKS);
                            newBlock.setType(Material.CHEST);

                            BlockState newState = newBlock.getState();
                            if (newState instanceof Chest newChest) {
                                newChest.getBlockInventory().setContents(contents);
                            }

                            modifiedCount++;
                        }
                    }
                }
            }

            if (modifiedCount > 0) {
                structure.getPersistentDataContainer().set(iglooModified, PersistentDataType.BOOLEAN, true);
                plugin.getLogger().info("Modified " + modifiedCount + " block(s) in igloo at " + boundingBox.getCenter());
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}
