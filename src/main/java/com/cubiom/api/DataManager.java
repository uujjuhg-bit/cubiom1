package com.cubiom.api;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import com.cubiom.arenas.DuelArena;
import com.cubiom.gamemodes.duels.Kit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final Cubiom plugin;
    private final Gson gson;
    private final File dataFolder;

    private Map<String, Arena> arenas;
    private Map<String, DuelArena> duelArenas;
    private Map<String, Kit> kits;
    private Map<UUID, String> playerLanguages;

    public DataManager(Cubiom plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = new File(plugin.getDataFolder(), "data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.arenas = new HashMap<>();
        this.duelArenas = new HashMap<>();
        this.kits = new HashMap<>();
        this.playerLanguages = new HashMap<>();
    }

    public void load() {
        plugin.getLogger().info("Loading data files...");

        loadArenas();
        loadDuelArenas();
        loadKits();
        loadPlayerLanguages();
        loadDefaultKits();
    }

    public void save() {
        saveArenas();
        saveDuelArenas();
        saveKits();
        savePlayerLanguages();
    }

    private void loadArenas() {
        File file = new File(dataFolder, "arenas.json");
        if (!file.exists()) {
            arenas = new HashMap<>();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Arena>>(){}.getType();
            arenas = gson.fromJson(reader, type);
            if (arenas == null) {
                arenas = new HashMap<>();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load arenas.json: " + e.getMessage());
            arenas = new HashMap<>();
        }
    }

    private void saveArenas() {
        File file = new File(dataFolder, "arenas.json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(arenas, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save arenas.json: " + e.getMessage());
        }
    }

    private void loadDuelArenas() {
        File file = new File(dataFolder, "duel-arenas.json");
        if (!file.exists()) {
            duelArenas = new HashMap<>();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, DuelArena>>(){}.getType();
            duelArenas = gson.fromJson(reader, type);
            if (duelArenas == null) {
                duelArenas = new HashMap<>();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load duel-arenas.json: " + e.getMessage());
            duelArenas = new HashMap<>();
        }
    }

    private void saveDuelArenas() {
        File file = new File(dataFolder, "duel-arenas.json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(duelArenas, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save duel-arenas.json: " + e.getMessage());
        }
    }

    private void loadKits() {
        File file = new File(dataFolder, "kits.json");
        if (!file.exists()) {
            kits = new HashMap<>();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Kit>>(){}.getType();
            kits = gson.fromJson(reader, type);
            if (kits == null) {
                kits = new HashMap<>();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load kits.json: " + e.getMessage());
            kits = new HashMap<>();
        }
    }

    private void saveKits() {
        File file = new File(dataFolder, "kits.json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(kits, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save kits.json: " + e.getMessage());
        }
    }

    private void loadPlayerLanguages() {
        File file = new File(dataFolder, "player-languages.json");
        if (!file.exists()) {
            playerLanguages = new HashMap<>();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<UUID, String>>(){}.getType();
            playerLanguages = gson.fromJson(reader, type);
            if (playerLanguages == null) {
                playerLanguages = new HashMap<>();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load player-languages.json: " + e.getMessage());
            playerLanguages = new HashMap<>();
        }
    }

    private void savePlayerLanguages() {
        File file = new File(dataFolder, "player-languages.json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(playerLanguages, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save player-languages.json: " + e.getMessage());
        }
    }

    private void loadDefaultKits() {
        if (kits.isEmpty()) {
            kits.put("Classic", Kit.createClassicKit());
            kits.put("NoDebuff", Kit.createNoDebuffKit());
            plugin.getLogger().info("Created default kits");
        }
    }

    public Map<String, Arena> getArenas() {
        return arenas;
    }

    public Map<String, DuelArena> getDuelArenas() {
        return duelArenas;
    }

    public Map<String, Kit> getKits() {
        return kits;
    }

    public Map<UUID, String> getPlayerLanguages() {
        return playerLanguages;
    }

    public String getPlayerLanguage(UUID uuid) {
        return playerLanguages.getOrDefault(uuid, plugin.getConfigManager().getDefaultLanguage());
    }

    public void setPlayerLanguage(UUID uuid, String language) {
        playerLanguages.put(uuid, language);
    }
}
