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
                    sendAdvancedMOTD(player);
                }
            }, 20L);
        }
    }

    private void sendAdvancedMOTD(Player player) {
        PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());
        int onlinePlayers = Bukkit.getOnlinePlayers().size();

        player.sendMessage("");
        player.sendMessage("§8§m                                                    ");
        player.sendMessage("");
        player.sendMessage("         §b§lCUBIOM §7§l» §fWelcome, §b" + player.getName() + "§f!");
        player.sendMessage("");
        player.sendMessage("  §7§l┃ §fServer §8» §b" + onlinePlayers + " §7online");
        player.sendMessage("  §7§l┃ §fYour Stats §8»");
        player.sendMessage("      §7§l▪ §fSG Wins: §b" + stats.getSgWins() + " §7§l▪ §fDuel ELO: §b" + stats.getDuelElo());
        player.sendMessage("      §7§l▪ §fSG Kills: §b" + stats.getSgKills() + " §7§l▪ §fDuel Wins: §b" + stats.getDuelWins());
        player.sendMessage("");
        player.sendMessage("  §e§l⚡ §eNew: §fBuildUHC & Combo kits now available!");
        player.sendMessage("  §e§l⚡ §eUse §b/stats §eto view detailed statistics");
        player.sendMessage("");
        player.sendMessage("§8§m                                                    ");
        player.sendMessage("");
    }
}
