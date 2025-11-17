package com.cubiom.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WorldSnapshot {

    private final Map<Location, BlockData> blocks;
    private final Map<Location, ItemStack[]> chestContents;

    public WorldSnapshot() {
        this.blocks = new HashMap<>();
        this.chestContents = new HashMap<>();
    }

    public void captureBlock(Block block) {
        Location loc = block.getLocation();
        blocks.put(loc, new BlockData(block.getType(), block.getData()));

        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            chestContents.put(loc, chest.getInventory().getContents().clone());
        }
    }

    public void captureArea(Location corner1, Location corner2) {
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(corner1.getWorld(), x, y, z);
                    captureBlock(loc.getBlock());
                }
            }
        }
    }

    public void restore() {
        for (Map.Entry<Location, BlockData> entry : blocks.entrySet()) {
            Location loc = entry.getKey();
            BlockData data = entry.getValue();

            Block block = loc.getBlock();
            block.setType(data.material);
            block.setData(data.data);

            if (chestContents.containsKey(loc) && block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                chest.getInventory().setContents(chestContents.get(loc));
                chest.update();
            }
        }
    }

    public void clear() {
        blocks.clear();
        chestContents.clear();
    }

    private static class BlockData {
        private final Material material;
        private final byte data;

        public BlockData(Material material, byte data) {
            this.material = material;
            this.data = data;
        }
    }
}
