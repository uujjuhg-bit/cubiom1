package com.cubiom.stats;

import com.cubiom.Cubiom;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {

    private final Cubiom plugin;
    private final Gson gson;
    private final File statsFile;
    private final Map<UUID, PlayerStats> playerStats;

    public StatsManager(Cubiom plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.statsFile = new File(plugin.getDataFolder() + "/data", "player-stats.json");
        this.playerStats = new HashMap<>();
    }

    public void load() {
        plugin.getLogger().info("Loading player stats...");

        if (!statsFile.exists()) {
            playerStats.clear();
            return;
        }

        try (Reader reader = new FileReader(statsFile)) {
            Type type = new TypeToken<Map<UUID, PlayerStats>>(){}.getType();
            Map<UUID, PlayerStats> loaded = gson.fromJson(reader, type);

            if (loaded != null) {
                playerStats.putAll(loaded);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load player-stats.json: " + e.getMessage());
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(statsFile)) {
            gson.toJson(playerStats, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save player-stats.json: " + e.getMessage());
        }
    }

    public PlayerStats getPlayerStats(UUID uuid) {
        return playerStats.computeIfAbsent(uuid, PlayerStats::new);
    }

    public void loadPlayerStats(UUID uuid) {
        playerStats.computeIfAbsent(uuid, PlayerStats::new);
    }

    public List<PlayerStats> getTopSGWins(int limit) {
        return playerStats.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getSgWins).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PlayerStats> getTopSGKills(int limit) {
        return playerStats.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getSgKills).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PlayerStats> getTopSGKDR(int limit) {
        return playerStats.values().stream()
                .sorted(Comparator.comparingDouble(PlayerStats::getSgKDR).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PlayerStats> getTopDuelElo(int limit) {
        return playerStats.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getDuelElo).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<PlayerStats> getTopDuelWins(int limit) {
        return playerStats.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getDuelWins).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public int calculateEloChange(int winnerElo, int loserElo, boolean isWinner) {
        int kFactor = plugin.getConfigManager().getDuelEloKFactor();
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, (loserElo - winnerElo) / 400.0));

        if (isWinner) {
            return (int) Math.round(kFactor * (1.0 - expectedScore));
        } else {
            return (int) Math.round(kFactor * (0.0 - (1.0 - expectedScore)));
        }
    }
}
