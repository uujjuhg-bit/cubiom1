package com.cubiom.game.sg;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SGManager {

    private final Cubiom plugin;
    private final Map<String, SGGame> activeGames;

    public SGManager(Cubiom plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
    }

    public SGGame createGame(SGArena arena) {
        if (activeGames.containsKey(arena.getName())) {
            return activeGames.get(arena.getName());
        }

        SGGame game = new SGGame(plugin, arena);
        activeGames.put(arena.getName(), game);
        return game;
    }

    public void joinGame(Player player, String arenaName) {
        SGGame game = activeGames.get(arenaName);
        if (game == null) {
            SGArena arena = plugin.getArenaManager().getSGArena(arenaName);
            if (arena == null || !arena.isEnabled()) {
                player.sendMessage("§cArena not found or disabled!");
                return;
            }
            game = createGame(arena);
        }

        game.addPlayer(player);
    }

    public void leaveGame(Player player) {
        for (SGGame game : activeGames.values()) {
            if (game.getPlayers().contains(player.getUniqueId())) {
                game.removePlayer(player);
                return;
            }
        }
    }

    public SGGame getPlayerGame(Player player) {
        for (SGGame game : activeGames.values()) {
            if (game.getPlayers().contains(player.getUniqueId())) {
                return game;
            }
        }
        return null;
    }

    public void removeGame(String arenaName) {
        activeGames.remove(arenaName);
    }

    public Map<String, SGGame> getActiveGames() {
        return activeGames;
    }

    public void shutdown() {
        for (SGGame game : activeGames.values()) {
            for (java.util.UUID uuid : game.getPlayers()) {
                Player player = org.bukkit.Bukkit.getPlayer(uuid);
                if (player != null) {
                    game.removePlayer(player);
                }
            }
        }
        activeGames.clear();
    }
}
