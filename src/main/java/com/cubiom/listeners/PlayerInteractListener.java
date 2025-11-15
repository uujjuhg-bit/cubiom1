package com.cubiom.listeners;

import com.cubiom.Cubiom;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private final Cubiom plugin;

    public PlayerInteractListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Material type = item.getType();
        String displayName = item.getItemMeta().getDisplayName();

        if (type == Material.DIAMOND_SWORD && displayName.contains("Survival Games")) {
            event.setCancelled(true);
            plugin.getGUIManager().openSGMenu(player);
        } else if (type == Material.IRON_SWORD && displayName.contains("Duel")) {
            event.setCancelled(true);
            plugin.getGUIManager().openDuelsMenu(player);
        } else if (type == Material.BOOK && displayName.contains("Stats") || displayName.contains("Statistik")) {
            event.setCancelled(true);
            plugin.getGUIManager().openStatsMenu(player);
        } else if (type == Material.EMERALD && displayName.contains("Leaderboard")) {
            event.setCancelled(true);
            plugin.getGUIManager().openTopMenu(player);
        } else if (type == Material.NAME_TAG && (displayName.contains("Language") || displayName.contains("Sprog") || displayName.contains("Sprache") || displayName.contains("Idioma"))) {
            event.setCancelled(true);
            plugin.getGUIManager().openLanguageMenu(player);
        } else if (type == Material.BED && displayName.contains("Leave") || displayName.contains("Forlad") || displayName.contains("verlassen") || displayName.contains("Dejar")) {
            event.setCancelled(true);
            if (plugin.getSGManager().isInGame(player)) {
                plugin.getSGManager().leaveGame(player);
            } else if (plugin.getDuelManager().isInDuel(player)) {
                plugin.getDuelManager().leaveQueue(player);
            }
        }
    }
}
