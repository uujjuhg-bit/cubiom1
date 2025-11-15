package com.cubiom.inventory;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import com.cubiom.stats.PlayerStats;
import com.cubiom.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GUIManager {

    private final Cubiom plugin;

    public GUIManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public void openSGMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(null, 27, lang.getMessage(player, "gui.sg-menu"));

        ItemBuilder joinItem = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&b&lJoin Game")
                .setLore("&7Click to join", "&7Survival Games");

        ItemBuilder statsItem = new ItemBuilder(Material.PAPER)
                .setName("&b&lYour SG Stats")
                .setLore("&7View your stats");

        ItemBuilder leaveItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Close this menu");

        inv.setItem(11, joinItem.build());
        inv.setItem(13, statsItem.build());
        inv.setItem(15, leaveItem.build());

        player.openInventory(inv);
    }

    public void openDuelsMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(null, 27, lang.getMessage(player, "gui.duels-menu"));

        ItemBuilder joinItem = new ItemBuilder(Material.IRON_SWORD)
                .setName("&b&lJoin Queue")
                .setLore("&7Click to join", "&7the duel queue");

        ItemBuilder statsItem = new ItemBuilder(Material.PAPER)
                .setName("&b&lYour Duel Stats")
                .setLore("&7View your stats");

        ItemBuilder leaveItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Close this menu");

        inv.setItem(11, joinItem.build());
        inv.setItem(13, statsItem.build());
        inv.setItem(15, leaveItem.build());

        player.openInventory(inv);
    }

    public void openStatsMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(null, 54, lang.getMessage(player, "gui.stats-menu"));

        PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());

        ItemBuilder sgStats = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&b&lSurvival Games Stats")
                .setLore(
                        "&7Wins: &b" + stats.getSgWins(),
                        "&7Kills: &b" + stats.getSgKills(),
                        "&7Deaths: &b" + stats.getSgDeaths(),
                        "&7KDR: &b" + String.format("%.2f", stats.getSgKDR())
                );

        ItemBuilder duelStats = new ItemBuilder(Material.IRON_SWORD)
                .setName("&b&lDuels Stats")
                .setLore(
                        "&7Wins: &b" + stats.getDuelWins(),
                        "&7Losses: &b" + stats.getDuelLosses(),
                        "&7ELO: &b" + stats.getDuelElo()
                );

        ItemBuilder closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Close this menu");

        inv.setItem(20, sgStats.build());
        inv.setItem(24, duelStats.build());
        inv.setItem(49, closeItem.build());

        player.openInventory(inv);
    }

    public void openTopMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "&b&lLeaderboards");

        ItemBuilder sgTop = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&b&lTop SG Players")
                .setLore("&7Coming soon...");

        ItemBuilder duelTop = new ItemBuilder(Material.IRON_SWORD)
                .setName("&b&lTop Duel Players")
                .setLore("&7Coming soon...");

        ItemBuilder closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Close this menu");

        inv.setItem(20, sgTop.build());
        inv.setItem(24, duelTop.build());
        inv.setItem(49, closeItem.build());

        player.openInventory(inv);
    }

    public void openLanguageMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(null, 27, lang.getMessage(player, "gui.language-menu"));

        ItemBuilder englishItem = new ItemBuilder(Material.PAPER)
                .setName("&bEnglish")
                .setLore("&7Click to select", "&7Code: en_US");

        ItemBuilder danishItem = new ItemBuilder(Material.PAPER)
                .setName("&bDansk")
                .setLore("&7Klik for at vælge", "&7Kode: da_DK");

        ItemBuilder germanItem = new ItemBuilder(Material.PAPER)
                .setName("&bDeutsch")
                .setLore("&7Klicken Sie um auszuwählen", "&7Code: de_DE");

        ItemBuilder spanishItem = new ItemBuilder(Material.PAPER)
                .setName("&bEspañol")
                .setLore("&7Haz clic para seleccionar", "&7Código: es_ES");

        ItemBuilder closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Close this menu");

        inv.setItem(10, englishItem.build());
        inv.setItem(12, danishItem.build());
        inv.setItem(14, germanItem.build());
        inv.setItem(16, spanishItem.build());
        inv.setItem(22, closeItem.build());

        player.openInventory(inv);
    }
}
