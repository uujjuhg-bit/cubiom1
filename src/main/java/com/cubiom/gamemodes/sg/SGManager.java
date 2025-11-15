package com.cubiom.gamemodes.sg;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import org.bukkit.entity.Player;

import java.util.*;

public class SGManager {

    private final Cubiom plugin;
    private final Map<String, SGGame> activeGames;
    private final Map<UUID, SGGame> playerGames;

    public SGManager(Cubiom plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
        this.playerGames = new HashMap<>();
    }

    public boolean joinGame(Player player) {
        if (isInGame(player)) {
            return false;
        }

        SGGame game = findAvailableGame();

        if (game == null) {
            return false;
        }

        if (game.addPlayer(player)) {
            playerGames.put(player.getUniqueId(), game);
            return true;
        }

        return false;
    }

    public void leaveGame(Player player) {
        SGGame game = playerGames.get(player.getUniqueId());

        if (game != null) {
            game.removePlayer(player);
            playerGames.remove(player.getUniqueId());
        }
    }

    public boolean isInGame(Player player) {
        return playerGames.containsKey(player.getUniqueId());
    }

    public SGGame getPlayerGame(Player player) {
        return playerGames.get(player.getUniqueId());
    }

    private SGGame findAvailableGame() {
        for (SGGame game : activeGames.values()) {
            GameState state = game.getState();
            if ((state == GameState.LOBBY || state == GameState.COUNTDOWN)
                    && game.getPlayers().size() < game.getArena().getMaxPlayers()) {
                return game;
            }
        }

        Arena arena = findAvailableArena();
        if (arena != null) {
            SGGame newGame = new SGGame(plugin, arena);
            activeGames.put(arena.getName(), newGame);
            return newGame;
        }

        return null;
    }

    private Arena findAvailableArena() {
        Map<String, Arena> arenas = plugin.getDataManager().getArenas();

        for (Arena arena : arenas.values()) {
            if (arena.isEnabled() && arena.isValid() && !activeGames.containsKey(arena.getName())) {
                return arena;
            }
        }

        return null;
    }

    public void shutdown() {
        for (SGGame game : activeGames.values()) {
            for (UUID uuid : new ArrayList<>(game.getPlayers().keySet())) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null) {
                    game.removePlayer(player);
                }
            }
        }
        activeGames.clear();
        playerGames.clear();
    }

    public Map<String, SGGame> getActiveGames() {
        return activeGames;
    }
}
