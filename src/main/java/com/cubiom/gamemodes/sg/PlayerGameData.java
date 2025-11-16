package com.cubiom.gamemodes.sg;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerGameData {

    private final UUID uuid;
    private final Player player;
    private boolean alive;
    private int kills;
    private Location spawnLocation;
    private UUID lastDamager;
    private Location previousLocation;
    private String previousWorld;

    public PlayerGameData(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.alive = true;
        this.kills = 0;
        this.previousLocation = player.getLocation().clone();
        this.previousWorld = player.getWorld().getName();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public UUID getLastDamager() {
        return lastDamager;
    }

    public void setLastDamager(UUID lastDamager) {
        this.lastDamager = lastDamager;
    }

    public Location getPreviousLocation() {
        return previousLocation;
    }

    public void setPreviousLocation(Location previousLocation) {
        this.previousLocation = previousLocation;
    }

    public String getPreviousWorld() {
        return previousWorld;
    }

    public void setPreviousWorld(String previousWorld) {
        this.previousWorld = previousWorld;
    }
}
