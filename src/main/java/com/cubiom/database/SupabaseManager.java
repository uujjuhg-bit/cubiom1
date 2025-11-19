package com.cubiom.database;

import com.cubiom.Cubiom;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupabaseManager {

    private final Cubiom plugin;
    private final String supabaseUrl;
    private final String supabaseKey;
    private final Gson gson;
    private final ExecutorService executor;
    private final Map<String, Object> cache;

    public SupabaseManager(Cubiom plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.executor = Executors.newFixedThreadPool(4);
        this.cache = new HashMap<>();

        String url = "";
        String key = "";

        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();

            url = dotenv.get("SUPABASE_URL");
            key = dotenv.get("SUPABASE_ANON_KEY");

            if (url == null || key == null) {
                plugin.getLogger().warning("Supabase credentials not found in .env file!");
                plugin.getLogger().warning("Please create a .env file in the server root with:");
                plugin.getLogger().warning("SUPABASE_URL=your_url");
                plugin.getLogger().warning("SUPABASE_ANON_KEY=your_key");
                url = "";
                key = "";
            } else {
                plugin.getLogger().info("Supabase Manager initialized successfully");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load .env file: " + e.getMessage());
            plugin.getLogger().warning("Please create a .env file in the server root directory");
        }

        this.supabaseUrl = url;
        this.supabaseKey = key;
    }

    public CompletableFuture<JsonObject> executeQuery(String table, String operation, JsonObject data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/" + table;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod(operation);
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=representation");

                if (data != null) {
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = gson.toJson(data).getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }

                int responseCode = conn.getResponseCode();
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                );

                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                if (responseCode >= 400) {
                    plugin.getLogger().warning("Supabase error: " + response.toString());
                    return null;
                }

                return gson.fromJson(response.toString(), JsonObject.class);

            } catch (Exception e) {
                plugin.getLogger().severe("Supabase query failed: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }, executor);
    }

    public CompletableFuture<Void> upsertPlayer(String uuid, String username, String language) {
        return CompletableFuture.runAsync(() -> {
            JsonObject data = new JsonObject();
            data.addProperty("uuid", uuid);
            data.addProperty("username", username);
            data.addProperty("language", language);
            data.addProperty("last_seen", System.currentTimeMillis());

            executeQuery("players", "POST", data).join();
        }, executor);
    }

    public CompletableFuture<Void> updateSGStats(String uuid, String playerName, int wins, int kills, int deaths, int gamesPlayed,
                                                   int soloWins, int soloKills, int soloDeaths, int soloGames,
                                                   int teamWins, int teamKills, int teamDeaths, int teamGames) {
        return CompletableFuture.runAsync(() -> {
            JsonObject data = new JsonObject();
            data.addProperty("player_uuid", uuid);
            data.addProperty("player_name", playerName);
            data.addProperty("wins", wins);
            data.addProperty("kills", kills);
            data.addProperty("deaths", deaths);
            data.addProperty("games_played", gamesPlayed);
            data.addProperty("solo_wins", soloWins);
            data.addProperty("solo_kills", soloKills);
            data.addProperty("solo_deaths", soloDeaths);
            data.addProperty("solo_games_played", soloGames);
            data.addProperty("team_wins", teamWins);
            data.addProperty("team_kills", teamKills);
            data.addProperty("team_deaths", teamDeaths);
            data.addProperty("team_games_played", teamGames);
            data.addProperty("updated_at", System.currentTimeMillis());

            executeQuery("sg_stats", "POST", data).join();
        }, executor);
    }

    public CompletableFuture<Void> updateDuelStats(String uuid, String playerName, String kitType, int wins, int losses,
                                                     int elo, int gamesPlayed, int winStreak, int bestWinStreak) {
        return CompletableFuture.runAsync(() -> {
            JsonObject data = new JsonObject();
            data.addProperty("player_uuid", uuid);
            data.addProperty("player_name", playerName);
            data.addProperty("kit_type", kitType);
            data.addProperty("wins", wins);
            data.addProperty("losses", losses);
            data.addProperty("elo", elo);
            data.addProperty("games_played", gamesPlayed);
            data.addProperty("win_streak", winStreak);
            data.addProperty("best_win_streak", bestWinStreak);
            data.addProperty("updated_at", System.currentTimeMillis());

            executeQuery("duel_stats", "POST", data).join();
        }, executor);
    }

    public CompletableFuture<JsonObject> loadSGStats(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/sg_stats?player_uuid=eq." + uuid;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                if (responseStr.startsWith("[") && responseStr.length() > 2) {
                    responseStr = responseStr.substring(1, responseStr.length() - 1);
                    return gson.fromJson(responseStr, JsonObject.class);
                }

                return new JsonObject();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load SG stats for " + uuid);
                return new JsonObject();
            }
        }, executor);
    }

    public CompletableFuture<JsonObject> loadDuelStats(String uuid, String kitType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/duel_stats?player_uuid=eq." + uuid + "&kit_type=eq." + kitType;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                if (responseStr.startsWith("[") && responseStr.length() > 2) {
                    responseStr = responseStr.substring(1, responseStr.length() - 1);
                    return gson.fromJson(responseStr, JsonObject.class);
                }

                return new JsonObject();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load duel stats for " + uuid + " kit " + kitType);
                return new JsonObject();
            }
        }, executor);
    }

    public CompletableFuture<JsonObject> loadPlayerData(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/players?uuid=eq." + uuid;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                if (responseStr.startsWith("[") && responseStr.length() > 2) {
                    responseStr = responseStr.substring(1, responseStr.length() - 1);
                    return gson.fromJson(responseStr, JsonObject.class);
                }

                return new JsonObject();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load player data for " + uuid);
                return new JsonObject();
            }
        }, executor);
    }

    public CompletableFuture<List<JsonObject>> getTopSGWins(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/sg_stats?order=wins.desc&limit=" + limit;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                JsonArray jsonArray = gson.fromJson(responseStr, JsonArray.class);
                List<JsonObject> result = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    result.add(jsonArray.get(i).getAsJsonObject());
                }
                return result;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to fetch top SG wins: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executor);
    }

    public CompletableFuture<List<JsonObject>> getTopDuelElo(String kitType, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/duel_stats?kit_type=eq." + kitType + "&order=elo.desc&limit=" + limit;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                JsonArray jsonArray = gson.fromJson(responseStr, JsonArray.class);
                List<JsonObject> result = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    result.add(jsonArray.get(i).getAsJsonObject());
                }
                return result;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to fetch top duel ELO for " + kitType + ": " + e.getMessage());
                return new ArrayList<>();
            }
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
        cache.clear();
    }

    public Map<String, Object> getCache() {
        return cache;
    }

    public CompletableFuture<JsonObject> getPlayerProfile(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/rpc/get_player_profile";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);
                conn.setRequestProperty("Content-Type", "application/json");

                JsonObject params = new JsonObject();
                params.addProperty("p_uuid", uuid);

                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = gson.toJson(params).getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return gson.fromJson(response.toString(), JsonObject.class);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load player profile for " + uuid + ": " + e.getMessage());
                return new JsonObject();
            }
        }, executor);
    }

    public CompletableFuture<JsonObject> getPlayerByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/players?username=ilike." + username + "*";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                if (responseStr.startsWith("[") && responseStr.length() > 2) {
                    responseStr = responseStr.substring(1, responseStr.length() - 1);
                    return gson.fromJson(responseStr, JsonObject.class);
                }

                return new JsonObject();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to search player by username: " + username);
                return new JsonObject();
            }
        }, executor);
    }

    public CompletableFuture<Integer> getPlayerRank(String uuid, String statType, String kit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/rpc/get_player_rank";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);
                conn.setRequestProperty("Content-Type", "application/json");

                JsonObject params = new JsonObject();
                params.addProperty("p_uuid", uuid);
                params.addProperty("stat_type", statType);
                if (kit != null) {
                    params.addProperty("kit", kit);
                }

                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = gson.toJson(params).getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return Integer.parseInt(response.toString());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get player rank: " + e.getMessage());
                return 0;
            }
        }, executor);
    }

    public CompletableFuture<JsonObject> getServerStatistics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/rpc/get_server_statistics";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);
                conn.setRequestProperty("Content-Type", "application/json");

                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write("{}".getBytes(StandardCharsets.UTF_8));
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return gson.fromJson(response.toString(), JsonObject.class);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get server statistics: " + e.getMessage());
                return new JsonObject();
            }
        }, executor);
    }

    public CompletableFuture<Void> saveMatchHistory(String gameType, String arenaName, String winnerUuid,
                                                      String winnerName, JsonArray participants, int durationSeconds,
                                                      JsonObject statsJson) {
        return CompletableFuture.runAsync(() -> {
            JsonObject data = new JsonObject();
            data.addProperty("game_type", gameType);
            data.addProperty("arena_name", arenaName);
            data.addProperty("winner_uuid", winnerUuid);
            data.addProperty("winner_name", winnerName);
            data.add("participants", participants);
            data.addProperty("duration_seconds", durationSeconds);
            data.add("stats_json", statsJson);

            executeQuery("match_history", "POST", data).join();
        }, executor);
    }

    public CompletableFuture<List<JsonObject>> getRecentMatches(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlString = supabaseUrl + "/rest/v1/match_history?order=ended_at.desc&limit=" + limit;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", supabaseKey);
                conn.setRequestProperty("Authorization", "Bearer " + supabaseKey);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseStr = response.toString();
                JsonArray jsonArray = gson.fromJson(responseStr, JsonArray.class);
                List<JsonObject> result = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    result.add(jsonArray.get(i).getAsJsonObject());
                }
                return result;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to fetch recent matches: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executor);
    }

    public CompletableFuture<Void> saveChallengeHistory(String challengerUuid, String challengerName,
                                                          String challengedUuid, String challengedName,
                                                          String kitType, String status, String winnerUuid) {
        return CompletableFuture.runAsync(() -> {
            JsonObject data = new JsonObject();
            data.addProperty("challenger_uuid", challengerUuid);
            data.addProperty("challenger_name", challengerName);
            data.addProperty("challenged_uuid", challengedUuid);
            data.addProperty("challenged_name", challengedName);
            data.addProperty("kit_type", kitType);
            data.addProperty("status", status);
            if (winnerUuid != null) {
                data.addProperty("winner_uuid", winnerUuid);
                data.addProperty("completed_at", System.currentTimeMillis());
            }

            executeQuery("duel_challenges", "POST", data).join();
        }, executor);
    }

    public CompletableFuture<Void> updateArenaStatistics(String arenaName, String gameType, int totalGames,
                                                           int totalPlayers, int avgDuration, int popularityScore) {
        return CompletableFuture.runAsync(() -> {
            JsonObject data = new JsonObject();
            data.addProperty("arena_name", arenaName);
            data.addProperty("game_type", gameType);
            data.addProperty("total_games", totalGames);
            data.addProperty("total_players", totalPlayers);
            data.addProperty("average_duration_seconds", avgDuration);
            data.addProperty("popularity_score", popularityScore);
            data.addProperty("last_used", System.currentTimeMillis());
            data.addProperty("updated_at", System.currentTimeMillis());

            executeQuery("arena_statistics", "POST", data).join();
        }, executor);
    }
}
