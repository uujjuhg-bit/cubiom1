package com.cubiom.player;

import com.cubiom.core.PlayerState;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CubiomPlayer {

    private final UUID uuid;
    private final Player bukkitPlayer;
    private PlayerState state;
    private String currentArena;
    private String selectedKit;
    private String language;
    private boolean playersVisible;
    private final Map<String, Integer> stats;
    private final Map<String, Long> cooldowns;

    public CubiomPlayer(Player bukkitPlayer) {
        this.uuid = bukkitPlayer.getUniqueId();
        this.bukkitPlayer = bukkitPlayer;
        this.state = PlayerState.LOBBY;
        this.stats = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.language = "en_US";
        this.playersVisible = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public String getCurrentArena() {
        return currentArena;
    }

    public void setCurrentArena(String arena) {
        this.currentArena = arena;
    }

    public String getSelectedKit() {
        return selectedKit;
    }

    public void setSelectedKit(String kit) {
        this.selectedKit = kit;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean hasCooldown(String type) {
        Long cooldown = cooldowns.get(type);
        if (cooldown == null) return false;
        return System.currentTimeMillis() < cooldown;
    }

    public void setCooldown(String type, long duration) {
        cooldowns.put(type, System.currentTimeMillis() + duration);
    }

    public long getRemainingCooldown(String type) {
        Long cooldown = cooldowns.get(type);
        if (cooldown == null) return 0;
        long remaining = cooldown - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    public boolean isInLobby() {
        return state == PlayerState.LOBBY;
    }

    public boolean isInGame() {
        return state == PlayerState.IN_GAME;
    }

    public boolean isSpectating() {
        return state == PlayerState.SPECTATING;
    }

    public boolean arePlayersVisible() {
        return playersVisible;
    }

    public void setPlayersVisible(boolean visible) {
        this.playersVisible = visible;
    }
}
