package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Cubiom plugin;

    public PlayerJoinListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());
        stats.updateLastPlayed();

        String language = plugin.getDataManager().getPlayerLanguage(player.getUniqueId());
        if (language == null) {
            plugin.getDataManager().setPlayerLanguage(
                    player.getUniqueId(),
                    plugin.getConfigManager().getDefaultLanguage()
            );
        }

        if (!plugin.getSGManager().isInGame(player) && !plugin.getDuelManager().isInDuel(player)) {
            plugin.getHotbarManager().giveLobbyHotbar(player);
        }
    }
}
