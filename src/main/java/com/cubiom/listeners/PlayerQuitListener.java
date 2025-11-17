package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final Cubiom plugin;

    public PlayerQuitListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);

        if (cp != null) {
            if (!cp.isInLobby()) {
                plugin.getSGManager().leaveGame(player);
                plugin.getDuelManager().leaveQueue(player);
            }

            plugin.getScoreboardManager().removeScoreboard(player);
            plugin.getPlayerManager().removePlayer(player);
        }

        event.setQuitMessage(plugin.getLanguageManager().getMessage("en_US", "quit-message")
            .replace("%player%", player.getName()));
    }
}
