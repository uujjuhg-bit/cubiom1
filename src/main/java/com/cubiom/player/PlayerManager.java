package com.cubiom.player;

import com.cubiom.Cubiom;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Cubiom plugin;
    private final Map<UUID, CubiomPlayer> players;

    public PlayerManager(Cubiom plugin) {
        this.plugin = plugin;
        this.players = new HashMap<>();
    }

    public CubiomPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public CubiomPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    public CubiomPlayer addPlayer(Player player) {
        CubiomPlayer cubiomPlayer = new CubiomPlayer(player);
        players.put(player.getUniqueId(), cubiomPlayer);
        return cubiomPlayer;
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public void removePlayer(Player player) {
        removePlayer(player.getUniqueId());
    }

    public Map<UUID, CubiomPlayer> getAllPlayers() {
        return players;
    }

    public void shutdown() {
        players.clear();
    }
}
