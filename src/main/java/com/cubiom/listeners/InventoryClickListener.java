package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import com.cubiom.core.PlayerState;
import com.cubiom.game.duel.Kit;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.ChatColor;
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
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);

        if (cp == null) return;

        if (cp.getState() == PlayerState.LOBBY || cp.getState() == PlayerState.SPECTATING) {
            event.setCancelled(true);
        }

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        if (title.contains("Game Selector")) {
            event.setCancelled(true);
            if (displayName.contains("Survival Games")) {
                player.closeInventory();
                plugin.getGUIManager().openSGArenaSelector(player);
            } else if (displayName.contains("Duels")) {
                player.closeInventory();
                plugin.getGUIManager().openKitSelector(player);
            }
        } else if (title.contains("Select SG Arena")) {
            event.setCancelled(true);
            String arenaName = ChatColor.stripColor(displayName);
            SGArena arena = plugin.getArenaManager().getSGArena(arenaName);
            if (arena != null) {
                player.closeInventory();
                plugin.getSGManager().joinGame(player, arena.getName());
            }
        } else if (title.contains("Select Duel Kit")) {
            event.setCancelled(true);
            String kitName = ChatColor.stripColor(displayName).split(" ")[0].toLowerCase();
            Kit kit = Kit.getKit(kitName);
            if (kit != null) {
                player.closeInventory();
                plugin.getDuelManager().joinQueue(player, kit.getName().toLowerCase());
            }
        } else if (title.contains("Active Players")) {
            event.setCancelled(true);
        } else if (title.contains("Leaderboards")) {
            event.setCancelled(true);
        } else if (title.contains("Settings")) {
            event.setCancelled(true);
        } else if (title.contains("Profile")) {
            event.setCancelled(true);
        }
    }
}
