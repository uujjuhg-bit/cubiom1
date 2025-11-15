package com.cubiom.utils;

import com.cubiom.Cubiom;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HotbarManager {

    private final Cubiom plugin;

    public HotbarManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public void giveLobbyHotbar(Player player) {
        player.getInventory().clear();

        ItemStack sgItem = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName(plugin.getLanguageManager().getMessage(player, "items.sg-selector"))
                .setLore("&7Right-click to open", "&7Survival Games menu")
                .build();

        ItemStack duelItem = new ItemBuilder(Material.IRON_SWORD)
                .setName(plugin.getLanguageManager().getMessage(player, "items.duels-selector"))
                .setLore("&7Right-click to open", "&7Duels menu")
                .build();

        ItemStack statsItem = new ItemBuilder(Material.BOOK)
                .setName(plugin.getLanguageManager().getMessage(player, "items.stats-selector"))
                .setLore("&7Right-click to view", "&7your statistics")
                .build();

        ItemStack topItem = new ItemBuilder(Material.EMERALD)
                .setName("&b&lLeaderboards")
                .setLore("&7Right-click to view", "&7top players")
                .build();

        ItemStack langItem = new ItemBuilder(Material.NAME_TAG)
                .setName(plugin.getLanguageManager().getMessage(player, "items.language-selector"))
                .setLore("&7Right-click to change", "&7your language")
                .build();

        player.getInventory().setItem(0, sgItem);
        player.getInventory().setItem(1, duelItem);
        player.getInventory().setItem(4, statsItem);
        player.getInventory().setItem(7, topItem);
        player.getInventory().setItem(8, langItem);

        player.updateInventory();
    }

    public void giveLeaveItem(Player player) {
        ItemStack leaveItem = new ItemBuilder(Material.BED)
                .setName(plugin.getLanguageManager().getMessage(player, "items.leave-game"))
                .setLore("&7Right-click to leave")
                .build();

        player.getInventory().setItem(8, leaveItem);
        player.updateInventory();
    }
}
