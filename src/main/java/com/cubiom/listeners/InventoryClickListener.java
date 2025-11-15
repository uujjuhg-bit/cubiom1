package com.cubiom.listeners;

import com.cubiom.Cubiom;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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
        String title = event.getInventory().getTitle();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (title.contains("Survival Games") || title.contains("SG")) {
            event.setCancelled(true);
            handleSGMenu(player, clicked);
        } else if (title.contains("Duel")) {
            event.setCancelled(true);
            handleDuelMenu(player, clicked);
        } else if (title.contains("Stats") || title.contains("Statistik")) {
            event.setCancelled(true);
            if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
            }
        } else if (title.contains("Leaderboard")) {
            event.setCancelled(true);
            if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
            }
        } else if (title.contains("Language") || title.contains("Sprog") || title.contains("Sprache") || title.contains("Idioma")) {
            event.setCancelled(true);
            handleLanguageMenu(player, clicked);
        }
    }

    private void handleSGMenu(Player player, ItemStack clicked) {
        if (clicked.getType() == Material.DIAMOND_SWORD) {
            player.closeInventory();
            plugin.getSGManager().joinGame(player);
        } else if (clicked.getType() == Material.PAPER) {
            player.closeInventory();
            plugin.getGUIManager().openStatsMenu(player);
        } else if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        }
    }

    private void handleDuelMenu(Player player, ItemStack clicked) {
        if (clicked.getType() == Material.IRON_SWORD) {
            player.closeInventory();
            plugin.getDuelManager().joinQueue(player);
        } else if (clicked.getType() == Material.PAPER) {
            player.closeInventory();
            plugin.getGUIManager().openStatsMenu(player);
        } else if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        }
    }

    private void handleLanguageMenu(Player player, ItemStack clicked) {
        if (clicked.getType() == Material.PAPER) {
            String displayName = clicked.getItemMeta().getDisplayName();

            if (displayName.contains("English")) {
                plugin.getDataManager().setPlayerLanguage(player.getUniqueId(), "en_US");
                player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("en_US", "general.reload-success"));
                plugin.getHotbarManager().giveLobbyHotbar(player);
            } else if (displayName.contains("Dansk")) {
                plugin.getDataManager().setPlayerLanguage(player.getUniqueId(), "da_DK");
                player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("da_DK", "general.reload-success"));
                plugin.getHotbarManager().giveLobbyHotbar(player);
            } else if (displayName.contains("Deutsch")) {
                plugin.getDataManager().setPlayerLanguage(player.getUniqueId(), "de_DE");
                player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("de_DE", "general.reload-success"));
                plugin.getHotbarManager().giveLobbyHotbar(player);
            } else if (displayName.contains("Español")) {
                plugin.getDataManager().setPlayerLanguage(player.getUniqueId(), "es_ES");
                player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("es_ES", "general.reload-success"));
                plugin.getHotbarManager().giveLobbyHotbar(player);
            }

            player.closeInventory();
        } else if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        }
    }
}
