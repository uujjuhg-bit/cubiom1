package com.cubiom.gamemodes.sg;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootManager {

    private final Random random;
    private final List<LootItem> tier1Items;
    private final List<LootItem> tier2Items;

    public LootManager() {
        this.random = new Random();
        this.tier1Items = new ArrayList<>();
        this.tier2Items = new ArrayList<>();

        initializeTier1Loot();
        initializeTier2Loot();
    }

    private void initializeTier1Loot() {
        tier1Items.add(new LootItem(Material.WOOD_SWORD, 1, 1, 30));
        tier1Items.add(new LootItem(Material.STONE_SWORD, 1, 1, 15));
        tier1Items.add(new LootItem(Material.WOOD_AXE, 1, 1, 25));
        tier1Items.add(new LootItem(Material.STONE_AXE, 1, 1, 10));
        tier1Items.add(new LootItem(Material.BOW, 1, 1, 10));
        tier1Items.add(new LootItem(Material.ARROW, 4, 16, 20));
        tier1Items.add(new LootItem(Material.LEATHER_HELMET, 1, 1, 15));
        tier1Items.add(new LootItem(Material.LEATHER_CHESTPLATE, 1, 1, 15));
        tier1Items.add(new LootItem(Material.LEATHER_LEGGINGS, 1, 1, 15));
        tier1Items.add(new LootItem(Material.LEATHER_BOOTS, 1, 1, 15));
        tier1Items.add(new LootItem(Material.CHAINMAIL_HELMET, 1, 1, 8));
        tier1Items.add(new LootItem(Material.CHAINMAIL_CHESTPLATE, 1, 1, 8));
        tier1Items.add(new LootItem(Material.BREAD, 2, 5, 25));
        tier1Items.add(new LootItem(Material.APPLE, 1, 3, 30));
        tier1Items.add(new LootItem(Material.COOKED_CHICKEN, 1, 3, 20));
        tier1Items.add(new LootItem(Material.STICK, 2, 6, 20));
        tier1Items.add(new LootItem(Material.FLINT, 1, 2, 15));
        tier1Items.add(new LootItem(Material.FEATHER, 2, 5, 15));
        tier1Items.add(new LootItem(Material.FISHING_ROD, 1, 1, 10));
        tier1Items.add(new LootItem(Material.WATER_BUCKET, 1, 1, 5));
        tier1Items.add(new LootItem(Material.LAVA_BUCKET, 1, 1, 3));
    }

    private void initializeTier2Loot() {
        tier2Items.add(new LootItem(Material.IRON_SWORD, 1, 1, 25));
        tier2Items.add(new LootItem(Material.DIAMOND_SWORD, 1, 1, 5));
        tier2Items.add(new LootItem(Material.IRON_AXE, 1, 1, 20));
        tier2Items.add(new LootItem(Material.BOW, 1, 1, 20));
        tier2Items.add(new LootItem(Material.ARROW, 8, 32, 25));
        tier2Items.add(new LootItem(Material.IRON_HELMET, 1, 1, 15));
        tier2Items.add(new LootItem(Material.IRON_CHESTPLATE, 1, 1, 15));
        tier2Items.add(new LootItem(Material.IRON_LEGGINGS, 1, 1, 15));
        tier2Items.add(new LootItem(Material.IRON_BOOTS, 1, 1, 15));
        tier2Items.add(new LootItem(Material.DIAMOND_HELMET, 1, 1, 5));
        tier2Items.add(new LootItem(Material.DIAMOND_CHESTPLATE, 1, 1, 5));
        tier2Items.add(new LootItem(Material.COOKED_BEEF, 3, 8, 30));
        tier2Items.add(new LootItem(Material.GOLDEN_APPLE, 1, 2, 15));
        tier2Items.add(new LootItem(Material.FLINT_AND_STEEL, 1, 1, 10));
        tier2Items.add(new LootItem(Material.ENDER_PEARL, 1, 2, 8));
        tier2Items.add(new LootItem(Material.POTION, 1, 2, 10));
        tier2Items.add(new LootItem(Material.TNT, 1, 3, 8));
        tier2Items.add(new LootItem(Material.LAVA_BUCKET, 1, 1, 5));
        tier2Items.add(new LootItem(Material.WATER_BUCKET, 1, 1, 10));
    }

    public void fillChest(Inventory inventory, int tier) {
        inventory.clear();

        List<LootItem> lootTable = tier == 1 ? tier1Items : tier2Items;
        int itemCount = tier == 1 ? random.nextInt(3) + 2 : random.nextInt(4) + 3;

        List<LootItem> selectedItems = selectRandomItems(lootTable, itemCount);

        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            slots.add(i);
        }
        Collections.shuffle(slots);

        for (int i = 0; i < selectedItems.size() && i < slots.size(); i++) {
            LootItem lootItem = selectedItems.get(i);
            ItemStack item = lootItem.createItemStack();
            inventory.setItem(slots.get(i), item);
        }
    }

    private List<LootItem> selectRandomItems(List<LootItem> lootTable, int count) {
        List<LootItem> selected = new ArrayList<>();
        int totalWeight = lootTable.stream().mapToInt(LootItem::getWeight).sum();

        for (int i = 0; i < count; i++) {
            int randomValue = random.nextInt(totalWeight);
            int currentWeight = 0;

            for (LootItem item : lootTable) {
                currentWeight += item.getWeight();
                if (randomValue < currentWeight) {
                    selected.add(item);
                    break;
                }
            }
        }

        return selected;
    }

    private static class LootItem {
        private final Material material;
        private final int minAmount;
        private final int maxAmount;
        private final int weight;

        public LootItem(Material material, int minAmount, int maxAmount, int weight) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }

        public ItemStack createItemStack() {
            Random random = new Random();
            int amount = minAmount + random.nextInt(maxAmount - minAmount + 1);
            return new ItemStack(material, amount);
        }
    }
}
