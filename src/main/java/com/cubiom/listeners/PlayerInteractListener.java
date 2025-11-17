package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import com.cubiom.core.PlayerState;
import com.cubiom.player.CubiomPlayer;
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
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);

        if (cp == null) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;

        String displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
            item.getItemMeta().getDisplayName() : "";

        if (cp.getState() == PlayerState.LOBBY) {
            handleLobbyClick(player, displayName);
        } else if (cp.getState() == PlayerState.SPECTATING) {
            handleSpectatorClick(player, displayName);
        }
    }

    private void handleLobbyClick(Player player, String displayName) {
        if (displayName.contains("Game Selector")) {
            plugin.getGUIManager().openGameSelector(player);
        } else if (displayName.contains("Quick Play SG")) {
            SGArena arena = plugin.getArenaManager().getEnabledSGArenas().stream()
                .findFirst()
                .orElse(null);
            if (arena != null) {
                plugin.getSGManager().joinGame(player, arena.getName());
            } else {
                player.sendMessage(ChatColor.RED + "No arenas available!");
            }
        } else if (displayName.contains("Quick Play Duels")) {
            plugin.getGUIManager().openKitSelector(player);
        } else if (displayName.contains("Your Profile")) {
            plugin.getGUIManager().openPlayerProfile(player);
        } else if (displayName.contains("Active Players")) {
            plugin.getGUIManager().openActivePlayers(player);
        } else if (displayName.contains("Leaderboards")) {
            plugin.getGUIManager().openLeaderboards(player);
        } else if (displayName.contains("Settings")) {
            plugin.getGUIManager().openSettings(player);
        }
    }

    private void handleSpectatorClick(Player player, String displayName) {
        if (displayName.contains("Leave Game")) {
            plugin.getSGManager().leaveGame(player);
        }
    }
}
