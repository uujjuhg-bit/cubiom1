package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import com.cubiom.core.PlayerState;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);

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
        } else if (displayName.contains("Players:")) {
            togglePlayerVisibility(player, cp);
        } else if (displayName.contains("Leaderboards")) {
            plugin.getGUIManager().openLeaderboards(player);
        } else if (displayName.contains("Settings")) {
            plugin.getGUIManager().openSettings(player);
        }
    }

    private void togglePlayerVisibility(Player player, CubiomPlayer cp) {
        boolean newState = !cp.arePlayersVisible();
        cp.setPlayersVisible(newState);

        ItemStack item = player.getInventory().getItem(6);
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (newState) {
                item.setType(Material.INK_SACK);
                item.setDurability((short) 10);
                meta.setDisplayName(ChatColor.GREEN + "Players: Visible");
                meta.setLore(java.util.Arrays.asList(ChatColor.GRAY + "Click to hide players"));

                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!other.equals(player)) {
                        player.showPlayer(other);
                    }
                }

                player.sendMessage(ChatColor.GREEN + "✓ Players are now visible!");
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);
            } else {
                item.setType(Material.INK_SACK);
                item.setDurability((short) 8);
                meta.setDisplayName(ChatColor.RED + "Players: Hidden");
                meta.setLore(java.util.Arrays.asList(ChatColor.GRAY + "Click to show players"));

                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!other.equals(player)) {
                        player.hidePlayer(other);
                    }
                }

                player.sendMessage(ChatColor.RED + "✓ Players are now hidden!");
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 0.8f);
            }

            item.setItemMeta(meta);
        }
    }

    private void handleSpectatorClick(Player player, String displayName) {
        if (displayName.contains("Leave Game")) {
            plugin.getSGManager().leaveGame(player);
        }
    }
}
