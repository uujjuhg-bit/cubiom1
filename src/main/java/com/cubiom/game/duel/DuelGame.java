package com.cubiom.game.duel;

import com.cubiom.Cubiom;
import com.cubiom.arena.DuelArena;
import com.cubiom.core.GameState;
import com.cubiom.core.PlayerState;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DuelGame {

    private final Cubiom plugin;
    private final DuelArena arena;
    private final UUID player1;
    private final UUID player2;
    private final Kit kit;
    private GameState state;
    private int countdown;
    private int combos;
    private long startTime;
    private BukkitTask task;

    public DuelGame(Cubiom plugin, DuelArena arena, Player p1, Player p2, Kit kit) {
        this.plugin = plugin;
        this.arena = arena;
        this.player1 = p1.getUniqueId();
        this.player2 = p2.getUniqueId();
        this.kit = kit;
        this.state = GameState.COUNTDOWN;
        this.countdown = 5;
        this.combos = 0;

        CubiomPlayer cp1 = plugin.getPlayerManager().getPlayer(p1);
        CubiomPlayer cp2 = plugin.getPlayerManager().getPlayer(p2);
        if (cp1 != null) {
            cp1.setState(PlayerState.IN_GAME);
            cp1.setCurrentArena(arena.getName());
            cp1.setSelectedKit(kit.getName());
        }
        if (cp2 != null) {
            cp2.setState(PlayerState.IN_GAME);
            cp2.setCurrentArena(arena.getName());
            cp2.setSelectedKit(kit.getName());
        }

        arena.setInUse(true);
        prepareArena();
        startCountdown();
    }

    private void prepareArena() {
        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (p1 != null) {
            p1.teleport(arena.getSpawn1());
            clearPlayer(p1);
        }
        if (p2 != null) {
            p2.teleport(arena.getSpawn2());
            clearPlayer(p2);
        }
    }

    private void clearPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    private void startCountdown() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (countdown <= 0) {
                startDuel();
                return;
            }

            sendMessage(ChatColor.YELLOW + "Duel starts in " + countdown + "...");
            playSound(Sound.NOTE_PLING);

            countdown--;
        }, 0L, 20L);
    }

    private void startDuel() {
        if (task != null) task.cancel();

        state = GameState.ACTIVE;
        startTime = System.currentTimeMillis();

        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (p1 != null) kit.applyKit(p1);
        if (p2 != null) kit.applyKit(p2);

        sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "DUEL STARTED!");
        playSound(Sound.ENDERDRAGON_GROWL);
    }

    public void handleDeath(Player loser) {
        if (state == GameState.ENDING) return;

        Player winner = getOpponent(loser);
        endDuel(winner, loser);
    }

    private void endDuel(Player winner, Player loser) {
        state = GameState.ENDING;
        if (task != null) task.cancel();

        long duration = (System.currentTimeMillis() - startTime) / 1000;

        sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + winner.getName() + " has won the duel!");
        sendMessage(ChatColor.YELLOW + "Duration: " + duration + " seconds");

        int winnerELO = calculateELO(winner, loser, true);
        int loserELO = calculateELO(loser, winner, false);

        winner.sendMessage(ChatColor.GREEN + "Your new ELO: " + winnerELO);
        loser.sendMessage(ChatColor.RED + "Your new ELO: " + loserELO);

        plugin.getSupabaseManager().updateDuelStats(
            winner.getUniqueId().toString(),
            winner.getName(),
            kit.getName().toLowerCase(),
            1, 0, winnerELO, 1, 1, 1
        );

        plugin.getSupabaseManager().updateDuelStats(
            loser.getUniqueId().toString(),
            loser.getName(),
            kit.getName().toLowerCase(),
            0, 1, loserELO, 1, 0, 0
        );

        plugin.getSupabaseManager().saveChallengeHistory(
            player1.getUniqueId().toString(),
            player1.getName(),
            player2.getUniqueId().toString(),
            player2.getName(),
            kit.getName().toLowerCase(),
            "completed",
            winner.getUniqueId().toString()
        );

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cleanup();
        }, 100L);
    }

    private int calculateELO(Player player, Player opponent, boolean won) {
        int currentELO = getPlayerELO(player);
        int opponentELO = getPlayerELO(opponent);

        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, (opponentELO - currentELO) / 400.0));
        int actualScore = won ? 1 : 0;
        int K = 32;

        int newELO = (int) Math.round(currentELO + K * (actualScore - expectedScore));
        return Math.max(100, newELO);
    }

    private int getPlayerELO(Player player) {
        try {
            com.google.gson.JsonObject stats = plugin.getSupabaseManager()
                .loadDuelStats(player.getUniqueId().toString(), kit.getName().toLowerCase())
                .join();

            if (stats != null && stats.has("elo")) {
                return stats.get("elo").getAsInt();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load ELO for " + player.getName());
        }
        return 1000;
    }

    private void cleanup() {
        arena.setInUse(false);

        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (p1 != null) {
            returnToLobby(p1);
        }
        if (p2 != null) {
            returnToLobby(p2);
        }

        plugin.getDuelManager().removeGame(this);
    }

    private void returnToLobby(Player player) {
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        if (cp != null) {
            cp.setState(PlayerState.LOBBY);
            cp.setCurrentArena(null);
        }

        clearPlayer(player);
        Location lobby = plugin.getConfig().contains("lobby.spawn") ?
            deserializeLocation(plugin.getConfig().getString("lobby.spawn")) :
            Bukkit.getWorlds().get(0).getSpawnLocation();
        player.teleport(lobby);
    }

    private Player getOpponent(Player player) {
        UUID uuid = player.getUniqueId();
        if (uuid.equals(player1)) {
            return Bukkit.getPlayer(player2);
        } else {
            return Bukkit.getPlayer(player1);
        }
    }

    private void sendMessage(String message) {
        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);
        if (p1 != null) p1.sendMessage(message);
        if (p2 != null) p2.sendMessage(message);
    }

    private void playSound(Sound sound) {
        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);
        if (p1 != null) p1.playSound(p1.getLocation(), sound, 1.0f, 1.0f);
        if (p2 != null) p2.playSound(p2.getLocation(), sound, 1.0f, 1.0f);
    }

    private Location deserializeLocation(String s) {
        String[] parts = s.split(",");
        return new Location(
            Bukkit.getWorld(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]),
            Float.parseFloat(parts[4]),
            Float.parseFloat(parts[5])
        );
    }

    public DuelArena getArena() {
        return arena;
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public boolean hasPlayer(Player player) {
        return player.getUniqueId().equals(player1) || player.getUniqueId().equals(player2);
    }

    public GameState getState() {
        return state;
    }

    public Kit getKit() {
        return kit;
    }
}
