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

    public CompletableFuture<Void> updateSGStats(String uuid, int wins, int kills, int deaths, int gamesPlayed) {
        return CompletableFuture.runAsync(() -> {
            JsonObject data = new JsonObject();
            data.addProperty("player_uuid", uuid);
            data.addProperty("wins", wins);
            data.addProperty("kills", kills);
            data.addProperty("deaths", deaths);
            data.addProperty("games_played", gamesPlayed);
            data.addProperty("updated_at", System.currentTimeMillis());

            executeQuery("sg_stats", "POST", data).join();
        }, executor);
    }

    public CompletableFuture<Void> updateDuelStats(String uuid, String kitType, int wins, int losses,
                                                     int elo, int gamesPlayed, int winStreak, int bestWinStreak) {
        return CompletableFuture.runAsync(() -> {
            JsonObject data = new JsonObject();
            data.addProperty("player_uuid", uuid);
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
}
