package com.cubiom.gamemodes.duels;

import com.cubiom.Cubiom;
import com.cubiom.arenas.DuelArena;
import com.cubiom.language.LanguageManager;
import com.cubiom.stats.PlayerStats;
import com.cubiom.stats.StatsManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelGame {

    private final Cubiom plugin;
    private final DuelArena arena;
    private final Player player1;
    private final Player player2;
    private final Kit kit;
    private DuelState state;
    private BukkitTask gameTask;
    private int countdown;

    public DuelGame(Cubiom plugin, DuelArena arena, Player player1, Player player2, Kit kit) {
        this.plugin = plugin;
        this.arena = arena;
        this.player1 = player1;
        this.player2 = player2;
        this.kit = kit;
        this.state = DuelState.STARTING;
        this.countdown = 5;
    }

    public void start() {
        arena.setInUse(true);

        player1.teleport(arena.getSpawn1());
        player2.teleport(arena.getSpawn2());

        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);

        kit.applyKit(player1);
        kit.applyKit(player2);

        player1.setHealth(20.0);
        player1.setFoodLevel(20);
        player2.setHealth(20.0);
        player2.setFoodLevel(20);

        LanguageManager langManager = plugin.getLanguageManager();

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    state = DuelState.FIGHTING;
                    player1.sendMessage(langManager.getMessageWithPrefix(player1, "duels.duel-started"));
                    player2.sendMessage(langManager.getMessageWithPrefix(player2, "duels.duel-started"));
                    cancel();
                    return;
                }

                Map<String, String> replacements = new HashMap<>();
                replacements.put("time", String.valueOf(countdown));

                player1.sendMessage(langManager.getMessage(player1, "general.prefix") +
                        langManager.formatMessage(player1, "duels.duel-starting", replacements));
                player2.sendMessage(langManager.getMessage(player2, "general.prefix") +
                        langManager.formatMessage(player2, "duels.duel-starting", replacements));

                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void handlePlayerDeath(Player loser) {
        if (state != DuelState.FIGHTING) {
            return;
        }

        state = DuelState.ENDING;

        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        Player winner = loser.equals(player1) ? player2 : player1;

        StatsManager statsManager = plugin.getStatsManager();
        PlayerStats winnerStats = statsManager.getPlayerStats(winner.getUniqueId());
        PlayerStats loserStats = statsManager.getPlayerStats(loser.getUniqueId());

        int eloChange = statsManager.calculateEloChange(winnerStats.getDuelElo(), loserStats.getDuelElo(), true);

        winnerStats.addDuelWin();
        winnerStats.addDuelElo(eloChange);

        loserStats.addDuelLoss();
        loserStats.addDuelElo(-eloChange);

        LanguageManager langManager = plugin.getLanguageManager();

        Map<String, String> winnerReplacements = new HashMap<>();
        winnerReplacements.put("player", winner.getName());
        winner.sendMessage(langManager.getMessage(winner, "general.prefix") +
                langManager.formatMessage(winner, "duels.winner", winnerReplacements));

        winnerReplacements.put("elo", String.valueOf(winnerStats.getDuelElo()));
        winnerReplacements.put("change", String.valueOf(eloChange));
        winner.sendMessage(langManager.formatMessage(winner, "duels.elo-change", winnerReplacements));

        Map<String, String> loserReplacements = new HashMap<>();
        loserReplacements.put("player", loser.getName());
        loser.sendMessage(langManager.getMessage(loser, "general.prefix") +
                langManager.formatMessage(loser, "duels.loser", loserReplacements));

        loserReplacements.put("elo", String.valueOf(loserStats.getDuelElo()));
        loserReplacements.put("change", String.valueOf(-eloChange));
        loser.sendMessage(langManager.formatMessage(loser, "duels.elo-change-loss", loserReplacements));

        cleanup();
    }

    public void cleanup() {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);

        player1.getInventory().clear();
        player2.getInventory().clear();

        arena.setInUse(false);
    }

    public DuelArena getArena() {
        return arena;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Kit getKit() {
        return kit;
    }

    public DuelState getState() {
        return state;
    }

    public boolean isInDuel(UUID uuid) {
        return player1.getUniqueId().equals(uuid) || player2.getUniqueId().equals(uuid);
    }

    private enum DuelState {
        STARTING,
        FIGHTING,
        ENDING
    }
}
