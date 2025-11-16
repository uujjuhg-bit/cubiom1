package com.cubiom.listeners;

import com.cubiom.Cubiom;
import org.bukkit.ChatColor;
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

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        Material type = item.getType();
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (type == Material.DIAMOND_SWORD) {
            plugin.getGUIManager().openSGMenu(player);
        }
        else if (type == Material.IRON_SWORD) {
            plugin.getGUIManager().openDuelsMenu(player);
        }
        else if (type == Material.PAPER) {
            plugin.getGUIManager().openStatsMenu(player);
        }
        else if (type == Material.EMERALD) {
            plugin.getGUIManager().openTopMenu(player);
        }
        else if (type == Material.NAME_TAG) {
            plugin.getGUIManager().openLanguageMenu(player);
        }
        else if (type == Material.BED) {
            if (plugin.getSGManager().isInGame(player)) {
                plugin.getSGManager().leaveGame(player);
            } else if (plugin.getDuelManager().isInDuel(player)) {
                plugin.getDuelManager().leaveQueue(player);
            }
        }
    }
}
