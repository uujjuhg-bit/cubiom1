package com.cubiom.data;

import com.cubiom.Cubiom;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private final Cubiom plugin;
    private final Gson gson;
    private final File dataFolder;

    public DataManager(Cubiom plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = new File(plugin.getDataFolder(), "data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public JsonObject loadJSON(String filename) {
        File file = new File(dataFolder, filename);

        if (!file.exists()) {
            return new JsonObject();
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            return new JsonParser().parse(content).getAsJsonObject();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load " + filename + ": " + e.getMessage());
            return new JsonObject();
        }
    }

    public void saveJSON(String filename, JsonObject data) {
        File file = new File(dataFolder, filename);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, String> loadPlayerLanguages() {
        Map<String, String> languages = new HashMap<>();
        JsonObject data = loadJSON("player-languages.json");

        for (String uuid : data.keySet()) {
            languages.put(uuid, data.get(uuid).getAsString());
        }

        return languages;
    }

    public void savePlayerLanguages(Map<String, String> languages) {
        JsonObject data = new JsonObject();

        for (Map.Entry<String, String> entry : languages.entrySet()) {
            data.addProperty(entry.getKey(), entry.getValue());
        }

        saveJSON("player-languages.json", data);
    }
}
