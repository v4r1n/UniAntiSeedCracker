package com.v4r1n.uniantiseedcracker.listeners;

import com.v4r1n.uniantiseedcracker.UniAntiSeedCracker;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OceanMonumentModifier implements Listener {

    private final UniAntiSeedCracker plugin;
    private final NamespacedKey monumentModified;

    public OceanMonumentModifier(UniAntiSeedCracker plugin) {
        this.plugin = plugin;
        this.monumentModified = new NamespacedKey(plugin, "monument-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                || !plugin.getConfig().getStringList("modifiers.ocean_monument.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.MONUMENT);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(monumentModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            BoundingBox boundingBox = structure.getBoundingBox();

            // Find gold blocks and sea lanterns within the monument (these are distinctive markers)
            List<Block> goldBlocks = new ArrayList<>();
            List<Block> seaLanterns = new ArrayList<>();

            for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
                for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++) {
                    for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.GOLD_BLOCK) {
                            goldBlocks.add(block);
                        } else if (block.getType() == Material.SEA_LANTERN) {
                            seaLanterns.add(block);
                        }
                    }
                }
            }

            int modifiedCount = 0;

            // Modify some sea lanterns (replace with prismarine or move)
            int lanternsToModify = Math.min(seaLanterns.size() / 4, 5); // Modify up to 25% or max 5
            for (int i = 0; i < lanternsToModify && i < seaLanterns.size(); i++) {
                Block lantern = seaLanterns.get(ThreadLocalRandom.current().nextInt(seaLanterns.size()));

                if (ThreadLocalRandom.current().nextBoolean()) {
                    // Replace with prismarine
                    lantern.setType(Material.PRISMARINE);
                    modifiedCount++;
                } else {
                    // Move by one block
                    int offsetY = ThreadLocalRandom.current().nextInt(-1, 2);
                    if (offsetY != 0) {
                        Block newBlock = lantern.getRelative(0, offsetY, 0);
                        if (newBlock.getType() == Material.PRISMARINE || newBlock.getType() == Material.PRISMARINE_BRICKS || newBlock.getType() == Material.DARK_PRISMARINE) {
                            lantern.setType(Material.PRISMARINE);
                            newBlock.setType(Material.SEA_LANTERN);
                            modifiedCount++;
                        }
                    }
                }
            }

            // Modify gold block positions slightly (treasure room markers)
            for (Block goldBlock : goldBlocks) {
                if (ThreadLocalRandom.current().nextInt(100) < 30) { // 30% chance
                    int offsetY = ThreadLocalRandom.current().nextInt(-1, 2);
                    if (offsetY != 0) {
                        Block newBlock = goldBlock.getRelative(0, offsetY, 0);
                        if (newBlock.getType() == Material.DARK_PRISMARINE || newBlock.getType() == Material.PRISMARINE_BRICKS) {
                            goldBlock.setType(Material.DARK_PRISMARINE);
                            newBlock.setType(Material.GOLD_BLOCK);
                            modifiedCount++;
                        }
                    }
                }
            }

            if (modifiedCount > 0) {
                structure.getPersistentDataContainer().set(monumentModified, PersistentDataType.BOOLEAN, true);
                plugin.getLogger().info("Modified " + modifiedCount + " block(s) in ocean monument at " + boundingBox.getCenter());
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}
