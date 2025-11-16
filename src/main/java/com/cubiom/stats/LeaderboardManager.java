package com.cubiom.stats;

import com.cubiom.Cubiom;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final Cubiom plugin;

    public LeaderboardManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public List<Map.Entry<UUID, Integer>> getTopSGWins(int limit) {
        Map<UUID, Integer> winsMap = new HashMap<>();

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());
            winsMap.put(player.getUniqueId(), stats.getSgWins());
        }

        return winsMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Map.Entry<UUID, Integer>> getTopSGKills(int limit) {
        Map<UUID, Integer> killsMap = new HashMap<>();

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());
            killsMap.put(player.getUniqueId(), stats.getSgKills());
        }

        return killsMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Map.Entry<UUID, Integer>> getTopDuelElo(int limit) {
        Map<UUID, Integer> eloMap = new HashMap<>();

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());
            eloMap.put(player.getUniqueId(), stats.getDuelElo());
        }

        return eloMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public int getPlayerRank(UUID playerId, LeaderboardType type) {
        List<Map.Entry<UUID, Integer>> leaderboard;

        switch (type) {
            case SG_WINS:
                leaderboard = getTopSGWins(Integer.MAX_VALUE);
                break;
            case SG_KILLS:
                leaderboard = getTopSGKills(Integer.MAX_VALUE);
                break;
            case DUEL_ELO:
                leaderboard = getTopDuelElo(Integer.MAX_VALUE);
                break;
            default:
                return -1;
        }

        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getKey().equals(playerId)) {
                return i + 1;
            }
        }

        return -1;
    }

    public enum LeaderboardType {
        SG_WINS,
        SG_KILLS,
        DUEL_ELO
    }
}
