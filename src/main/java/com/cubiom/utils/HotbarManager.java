package com.cubiom.utils;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotbarManager {

    private final Cubiom plugin;

    public HotbarManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public void giveHotbarItems(Player player) {
        player.getInventory().clear();
        LanguageManager lang = plugin.getLanguageManager();

        String sgName = lang.getMessage(player, "items.sg-selector.name");
        List<String> sgLore = lang.getMessageList(player, "items.sg-selector.lore");
        ItemStack sgItem = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName(sgName)
                .setLore(sgLore)
                .build();

        String duelName = lang.getMessage(player, "items.duels-selector.name");
        List<String> duelLore = lang.getMessageList(player, "items.duels-selector.lore");
        ItemStack duelItem = new ItemBuilder(Material.IRON_SWORD)
                .setName(duelName)
                .setLore(duelLore)
                .build();

        String statsName = lang.getMessage(player, "items.stats-selector.name");
        List<String> statsLore = lang.getMessageList(player, "items.stats-selector.lore");
        ItemStack statsItem = new ItemBuilder(Material.PAPER)
                .setName(statsName)
                .setLore(statsLore)
                .build();

        String lbName = lang.getMessage(player, "items.leaderboard-selector.name");
        List<String> lbLore = lang.getMessageList(player, "items.leaderboard-selector.lore");
        ItemStack lbItem = new ItemBuilder(Material.EMERALD)
                .setName(lbName)
                .setLore(lbLore)
                .build();

        Map<String, String> langRep = new HashMap<>();
        String currentLang = plugin.getDataManager().getPlayerLanguage(player.getUniqueId());
        langRep.put("language", lang.getLanguageName(currentLang));

        String langName = lang.formatMessage(player, "items.language-selector.name", langRep);
        List<String> langLore = lang.getMessageList(player, "items.language-selector.lore");
        for (int i = 0; i < langLore.size(); i++) {
            String line = langLore.get(i);
            for (Map.Entry<String, String> entry : langRep.entrySet()) {
                line = line.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            langLore.set(i, line);
        }
        ItemStack langItem = new ItemBuilder(Material.NAME_TAG)
                .setName(langName)
                .setLore(langLore)
                .build();

        player.getInventory().setItem(0, sgItem);
        player.getInventory().setItem(1, duelItem);
        player.getInventory().setItem(4, statsItem);
        player.getInventory().setItem(7, lbItem);
        player.getInventory().setItem(8, langItem);

        player.updateInventory();
    }

    public void giveLobbyHotbar(Player player) {
        giveHotbarItems(player);
    }

    public void giveLeaveItem(Player player) {
        LanguageManager lang = plugin.getLanguageManager();

        String leaveName = lang.getMessage(player, "items.leave-game.name");
        List<String> leaveLore = lang.getMessageList(player, "items.leave-game.lore");
        ItemStack leaveItem = new ItemBuilder(Material.BED)
                .setName(leaveName)
                .setLore(leaveLore)
                .build();

        player.getInventory().setItem(8, leaveItem);
        player.updateInventory();
    }
}
