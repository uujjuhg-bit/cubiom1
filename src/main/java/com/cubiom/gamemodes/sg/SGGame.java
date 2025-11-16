package com.cubiom.gamemodes.sg;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import com.cubiom.language.LanguageManager;
import com.cubiom.stats.PlayerStats;
import com.cubiom.stats.StatsManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SGGame {

    private final Cubiom plugin;
    private final Arena arena;
    private final Map<UUID, PlayerGameData> players;
    private final Set<UUID> spectators;
    private final LootManager lootManager;
    private final WorldSnapshot worldSnapshot;
    private GameState state;
    private BukkitTask gameTask;
    private int timer;

    public SGGame(Cubiom plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.players = new HashMap<>();
        this.spectators = new HashSet<>();
        this.lootManager = new LootManager();
        this.worldSnapshot = new WorldSnapshot(plugin, arena);
        this.state = GameState.WAITING;
        this.timer = 0;
    }

    public Arena getArena() {
        return arena;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Map<UUID, PlayerGameData> getPlayers() {
        return players;
    }

    public Set<UUID> getSpectators() {
        return spectators;
    }

    public boolean addPlayer(Player player) {
        if (players.size() >= arena.getMaxPlayers()) {
            return false;
        }

        if (state != GameState.WAITING && state != GameState.COUNTDOWN) {
            return false;
        }

        PlayerGameData data = new PlayerGameData(player);
        players.put(player.getUniqueId(), data);

        List<Location> spawns = new ArrayList<>(arena.getSpawnPoints());
        Location spawn = spawns.get(new Random().nextInt(spawns.size()));
        player.teleport(spawn);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);

        LanguageManager langManager = plugin.getLanguageManager();
        player.sendMessage(langManager.getMessageWithPrefix(player, "sg.join-arena"));

        Map<String, String> replacements = new HashMap<>();
        replacements.put("current", String.valueOf(players.size()));
        replacements.put("min", String.valueOf(arena.getMinPlayers()));
        broadcastMessageFormatted("sg.waiting-for-players", replacements);

        plugin.getScoreboardManager().setSGWaitingScoreboard(player, players.size(), arena.getMinPlayers());

        if (players.size() >= arena.getMinPlayers() && state == GameState.WAITING) {
            broadcastMessage("sg.countdown-starting", null);
            startCountdown();
        }

        return true;
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        players.remove(uuid);
        spectators.remove(uuid);

        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();

        if (players.size() < arena.getMinPlayers() && state == GameState.COUNTDOWN) {
            cancelCountdown();
        }

        plugin.getScoreboardManager().setLobbyScoreboard(player);
    }

    private void startCountdown() {
        state = GameState.COUNTDOWN;
        timer = plugin.getConfigManager().getSGCountdown();

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer <= 0) {
                    startGame();
                    cancel();
                    return;
                }

                if (timer <= 5 || timer % 10 == 0) {
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("time", String.valueOf(timer));
                    broadcastMessageFormatted("sg.game-starting", replacements);
                }

                timer--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelCountdown() {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        state = GameState.WAITING;
        timer = 0;

        for (PlayerGameData data : players.values()) {
            Player p = data.getPlayer();
            if (p != null && p.isOnline()) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("current", String.valueOf(players.size()));
                replacements.put("min", String.valueOf(arena.getMinPlayers()));
                String message = plugin.getLanguageManager().formatMessage(p, "sg.waiting-for-players", replacements);
                p.sendMessage(message);
            }
        }
    }

    private void startGame() {
        state = GameState.GRACE_PERIOD;

        worldSnapshot.captureSnapshot();

        List<Location> spawns = new ArrayList<>(arena.getSpawnPoints());
        Collections.shuffle(spawns);

        int index = 0;
        for (PlayerGameData data : players.values()) {
            Location spawn = spawns.get(index % spawns.size());
            data.setSpawnLocation(spawn);
            data.getPlayer().teleport(spawn);
            plugin.getScoreboardManager().setSGGameScoreboard(data.getPlayer(), this);
            index++;
        }

        broadcastMessage("sg.game-started", null);

        fillChests();

        timer = plugin.getConfigManager().getSGGracePeriod();
        startGracePeriod();
    }

    private void startGracePeriod() {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("time", String.valueOf(timer));
        broadcastMessageFormatted("sg.grace-period", replacements);

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer <= 0) {
                    endGracePeriod();
                    cancel();
                    return;
                }
                timer--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void endGracePeriod() {
        state = GameState.RUNNING;
        broadcastMessage("sg.grace-period-end", null);

        timer = plugin.getConfigManager().getSGDeathmatchTime();
        startMainGame();
    }

    private void startMainGame() {
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer <= 0) {
                    startDeathmatch();
                    cancel();
                    return;
                }
                timer--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startDeathmatch() {
        state = GameState.DEATHMATCH;
        broadcastMessage("sg.deathmatch-started", null);

        for (PlayerGameData data : players.values()) {
            if (data.isAlive()) {
                data.getPlayer().teleport(arena.getDeathmatchSpawn());
            }
        }
    }

    private void fillChests() {
        for (Location loc : arena.getTier1Chests()) {
            Block block = loc.getBlock();
            if (block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                lootManager.fillChest(chest.getInventory(), 1);
            }
        }

        for (Location loc : arena.getTier2Chests()) {
            Block block = loc.getBlock();
            if (block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                lootManager.fillChest(chest.getInventory(), 2);
            }
        }
    }

    public void handlePlayerDeath(Player player, Player killer) {
        UUID uuid = player.getUniqueId();
        PlayerGameData data = players.get(uuid);

        if (data == null || !data.isAlive()) {
            return;
        }

        data.setAlive(false);

        StatsManager statsManager = plugin.getStatsManager();
        PlayerStats stats = statsManager.getPlayerStats(uuid);
        stats.addSgDeath();

        if (killer != null) {
            PlayerGameData killerData = players.get(killer.getUniqueId());
            if (killerData != null) {
                killerData.addKill();
                PlayerStats killerStats = statsManager.getPlayerStats(killer.getUniqueId());
                killerStats.addSgKill();

                Map<String, String> replacements = new HashMap<>();
                replacements.put("player", player.getName());
                replacements.put("killer", killer.getName());
                broadcastMessageFormatted("sg.player-death", replacements);
            }
        } else {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("player", player.getName());
            broadcastMessageFormatted("sg.player-death-natural", replacements);
        }

        player.setGameMode(GameMode.SPECTATOR);
        spectators.add(uuid);

        checkGameEnd();
    }

    private void checkGameEnd() {
        long aliveCount = players.values().stream().filter(PlayerGameData::isAlive).count();

        Map<String, String> replacements = new HashMap<>();
        replacements.put("count", String.valueOf(aliveCount));
        broadcastMessageFormatted("sg.tributes-remaining", replacements);

        if (aliveCount == 1) {
            endGame();
        } else if (aliveCount == 0) {
            endGameNoWinner();
        }
    }

    private void endGame() {
        state = GameState.END;

        PlayerGameData winner = players.values().stream()
                .filter(PlayerGameData::isAlive)
                .findFirst()
                .orElse(null);

        if (winner != null) {
            Player winnerPlayer = winner.getPlayer();
            StatsManager statsManager = plugin.getStatsManager();
            PlayerStats stats = statsManager.getPlayerStats(winnerPlayer.getUniqueId());
            stats.addSgWin();

            Map<String, String> replacements = new HashMap<>();
            replacements.put("player", winnerPlayer.getName());
            broadcastMessageFormatted("sg.winner", replacements);
        }

        cleanup();
    }

    private void endGameNoWinner() {
        state = GameState.END;
        cleanup();
    }

    private void cleanup() {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        World lobbyWorld = plugin.getServer().getWorld("lobby");

        for (PlayerGameData data : players.values()) {
            Player player = data.getPlayer();
            if (player != null && player.isOnline()) {
                if (lobbyWorld != null) {
                    player.teleport(lobbyWorld.getSpawnLocation());
                }

                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setFireTicks(0);
                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

                plugin.getHotbarManager().giveHotbarItems(player);
                plugin.getScoreboardManager().setLobbyScoreboard(player);

                LanguageManager langManager = plugin.getLanguageManager();
                player.sendMessage(langManager.getMessageWithPrefix(player, "general.returning-to-lobby"));
            }
        }

        worldSnapshot.restore();

        players.clear();
        spectators.clear();
        state = GameState.WAITING;
    }

    public void trackBlockPlace(Location location) {
        worldSnapshot.trackBlockPlace(location);
    }

    public void trackBlockBreak(Block block) {
        worldSnapshot.trackBlockBreak(block);
    }

    private void broadcastMessage(String key, Player exclude) {
        LanguageManager langManager = plugin.getLanguageManager();
        for (PlayerGameData data : players.values()) {
            Player player = data.getPlayer();
            if (player != null && player.isOnline() && !player.equals(exclude)) {
                player.sendMessage(langManager.getMessageWithPrefix(player, key));
            }
        }
    }

    private void broadcastMessageFormatted(String key, Map<String, String> replacements) {
        LanguageManager langManager = plugin.getLanguageManager();
        for (PlayerGameData data : players.values()) {
            Player player = data.getPlayer();
            if (player != null && player.isOnline()) {
                String prefix = langManager.getMessage(player, "general.prefix");
                String message = langManager.formatMessage(player, key, replacements);
                player.sendMessage(prefix + message);
            }
        }
    }

    public int getAliveCount() {
        return (int) players.values().stream().filter(PlayerGameData::isAlive).count();
    }

    public boolean isInGame(UUID uuid) {
        return players.containsKey(uuid);
    }

    public List<Player> getAlivePlayers() {
        List<Player> alive = new ArrayList<>();
        for (PlayerGameData data : players.values()) {
            if (data.isAlive() && data.getPlayer() != null && data.getPlayer().isOnline()) {
                alive.add(data.getPlayer());
            }
        }
        return alive;
    }

    public PlayerGameData getPlayerData(Player player) {
        return players.get(player.getUniqueId());
    }
}
