package com.cubiom.gamemodes.sg;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BlockSnapshot {

    private final Location location;
    private final Material material;
    private final byte data;
    private final Map<Integer, ItemStack> chestContents;

    public BlockSnapshot(Block block) {
        this.location = block.getLocation();
        this.material = block.getType();
        this.data = block.getData();
        this.chestContents = new HashMap<>();

        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            Inventory inv = chest.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null) {
                    chestContents.put(i, item.clone());
                }
            }
        }
    }

    public void restore() {
        Block block = location.getBlock();

        block.setType(material);
        block.setData(data);

        if (!chestContents.isEmpty() && block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            Inventory inv = chest.getInventory();
            inv.clear();

            for (Map.Entry<Integer, ItemStack> entry : chestContents.entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue().clone());
            }
        }
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }
}
