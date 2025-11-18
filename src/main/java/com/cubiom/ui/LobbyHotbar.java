package com.cubiom.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class LobbyHotbar {

    public void giveHotbar(Player player) {
        giveLobbyItems(player);
    }

    public static void giveLobbyItems(Player player) {
        player.getInventory().clear();

        player.getInventory().setItem(0, createItem(Material.COMPASS, ChatColor.GREEN + "Game Selector",
            ChatColor.GRAY + "Choose between SG and Duels"));

        player.getInventory().setItem(1, createItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "Quick Play SG",
            ChatColor.GRAY + "Join a Survival Games match"));

        player.getInventory().setItem(2, createItem(Material.IRON_SWORD, ChatColor.LIGHT_PURPLE + "Quick Play Duels",
            ChatColor.GRAY + "Select a kit and find a match"));

        ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
        headMeta.setOwner(player.getName());
        headMeta.setDisplayName(ChatColor.YELLOW + "Your Profile");
        headMeta.setLore(java.util.Arrays.asList(ChatColor.GRAY + "View your stats and settings"));
        playerHead.setItemMeta(headMeta);
        player.getInventory().setItem(4, playerHead);

        player.getInventory().setItem(6, createItem(Material.EMERALD, ChatColor.GREEN + "Active Players",
            ChatColor.GRAY + "See online players"));

        player.getInventory().setItem(7, createItem(Material.GOLD_INGOT, ChatColor.GOLD + "Leaderboards",
            ChatColor.GRAY + "View top players"));

        player.getInventory().setItem(8, createItem(Material.REDSTONE_COMPARATOR, ChatColor.RED + "Settings",
            ChatColor.GRAY + "Language and preferences"));

        player.updateInventory();
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(java.util.Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}
