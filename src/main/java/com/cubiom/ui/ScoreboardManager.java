package com.cubiom.ui;

import com.cubiom.Cubiom;
import com.cubiom.core.PlayerState;
import com.cubiom.game.duel.DuelGame;
import com.cubiom.game.sg.SGGame;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final Cubiom plugin;
    private final Map<UUID, Scoreboard> boards;

    public ScoreboardManager(Cubiom plugin) {
        this.plugin = plugin;
        this.boards = new HashMap<>();
        startUpdater();
    }

    public void createScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("cubiom", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "CUBIOM");

        boards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) return;

        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        if (cp == null) return;

        Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
        if (obj == null) return;

        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        PlayerState state = cp.getState();

        if (state == PlayerState.LOBBY) {
            updateLobbyScoreboard(player, obj);
        } else if (state == PlayerState.IN_GAME) {
            SGGame sgGame = plugin.getSGManager().getPlayerGame(player);
            if (sgGame != null) {
                updateSGScoreboard(player, obj, sgGame);
            } else {
                DuelGame duelGame = plugin.getDuelManager().getPlayerGame(player);
                if (duelGame != null) {
                    updateDuelScoreboard(player, obj, duelGame);
                }
            }
        } else if (state == PlayerState.SPECTATING) {
            SGGame sgGame = plugin.getSGManager().getPlayerGame(player);
            if (sgGame != null) {
                updateSpectatorScoreboard(player, obj, sgGame);
            }
        }
    }

    private void updateLobbyScoreboard(Player player, Objective obj) {
        setScore(obj, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------", 10);
        setScore(obj, ChatColor.WHITE + "Online: " + ChatColor.GREEN + Bukkit.getOnlinePlayers().size(), 9);
        setScore(obj, "", 8);
        setScore(obj, ChatColor.YELLOW + "Your Stats:", 7);
        setScore(obj, ChatColor.WHITE + "SG Wins: " + ChatColor.GREEN + "0", 6);
        setScore(obj, ChatColor.WHITE + "Duel ELO: " + ChatColor.GREEN + "1000", 5);
        setScore(obj, " ", 4);
        setScore(obj, ChatColor.GRAY + "cubiom.net", 3);
        setScore(obj, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------", 2);
    }

    private void updateSGScoreboard(Player player, Objective obj, SGGame game) {
        setScore(obj, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------", 12);
        setScore(obj, ChatColor.WHITE + "Arena: " + ChatColor.GREEN + game.getArena().getName(), 11);
        setScore(obj, "", 10);
        setScore(obj, ChatColor.WHITE + "Players: " + ChatColor.YELLOW + game.getAlivePlayers().size() + "/" + game.getPlayers().size(), 9);
        setScore(obj, ChatColor.WHITE + "Kills: " + ChatColor.RED + game.getKills(player.getUniqueId()), 8);
        setScore(obj, " ", 7);

        switch (game.getState()) {
            case WAITING:
                setScore(obj, ChatColor.YELLOW + "Waiting for players...", 6);
                break;
            case COUNTDOWN:
                setScore(obj, ChatColor.YELLOW + "Starting soon...", 6);
                break;
            case GRACE_PERIOD:
                setScore(obj, ChatColor.GREEN + "Grace Period", 6);
                break;
            case ACTIVE:
                setScore(obj, ChatColor.GREEN + "Game Active", 6);
                break;
            case DEATHMATCH:
                setScore(obj, ChatColor.RED + "DEATHMATCH!", 6);
                break;
        }

        setScore(obj, "  ", 5);
        setScore(obj, ChatColor.GRAY + "cubiom.net", 4);
        setScore(obj, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------", 3);
    }

    private void updateDuelScoreboard(Player player, Objective obj, DuelGame game) {
        Player opponent = Bukkit.getPlayer(game.getPlayer1().equals(player.getUniqueId()) ? game.getPlayer2() : game.getPlayer1());

        setScore(obj, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------", 11);
        setScore(obj, ChatColor.WHITE + "Kit: " + ChatColor.YELLOW + game.getKit().getName(), 10);
        setScore(obj, "", 9);
        setScore(obj, ChatColor.WHITE + "Opponent:", 8);
        setScore(obj, ChatColor.GREEN + (opponent != null ? opponent.getName() : "Unknown"), 7);
        setScore(obj, " ", 6);
        setScore(obj, ChatColor.WHITE + "Your ELO: " + ChatColor.AQUA + "1000", 5);
        setScore(obj, "  ", 4);
        setScore(obj, ChatColor.GRAY + "cubiom.net", 3);
        setScore(obj, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------", 2);
    }

    private void updateSpectatorScoreboard(Player player, Objective obj, SGGame game) {
        setScore(obj, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------", 10);
        setScore(obj, ChatColor.RED + "SPECTATING", 9);
        setScore(obj, "", 8);
        setScore(obj, ChatColor.WHITE + "Arena: " + ChatColor.GREEN + game.getArena().getName(), 7);
        setScore(obj, ChatColor.WHITE + "Alive: " + ChatColor.YELLOW + game.getAlivePlayers().size(), 6);
        setScore(obj, " ", 5);
        setScore(obj, ChatColor.GRAY + "cubiom.net", 4);
        setScore(obj, ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------", 3);
    }

    private void setScore(Objective obj, String text, int score) {
        Score s = obj.getScore(text);
        s.setScore(score);
    }

    private void startUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 0L, 20L);
    }

    public void shutdown() {
        boards.clear();
    }
}
