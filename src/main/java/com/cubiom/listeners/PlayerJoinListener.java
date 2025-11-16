package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.World;
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

        plugin.getStatsManager().loadPlayerStats(player.getUniqueId());

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
            World lobbyWorld = Bukkit.getWorld("lobby");
            if (lobbyWorld != null) {
                player.teleport(lobbyWorld.getSpawnLocation());
            }

            plugin.getHotbarManager().giveHotbarItems(player);

            plugin.getScoreboardManager().setLobbyScoreboard(player);

            plugin.getTabListManager().updateTabList(player);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    String motd = plugin.getLanguageManager().getMessage(player, "motd.join");
                    player.sendMessage(motd);
                }
            }, 20L);
        }
    }
}
