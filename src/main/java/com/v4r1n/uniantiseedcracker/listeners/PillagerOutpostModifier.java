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

public class PillagerOutpostModifier implements Listener {

    private final UniAntiSeedCracker plugin;
    private final NamespacedKey outpostModified;

    public PillagerOutpostModifier(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.outpostModified = new NamespacedKey(plugin, "outpost-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                || !plugin.getConfig().getStringList("modifiers.pillager_outpost.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.PILLAGER_OUTPOST);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(outpostModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            BoundingBox boundingBox = structure.getBoundingBox();

            // Find chests, dark oak logs (cage structure), and targets within the outpost
            List<Block> chests = new ArrayList<>();
            List<Block> darkOakLogs = new ArrayList<>();
            List<Block> targets = new ArrayList<>();

            for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
                for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++) {
                    for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.CHEST) {
                            chests.add(block);
                        } else if (block.getType() == Material.DARK_OAK_LOG) {
                            darkOakLogs.add(block);
                        } else if (block.getType() == Material.TARGET) {
                            targets.add(block);
                        }
                    }
                }
            }

            int modifiedCount = 0;

            // Modify chest positions
            for (Block chestBlock : chests) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
                    int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);

                    if (offsetX != 0 || offsetZ != 0) {
                        Location newLocation = chestBlock.getLocation().add(offsetX, 0, offsetZ);
                        Block newBlock = newLocation.getBlock();

                        if (newBlock.getType() == Material.AIR || newBlock.getType() == Material.BIRCH_PLANKS) {
                            BlockState state = chestBlock.getState();
                            if (state instanceof Chest oldChest) {
                                Inventory oldInventory = oldChest.getBlockInventory();
                                ItemStack[] contents = oldInventory.getContents().clone();

                                chestBlock.setType(Material.BIRCH_PLANKS);
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
            }

            // Modify some cage logs (replace with birch or cobblestone)
            int logsToModify = Math.min(darkOakLogs.size() / 10, 3);
            for (int i = 0; i < logsToModify; i++) {
                if (!darkOakLogs.isEmpty()) {
                    Block log = darkOakLogs.get(ThreadLocalRandom.current().nextInt(darkOakLogs.size()));
                    log.setType(Material.SPRUCE_WOOD);
                    darkOakLogs.remove(log);
                    modifiedCount++;
                }
            }

            // Modify target block positions
            for (Block target : targets) {
                if (ThreadLocalRandom.current().nextInt(100) < 40) { // 40% chance
                    int offsetY = ThreadLocalRandom.current().nextInt(-1, 2);
                    if (offsetY != 0) {
                        Block newBlock = target.getRelative(0, offsetY, 0);
                        if (newBlock.getType() == Material.AIR) {
                            target.setType(Material.AIR);
                            newBlock.setType(Material.TARGET);
                            modifiedCount++;
                        }
                    }
                }
            }

            if (modifiedCount > 0) {
                structure.getPersistentDataContainer().set(outpostModified, PersistentDataType.BOOLEAN, true);
                plugin.getLogger().info("Modified " + modifiedCount + " block(s) in pillager outpost at " + boundingBox.getCenter());
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}
