package com.cubiom.game.sg;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootManager {

    private static final Random random = new Random();

    public static void fillTier1Chest(Inventory chest) {
        chest.clear();

        int itemCount = 3 + random.nextInt(4);

        List<ItemStack> possibleItems = getTier1Items();
        Collections.shuffle(possibleItems);

        for (int i = 0; i < Math.min(itemCount, possibleItems.size()); i++) {
            int slot = random.nextInt(chest.getSize());
            while (chest.getItem(slot) != null) {
                slot = random.nextInt(chest.getSize());
            }
            chest.setItem(slot, possibleItems.get(i));
        }
    }

    public static void fillTier2Chest(Inventory chest) {
        chest.clear();

        int itemCount = 4 + random.nextInt(5);

        List<ItemStack> possibleItems = getTier2Items();
        Collections.shuffle(possibleItems);

        for (int i = 0; i < Math.min(itemCount, possibleItems.size()); i++) {
            int slot = random.nextInt(chest.getSize());
            while (chest.getItem(slot) != null) {
                slot = random.nextInt(chest.getSize());
            }
            chest.setItem(slot, possibleItems.get(i));
        }
    }

    private static List<ItemStack> getTier1Items() {
        List<ItemStack> items = new ArrayList<>();

        items.add(new ItemStack(Material.WOOD_SWORD));
        items.add(new ItemStack(Material.STONE_SWORD));
        items.add(new ItemStack(Material.WOOD_AXE));
        items.add(new ItemStack(Material.STONE_AXE));

        items.add(new ItemStack(Material.LEATHER_HELMET));
        items.add(new ItemStack(Material.LEATHER_CHESTPLATE));
        items.add(new ItemStack(Material.LEATHER_LEGGINGS));
        items.add(new ItemStack(Material.LEATHER_BOOTS));
        items.add(new ItemStack(Material.CHAINMAIL_HELMET));
        items.add(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

        items.add(new ItemStack(Material.ARROW, 8 + random.nextInt(8)));
        items.add(new ItemStack(Material.BOW));

        items.add(new ItemStack(Material.BREAD, 2 + random.nextInt(3)));
        items.add(new ItemStack(Material.APPLE, 1 + random.nextInt(3)));
        items.add(new ItemStack(Material.COOKED_CHICKEN, 1 + random.nextInt(2)));

        items.add(new ItemStack(Material.STICK, 4 + random.nextInt(8)));
        items.add(new ItemStack(Material.WOOD, 8 + random.nextInt(16)));
        items.add(new ItemStack(Material.COBBLESTONE, 16 + random.nextInt(16)));

        items.add(new ItemStack(Material.FISHING_ROD));
        items.add(new ItemStack(Material.FLINT_AND_STEEL));

        return items;
    }

    private static List<ItemStack> getTier2Items() {
        List<ItemStack> items = new ArrayList<>();

        ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
        items.add(ironSword);

        ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
        items.add(diamondSword);

        items.add(new ItemStack(Material.IRON_AXE));
        items.add(new ItemStack(Material.DIAMOND_AXE));

        items.add(new ItemStack(Material.IRON_HELMET));
        items.add(new ItemStack(Material.IRON_CHESTPLATE));
        items.add(new ItemStack(Material.IRON_LEGGINGS));
        items.add(new ItemStack(Material.IRON_BOOTS));

        items.add(new ItemStack(Material.DIAMOND_HELMET));
        items.add(new ItemStack(Material.DIAMOND_CHESTPLATE));
        items.add(new ItemStack(Material.DIAMOND_LEGGINGS));
        items.add(new ItemStack(Material.DIAMOND_BOOTS));

        ItemStack enchantedBow = new ItemStack(Material.BOW);
        enchantedBow.addEnchantment(Enchantment.ARROW_DAMAGE, 1 + random.nextInt(2));
        items.add(enchantedBow);

        items.add(new ItemStack(Material.ARROW, 16 + random.nextInt(16)));

        items.add(new ItemStack(Material.GOLDEN_APPLE, 1 + random.nextInt(2)));
        items.add(new ItemStack(Material.COOKED_BEEF, 3 + random.nextInt(4)));
        items.add(new ItemStack(Material.BREAD, 4 + random.nextInt(4)));

        items.add(new ItemStack(Material.ENDER_PEARL, 1 + random.nextInt(2)));
        items.add(new ItemStack(Material.FISHING_ROD));
        items.add(new ItemStack(Material.FLINT_AND_STEEL));
        items.add(new ItemStack(Material.LAVA_BUCKET));
        items.add(new ItemStack(Material.WATER_BUCKET));

        ItemStack healthPotion = new ItemStack(Material.POTION, 1, (short) 8197);
        items.add(healthPotion);

        ItemStack speedPotion = new ItemStack(Material.POTION, 1, (short) 8194);
        items.add(speedPotion);

        items.add(new ItemStack(Material.DIAMOND, 1 + random.nextInt(3)));
        items.add(new ItemStack(Material.IRON_INGOT, 2 + random.nextInt(4)));
        items.add(new ItemStack(Material.GOLD_INGOT, 2 + random.nextInt(4)));

        items.add(new ItemStack(Material.WOOD, 16 + random.nextInt(32)));
        items.add(new ItemStack(Material.COBBLESTONE, 32 + random.nextInt(32)));

        return items;
    }
}
