package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import com.cubiom.language.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final Cubiom plugin;

    public InventoryClickListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) {
            return;
        }

        String title = event.getInventory().getTitle();
        String menuType = plugin.getGUIManager().getPlayerMenu(player);

        if (menuType == null) {
            return;
        }

        event.setCancelled(true);

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (itemName.equalsIgnoreCase("Close")) {
            player.closeInventory();
            return;
        }

        switch (menuType) {
            case "SG_MAIN":
                handleSGMainMenu(player, clicked, itemName, event.getSlot());
                break;

            case "DUELS_MAIN":
                handleDuelsMainMenu(player, clicked, itemName);
                break;

            case "STATS":
                handleStatsMenu(player, clicked, itemName);
                break;

            case "LEADERBOARD":
                handleLeaderboardMenu(player, clicked, itemName);
                break;

            case "LANGUAGE":
                handleLanguageMenu(player, clicked, itemName);
                break;

            default:
                if (menuType.startsWith("LEADERBOARD_")) {
                    handleLeaderboardDetailsMenu(player, clicked, itemName);
                }
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            plugin.getGUIManager().removePlayerMenu(player);
        }
    }

    private void handleSGMainMenu(Player player, ItemStack clicked, String itemName, int slot) {
        if (itemName.contains("Your SG Stats")) {
            plugin.getGUIManager().openStatsMenu(player);
            return;
        }

        if (slot >= 10 && slot <= 34) {
            String arenaName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            Arena arena = plugin.getSGManager().getArenaByName(arenaName);

            if (arena != null && arena.isEnabled()) {
                player.closeInventory();
                boolean joined = plugin.getSGManager().joinGame(player, arena);

                LanguageManager lang = plugin.getLanguageManager();
                if (joined) {
                    player.sendMessage(lang.getMessageWithPrefix(player, "sg.join-success"));
                } else {
                    player.sendMessage(lang.getMessageWithPrefix(player, "sg.game-full"));
                }
            }
        }
    }

    private void handleDuelsMainMenu(Player player, ItemStack clicked, String itemName) {
        LanguageManager lang = plugin.getLanguageManager();

        if (itemName.contains("Your Duel Stats")) {
            plugin.getGUIManager().openStatsMenu(player);
            return;
        }

        String kitName = null;

        if (itemName.contains("NoDebuff")) {
            kitName = "NoDebuff";
        } else if (itemName.contains("Debuff") && !itemName.contains("No")) {
            kitName = "Debuff";
        } else if (itemName.contains("BuildUHC")) {
            kitName = "BuildUHC";
        } else if (itemName.contains("Classic")) {
            kitName = "Classic";
        } else if (itemName.contains("Combo")) {
            kitName = "Combo";
        }

        if (kitName != null) {
            player.closeInventory();
            plugin.getDuelManager().setPlayerKit(player, kitName);
            plugin.getDuelManager().joinQueue(player);

            player.sendMessage(lang.getMessageWithPrefix(player, "duels.join-queue"));
        }
    }

    private void handleStatsMenu(Player player, ItemStack clicked, String itemName) {
    }

    private void handleLeaderboardMenu(Player player, ItemStack clicked, String itemName) {
        if (itemName.contains("Top SG Wins")) {
            plugin.getGUIManager().openLeaderboardDetails(player, "SG_WINS");
        } else if (itemName.contains("Top SG Kills")) {
            plugin.getGUIManager().openLeaderboardDetails(player, "SG_KILLS");
        } else if (itemName.contains("Top Duel ELO")) {
            plugin.getGUIManager().openLeaderboardDetails(player, "DUEL_ELO");
        }
    }

    private void handleLeaderboardDetailsMenu(Player player, ItemStack clicked, String itemName) {
        if (itemName.equalsIgnoreCase("Back")) {
            plugin.getGUIManager().openTopMenu(player);
        }
    }

    private void handleLanguageMenu(Player player, ItemStack clicked, String itemName) {
        LanguageManager lang = plugin.getLanguageManager();
        String newLang = null;

        if (itemName.equalsIgnoreCase("English")) {
            newLang = "en_US";
        } else if (itemName.equalsIgnoreCase("Dansk")) {
            newLang = "da_DK";
        } else if (itemName.equalsIgnoreCase("Deutsch")) {
            newLang = "de_DE";
        } else if (itemName.equalsIgnoreCase("Español")) {
            newLang = "es_ES";
        }

        if (newLang != null) {
            plugin.getDataManager().setPlayerLanguage(player.getUniqueId(), newLang);
            player.closeInventory();

            plugin.getHotbarManager().giveHotbarItems(player);
            plugin.getScoreboardManager().setLobbyScoreboard(player);

            player.sendMessage(lang.getMessageWithPrefix(player, "language.changed"));
        }
    }
}
