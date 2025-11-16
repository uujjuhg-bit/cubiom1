package com.cubiom.arenas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Arena {

    private String name;
    private String worldName;
    private Location lobbySpawn;
    private List<Location> spawnPoints;
    private List<Location> tier1Chests;
    private List<Location> tier2Chests;
    private Location deathmatchSpawn;
    private int minPlayers;
    private int maxPlayers;
    private boolean enabled;

    public Arena(String name) {
        this.name = name;
        this.spawnPoints = new ArrayList<>();
        this.tier1Chests = new ArrayList<>();
        this.tier2Chests = new ArrayList<>();
        this.minPlayers = 8;
        this.maxPlayers = 24;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
        if (worldName == null && lobbySpawn != null) {
            this.worldName = lobbySpawn.getWorld().getName();
        }
    }

    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }

    public void addSpawnPoint(Location location) {
        this.spawnPoints.add(location);
    }

    public void clearSpawnPoints() {
        this.spawnPoints.clear();
    }

    public List<Location> getTier1Chests() {
        return tier1Chests;
    }

    public void addTier1Chest(Location location) {
        this.tier1Chests.add(location);
    }

    public void clearTier1Chests() {
        this.tier1Chests.clear();
    }

    public List<Location> getTier2Chests() {
        return tier2Chests;
    }

    public void addTier2Chest(Location location) {
        this.tier2Chests.add(location);
    }

    public void clearTier2Chests() {
        this.tier2Chests.clear();
    }

    public Location getDeathmatchSpawn() {
        return deathmatchSpawn;
    }

    public void setDeathmatchSpawn(Location deathmatchSpawn) {
        this.deathmatchSpawn = deathmatchSpawn;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isValid() {
        return worldName != null
                && lobbySpawn != null
                && !spawnPoints.isEmpty()
                && !tier1Chests.isEmpty()
                && deathmatchSpawn != null
                && minPlayers > 0
                && maxPlayers >= minPlayers;
    }

    public void setupArena() {
        if (worldName == null) {
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }

        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("showDeathMessages", "false");
        world.setGameRuleValue("keepInventory", "false");
        world.setGameRuleValue("naturalRegeneration", "true");
        world.setTime(6000);
        world.setStorm(false);
        world.setThundering(false);

        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv modify set animals false " + worldName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv modify set monsters false " + worldName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv modify set pvp true " + worldName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv modify set difficulty 2 " + worldName);
        }
    }
}
