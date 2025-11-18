package com.cubiom.arena;

import com.cubiom.Cubiom;
import com.cubiom.core.GameType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class ArenaManager {

    private final Cubiom plugin;
    private final Map<String, SGArena> sgArenas;
    private final Map<String, DuelArena> duelArenas;

    private final Gson gson;

    public ArenaManager(Cubiom plugin) {
        this.plugin = plugin;
        this.sgArenas = new HashMap<>();
        this.duelArenas = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void loadArenas() {
        loadSGArenas();
        loadDuelArenas();
        plugin.getLogger().info("Loaded " + sgArenas.size() + " SG arenas and " + duelArenas.size() + " Duel arenas");
    }

    private void loadSGArenas() {
        File file = new File(plugin.getDataFolder(), "data/arenas.json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String name = entry.getKey();
                SGArena arena = SGArena.fromJSON(name, entry.getValue().getAsJsonObject());
                if (arena != null) {
                    sgArenas.put(name, arena);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load SG arenas: " + e.getMessage());
        }
    }

    private void loadDuelArenas() {
        File file = new File(plugin.getDataFolder(), "data/duel-arenas.json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String name = entry.getKey();
                DuelArena arena = DuelArena.fromJSON(name, entry.getValue().getAsJsonObject());
                if (arena != null) {
                    duelArenas.put(name, arena);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load duel arenas: " + e.getMessage());
        }
    }

    public void saveAllArenas() {
        saveSGArenas();
        saveDuelArenas();
    }

    private void saveSGArenas() {
        File file = new File(plugin.getDataFolder(), "data/arenas.json");
        file.getParentFile().mkdirs();

        JsonObject json = new JsonObject();
        for (Map.Entry<String, SGArena> entry : sgArenas.entrySet()) {
            json.add(entry.getKey(), entry.getValue().toJSON());
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save SG arenas: " + e.getMessage());
        }
    }

    private void saveDuelArenas() {
        File file = new File(plugin.getDataFolder(), "data/duel-arenas.json");
        file.getParentFile().mkdirs();

        JsonObject json = new JsonObject();
        for (Map.Entry<String, DuelArena> entry : duelArenas.entrySet()) {
            json.add(entry.getKey(), entry.getValue().toJSON());
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save duel arenas: " + e.getMessage());
        }
    }

    public SGArena getSGArena(String name) {
        return sgArenas.get(name);
    }

    public DuelArena getDuelArena(String name) {
        return duelArenas.get(name);
    }

    public void addSGArena(SGArena arena) {
        sgArenas.put(arena.getName(), arena);
        saveSGArenas();
    }

    public void addDuelArena(DuelArena arena) {
        duelArenas.put(arena.getName(), arena);
        saveDuelArenas();
    }

    public void removeSGArena(String name) {
        sgArenas.remove(name);
        saveSGArenas();
    }

    public void removeDuelArena(String name) {
        duelArenas.remove(name);
        saveDuelArenas();
    }

    public List<SGArena> getAllSGArenas() {
        return new ArrayList<>(sgArenas.values());
    }

    public List<DuelArena> getAllDuelArenas() {
        return new ArrayList<>(duelArenas.values());
    }

    public List<SGArena> getEnabledSGArenas() {
        List<SGArena> enabled = new ArrayList<>();
        for (SGArena arena : sgArenas.values()) {
            if (arena.isEnabled()) enabled.add(arena);
        }
        return enabled;
    }

    public List<DuelArena> getAvailableDuelArenas() {
        List<DuelArena> available = new ArrayList<>();
        for (DuelArena arena : duelArenas.values()) {
            if (arena.isEnabled() && !arena.isInUse()) available.add(arena);
        }
        return available;
    }

    public DuelArena getRandomAvailableDuelArena() {
        List<DuelArena> available = getAvailableDuelArenas();
        if (available.isEmpty()) return null;
        return available.get(new Random().nextInt(available.size()));
    }
}
