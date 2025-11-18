package com.cubiom.arena;

import com.cubiom.core.GameType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SGArena extends Arena {

    private final List<Location> playerSpawns;
    private final List<Location> tier1Chests;
    private final List<Location> tier2Chests;
    private final List<Location> deathmatchSpawns;
    private int minPlayers;
    private int maxPlayers;
    private boolean soloOnly;
    private final Map<String, Boolean> gameRules;
    private Location spectatorSpawn;

    public SGArena(String name) {
        super(name, GameType.SURVIVAL_GAMES);
        this.playerSpawns = new ArrayList<>();
        this.tier1Chests = new ArrayList<>();
        this.tier2Chests = new ArrayList<>();
        this.deathmatchSpawns = new ArrayList<>();
        this.gameRules = new HashMap<>();
        this.minPlayers = 8;
        this.maxPlayers = 24;
        this.soloOnly = true;

        gameRules.put("doMobSpawning", false);
        gameRules.put("doFireTick", false);
        gameRules.put("mobGriefing", false);
    }

    public void addPlayerSpawn(Location location) {
        playerSpawns.add(location);
    }

    public void addTier1Chest(Location location) {
        tier1Chests.add(location);
    }

    public void addTier2Chest(Location location) {
        tier2Chests.add(location);
    }

    public void addDeathmatchSpawn(Location location) {
        deathmatchSpawns.add(location);
    }

    public void setSpectatorSpawn(Location location) {
        this.spectatorSpawn = location;
    }

    public List<Location> getPlayerSpawns() {
        return playerSpawns;
    }

    public List<Location> getTier1Chests() {
        return tier1Chests;
    }

    public List<Location> getTier2Chests() {
        return tier2Chests;
    }

    public List<Location> getDeathmatchSpawns() {
        return deathmatchSpawns;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isSoloOnly() {
        return soloOnly;
    }

    public void setSoloOnly(boolean soloOnly) {
        this.soloOnly = soloOnly;
    }

    public Map<String, Boolean> getGameRules() {
        return gameRules;
    }

    public void setGameRule(String rule, boolean value) {
        gameRules.put(rule, value);
    }

    @Override
    public boolean isValid() {
        if (playerSpawns.size() < minPlayers) return false;
        if (tier1Chests.isEmpty()) return false;
        if (deathmatchSpawns.isEmpty()) return false;
        if (spectatorSpawn == null) return false;
        return true;
    }

    @Override
    public void saveToConfig() {
    }

    @Override
    public void loadFromConfig() {
    }

    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("worldName", worldName);
        json.addProperty("enabled", enabled);
        json.addProperty("minPlayers", minPlayers);
        json.addProperty("maxPlayers", maxPlayers);
        json.addProperty("soloOnly", soloOnly);

        JsonArray spawns = new JsonArray();
        for (Location loc : playerSpawns) {
            spawns.add(locationToJSON(loc));
        }
        json.add("spawnPoints", spawns);

        JsonArray tier1 = new JsonArray();
        for (Location loc : tier1Chests) {
            tier1.add(locationToJSON(loc));
        }
        json.add("tier1Chests", tier1);

        JsonArray tier2 = new JsonArray();
        for (Location loc : tier2Chests) {
            tier2.add(locationToJSON(loc));
        }
        json.add("tier2Chests", tier2);

        JsonArray dmSpawns = new JsonArray();
        for (Location loc : deathmatchSpawns) {
            dmSpawns.add(locationToJSON(loc));
        }
        json.add("deathmatchSpawns", dmSpawns);

        if (spectatorSpawn != null) {
            json.add("spectatorSpawn", locationToJSON(spectatorSpawn));
        }

        return json;
    }

    public static SGArena fromJSON(String name, JsonObject json) {
        try {
            SGArena arena = new SGArena(name);
            arena.worldName = json.get("worldName").getAsString();
            arena.enabled = json.get("enabled").getAsBoolean();
            arena.minPlayers = json.get("minPlayers").getAsInt();
            arena.maxPlayers = json.get("maxPlayers").getAsInt();
            arena.soloOnly = json.get("soloOnly").getAsBoolean();

            if (json.has("spawnPoints")) {
                JsonArray spawns = json.getAsJsonArray("spawnPoints");
                for (int i = 0; i < spawns.size(); i++) {
                    arena.playerSpawns.add(locationFromJSON(spawns.get(i).getAsJsonObject()));
                }
            }

            if (json.has("tier1Chests")) {
                JsonArray tier1 = json.getAsJsonArray("tier1Chests");
                for (int i = 0; i < tier1.size(); i++) {
                    arena.tier1Chests.add(locationFromJSON(tier1.get(i).getAsJsonObject()));
                }
            }

            if (json.has("tier2Chests")) {
                JsonArray tier2 = json.getAsJsonArray("tier2Chests");
                for (int i = 0; i < tier2.size(); i++) {
                    arena.tier2Chests.add(locationFromJSON(tier2.get(i).getAsJsonObject()));
                }
            }

            if (json.has("deathmatchSpawns")) {
                JsonArray dmSpawns = json.getAsJsonArray("deathmatchSpawns");
                for (int i = 0; i < dmSpawns.size(); i++) {
                    arena.deathmatchSpawns.add(locationFromJSON(dmSpawns.get(i).getAsJsonObject()));
                }
            }

            if (json.has("spectatorSpawn")) {
                arena.spectatorSpawn = locationFromJSON(json.getAsJsonObject("spectatorSpawn"));
            }

            return arena;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonObject locationToJSON(Location loc) {
        JsonObject json = new JsonObject();
        json.addProperty("world", loc.getWorld().getName());
        json.addProperty("x", loc.getX());
        json.addProperty("y", loc.getY());
        json.addProperty("z", loc.getZ());
        json.addProperty("yaw", loc.getYaw());
        json.addProperty("pitch", loc.getPitch());
        return json;
    }

    private static Location locationFromJSON(JsonObject json) {
        return new Location(
            Bukkit.getWorld(json.get("world").getAsString()),
            json.get("x").getAsDouble(),
            json.get("y").getAsDouble(),
            json.get("z").getAsDouble(),
            json.get("yaw").getAsFloat(),
            json.get("pitch").getAsFloat()
        );
    }
}
