package com.cubiom.database;

import com.cubiom.Cubiom;
import com.google.gson.Gson;
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

        Dotenv dotenv = Dotenv.configure()
                .directory(plugin.getDataFolder().getParent() + "/..")
                .filename(".env")
                .load();

        this.supabaseUrl = dotenv.get("SUPABASE_URL");
        this.supabaseKey = dotenv.get("SUPABASE_ANON_KEY");

        plugin.getLogger().info("Supabase Manager initialized");
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

    public void shutdown() {
        executor.shutdown();
        cache.clear();
    }

    public Map<String, Object> getCache() {
        return cache;
    }
}
