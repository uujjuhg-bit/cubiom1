package com.cubiom.utils;

import com.cubiom.Cubiom;
import com.cubiom.gamemodes.sg.SGGame;
import com.cubiom.language.LanguageManager;
import com.cubiom.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardManager {

    private final Cubiom plugin;
    private final Map<Player, Scoreboard> playerScoreboards;

    public ScoreboardManager(Cubiom plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
    }

    public void setLobbyScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("lobby", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        LanguageManager lang = plugin.getLanguageManager();
        objective.setDisplayName(lang.getMessage(player, "scoreboard.title"));

        PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());

        Map<String, String> replacements = new HashMap<>();
        replacements.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        replacements.put("sg_wins", String.valueOf(stats.getSgWins()));
        replacements.put("duel_elo", String.valueOf(stats.getDuelElo()));

        setScoreboardLines(scoreboard, objective, new String[]{
            lang.formatMessage(player, "scoreboard.lobby.line1", replacements),
            lang.formatMessage(player, "scoreboard.lobby.line2", replacements),
            lang.formatMessage(player, "scoreboard.lobby.line3", replacements),
            lang.formatMessage(player, "scoreboard.lobby.line4", replacements),
            lang.formatMessage(player, "scoreboard.lobby.line5", replacements),
            lang.formatMessage(player, "scoreboard.lobby.line6", replacements),
            lang.formatMessage(player, "scoreboard.lobby.line7", replacements),
            lang.formatMessage(player, "scoreboard.lobby.line8", replacements)
        });

        player.setScoreboard(scoreboard);
        playerScoreboards.put(player, scoreboard);
    }

    public void setSGWaitingScoreboard(Player player, int current, int min) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("sg_waiting", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        LanguageManager lang = plugin.getLanguageManager();
        objective.setDisplayName(lang.getMessage(player, "scoreboard.title"));

        Map<String, String> replacements = new HashMap<>();
        replacements.put("current", String.valueOf(current));
        replacements.put("min", String.valueOf(min));

        setScoreboardLines(scoreboard, objective, new String[]{
            lang.formatMessage(player, "scoreboard.sg-waiting.line1", replacements),
            lang.formatMessage(player, "scoreboard.sg-waiting.line2", replacements),
            lang.formatMessage(player, "scoreboard.sg-waiting.line3", replacements),
            lang.formatMessage(player, "scoreboard.sg-waiting.line4", replacements),
            lang.formatMessage(player, "scoreboard.sg-waiting.line5", replacements),
            lang.formatMessage(player, "scoreboard.sg-waiting.line6", replacements),
            lang.formatMessage(player, "scoreboard.sg-waiting.line7", replacements)
        });

        player.setScoreboard(scoreboard);
        playerScoreboards.put(player, scoreboard);
    }

    public void setSGGameScoreboard(Player player, SGGame game) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("sg_game", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        LanguageManager lang = plugin.getLanguageManager();
        objective.setDisplayName(lang.getMessage(player, "scoreboard.title"));

        int kills = game.getPlayerData(player) != null ? game.getPlayerData(player).getKills() : 0;

        Map<String, String> replacements = new HashMap<>();
        replacements.put("arena", game.getArena().getName());
        replacements.put("alive", String.valueOf(game.getAlivePlayers().size()));
        replacements.put("total", String.valueOf(game.getPlayers().size()));
        replacements.put("kills", String.valueOf(kills));

        setScoreboardLines(scoreboard, objective, new String[]{
            lang.formatMessage(player, "scoreboard.sg.line1", replacements),
            lang.formatMessage(player, "scoreboard.sg.line2", replacements),
            lang.formatMessage(player, "scoreboard.sg.line3", replacements),
            lang.formatMessage(player, "scoreboard.sg.line4", replacements),
            lang.formatMessage(player, "scoreboard.sg.line5", replacements),
            lang.formatMessage(player, "scoreboard.sg.line6", replacements),
            lang.formatMessage(player, "scoreboard.sg.line7", replacements),
            lang.formatMessage(player, "scoreboard.sg.line8", replacements)
        });

        player.setScoreboard(scoreboard);
        playerScoreboards.put(player, scoreboard);
    }

    private void setScoreboardLines(Scoreboard scoreboard, Objective objective, String[] lines) {
        int score = lines.length;
        for (String line : lines) {
            objective.getScore(line).setScore(score--);
        }
    }

    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void updateAll() {
        for (Player player : playerScoreboards.keySet()) {
            if (plugin.getSGManager().isInGame(player)) {
                SGGame game = plugin.getSGManager().getPlayerGame(player);
                if (game != null) {
                    setSGGameScoreboard(player, game);
                }
            } else {
                setLobbyScoreboard(player);
            }
        }
    }
}
