package com.cubiom.arena;

import com.cubiom.core.GameType;
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

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location deserializeLocation(String s) {
        String[] parts = s.split(",");
        return new Location(
            Bukkit.getWorld(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]),
            Float.parseFloat(parts[4]),
            Float.parseFloat(parts[5])
        );
    }
}
