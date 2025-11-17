package com.cubiom.arena;

import com.cubiom.Cubiom;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaSetupManager {

    private final Cubiom plugin;
    private final Map<UUID, ArenaSetupSession> sessions;

    public ArenaSetupManager(Cubiom plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
    }

    public void startSession(Player player, String arenaName, ArenaSetupSession.ArenaType type) {
        ArenaSetupSession session = new ArenaSetupSession(player, arenaName, type, plugin);
        sessions.put(player.getUniqueId(), session);
    }

    public void endSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public ArenaSetupSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void shutdown() {
        sessions.clear();
    }
}
