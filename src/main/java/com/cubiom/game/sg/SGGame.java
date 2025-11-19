package com.cubiom.game.sg;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import com.cubiom.core.GameState;
import com.cubiom.core.PlayerState;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SGGame {

    private final Cubiom plugin;
    private final SGArena arena;
    private GameState state;
    private final Set<UUID> players;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> spectators;
    private final Map<UUID, Integer> kills;
    private int countdown;
    private int gracePeriod;
    private int gameTime;
    private BukkitTask task;

    public SGGame(Cubiom plugin, SGArena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.state = GameState.WAITING;
        this.players = new HashSet<>();
        this.alivePlayers = new HashSet<>();
        this.spectators = new HashSet<>();
        this.kills = new HashMap<>();
        this.countdown = 10;
        this.gracePeriod = 30;
        this.gameTime = 0;
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        alivePlayers.add(player.getUniqueId());
        kills.put(player.getUniqueId(), 0);

        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        if (cp != null) {
            cp.setState(PlayerState.IN_GAME);
            cp.setCurrentArena(arena.getName());
        }

        if (state == GameState.WAITING) {
            teleportToSpawn(player);
            prepareWaitingPlayer(player);
            checkStart();
        }
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        players.remove(uuid);
        alivePlayers.remove(uuid);
        spectators.remove(uuid);
        kills.remove(uuid);

        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        if (cp != null) {
            cp.setState(PlayerState.LOBBY);
            cp.setCurrentArena(null);
        }

        teleportToLobby(player);
        giveHotbar(player);

        checkWinner();
    }

    public void handleDeath(Player player, Player killer) {
        UUID uuid = player.getUniqueId();
        if (!alivePlayers.contains(uuid)) return;

        alivePlayers.remove(uuid);

        if (killer != null && players.contains(killer.getUniqueId())) {
            kills.put(killer.getUniqueId(), kills.getOrDefault(killer.getUniqueId(), 0) + 1);
        }

        makeSpectator(player);
        checkWinner();
    }

    private void makeSpectator(Player player) {
        spectators.add(player.getUniqueId());
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        if (cp != null) {
            cp.setState(PlayerState.SPECTATING);
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();
        player.teleport(arena.getSpectatorSpawn());

        ItemStack leaveItem = new ItemStack(Material.BED);
        org.bukkit.inventory.meta.ItemMeta meta = leaveItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Leave Game");
        leaveItem.setItemMeta(meta);
        player.getInventory().setItem(8, leaveItem);

        broadcastMessage(ChatColor.RED + player.getName() + " has died! " + alivePlayers.size() + " players remaining.");
    }

    private void teleportToSpawn(Player player) {
        List<Location> spawns = arena.getPlayerSpawns();
        if (spawns.isEmpty()) return;

        int index = new Random().nextInt(spawns.size());
        player.teleport(spawns.get(index));
    }

    private void prepareWaitingPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    private void checkStart() {
        if (state != GameState.WAITING) return;
        if (players.size() >= arena.getMinPlayers()) {
            startCountdown();
        }
    }

    private void startCountdown() {
        state = GameState.COUNTDOWN;
        countdown = 10;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (countdown <= 0) {
                startGame();
                return;
            }

            if (countdown <= 5 || countdown % 10 == 0) {
                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        String msg = plugin.getLanguageManager().getMessage(p, "sg.game-starting")
                            .replace("{time}", String.valueOf(countdown));
                        p.sendMessage(msg);
                    }
                }
                playSound(Sound.NOTE_PLING);
            }

            countdown--;
        }, 0L, 20L);
    }

    private void startGame() {
        if (task != null) {
            task.cancel();
        }

        state = GameState.GRACE_PERIOD;
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                String msg = plugin.getLanguageManager().getMessage(p, "sg.grace-period")
                    .replace("{time}", String.valueOf(gracePeriod));
                p.sendMessage(msg);
            }
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        applyGameRules();
        fillChests();
        startGracePeriod();
    }

    private void fillChests() {
        World world = Bukkit.getWorld(arena.getWorldName());
        if (world == null) return;

        for (Location loc : arena.getTier1Chests()) {
            if (loc.getBlock().getState() instanceof org.bukkit.block.Chest) {
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) loc.getBlock().getState();
                LootManager.fillTier1Chest(chest.getInventory());
            }
        }

        for (Location loc : arena.getTier2Chests()) {
            if (loc.getBlock().getState() instanceof org.bukkit.block.Chest) {
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) loc.getBlock().getState();
                LootManager.fillTier2Chest(chest.getInventory());
            }
        }
    }

    private void startGracePeriod() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (gracePeriod <= 0) {
                state = GameState.ACTIVE;
                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage(plugin.getLanguageManager().getMessage(p, "sg.grace-period-end"));
                    }
                }
                playSound(Sound.ENDERDRAGON_GROWL);
                if (task != null) task.cancel();
                startGameTimer();
                return;
            }

            if (gracePeriod <= 5 || gracePeriod % 10 == 0) {
                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        String msg = plugin.getLanguageManager().getMessage(p, "sg.grace-period-warning")
                            .replace("{0}", String.valueOf(gracePeriod));
                        p.sendMessage(msg);
                    }
                }
            }

            gracePeriod--;
        }, 0L, 20L);
    }

    private void startGameTimer() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            gameTime++;

            if (gameTime == 300) {
                for (UUID uuid : players) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage(plugin.getLanguageManager().getMessage(p, "sg.deathmatch-minute-warning"));
                    }
                }
            } else if (gameTime == 360) {
                startDeathmatch();
            }
        }, 0L, 20L);
    }

    private void startDeathmatch() {
        state = GameState.DEATHMATCH;
        broadcastMessage(ChatColor.DARK_RED + "DEATHMATCH! All players being teleported!");

        List<Location> dmSpawns = arena.getDeathmatchSpawns();
        int index = 0;

        for (UUID uuid : alivePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                Location spawn = dmSpawns.get(index % dmSpawns.size());
                player.teleport(spawn);
                index++;
            }
        }

        playSound(Sound.WITHER_SPAWN);
    }

    private void checkWinner() {
        if (state == GameState.ENDING) return;
        if (alivePlayers.size() == 1) {
            UUID winnerId = alivePlayers.iterator().next();
            Player winner = Bukkit.getPlayer(winnerId);
            if (winner != null) {
                endGame(winner);
            }
        } else if (alivePlayers.isEmpty()) {
            endGame(null);
        }
    }

    private void endGame(Player winner) {
        state = GameState.ENDING;
        if (task != null) {
            task.cancel();
        }

        if (winner != null) {
            for (UUID uuid : players) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    String winMsg = plugin.getLanguageManager().getMessage(p, "sg.winner")
                        .replace("{player}", winner.getName());
                    p.sendMessage(winMsg);

                    int playerKills = kills.getOrDefault(winner.getUniqueId(), 0);
                    String killMsg = plugin.getLanguageManager().getMessage(p, "sg.winner-kills")
                        .replace("{0}", String.valueOf(playerKills));
                    p.sendMessage(killMsg);
                }
            }
        } else {
            for (UUID uuid : players) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendMessage(plugin.getLanguageManager().getMessage(p, "sg.no-winner"));
                }
            }
        }

        boolean isSolo = arena.isSoloOnly();
        long gameDuration = (System.currentTimeMillis() - (System.currentTimeMillis() - (gameTime * 1000))) / 1000;
        com.google.gson.JsonArray participants = new com.google.gson.JsonArray();
        com.google.gson.JsonObject statsJson = new com.google.gson.JsonObject();

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            int playerKills = kills.getOrDefault(uuid, 0);
            int deaths = uuid.equals(winner != null ? winner.getUniqueId() : null) ? 0 : 1;
            int wins = uuid.equals(winner != null ? winner.getUniqueId() : null) ? 1 : 0;

            plugin.getSupabaseManager().updateSGStats(
                uuid.toString(),
                player.getName(),
                wins,
                playerKills,
                deaths,
                1,
                isSolo ? wins : 0,
                isSolo ? playerKills : 0,
                isSolo ? deaths : 0,
                isSolo ? 1 : 0,
                isSolo ? 0 : wins,
                isSolo ? 0 : playerKills,
                isSolo ? 0 : deaths,
                isSolo ? 0 : 1
            );

            com.google.gson.JsonObject playerData = new com.google.gson.JsonObject();
            playerData.addProperty("uuid", uuid.toString());
            playerData.addProperty("name", player.getName());
            playerData.addProperty("kills", playerKills);
            playerData.addProperty("died", deaths == 1);
            participants.add(playerData);
        }

        statsJson.addProperty("game_mode", isSolo ? "solo" : "team");
        statsJson.addProperty("total_kills", kills.values().stream().mapToInt(Integer::intValue).sum());

        if (winner != null) {
            plugin.getSupabaseManager().saveMatchHistory(
                "sg",
                arena.getName(),
                winner.getUniqueId().toString(),
                winner.getName(),
                participants,
                (int) gameDuration,
                statsJson
            );
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : new HashSet<>(players)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    removePlayer(player);
                }
            }
            plugin.getSGManager().removeGame(arena.getName());
        }, 100L);
    }

    private void applyGameRules() {
        World world = Bukkit.getWorld(arena.getWorldName());
        if (world == null) return;

        for (Map.Entry<String, Boolean> entry : arena.getGameRules().entrySet()) {
            world.setGameRuleValue(entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    private void teleportToLobby(Player player) {
        Location lobby = plugin.getConfig().contains("lobby.spawn") ?
            deserializeLocation(plugin.getConfig().getString("lobby.spawn")) :
            Bukkit.getWorlds().get(0).getSpawnLocation();
        player.teleport(lobby);
    }

    private void giveHotbar(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        plugin.getLobbyHotbar().giveHotbar(player);
    }

    private void broadcastMessage(String message) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
        for (UUID uuid : spectators) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }

    private void playSound(Sound sound) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
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

    public SGArena getArena() {
        return arena;
    }

    public GameState getState() {
        return state;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public Set<UUID> getAlivePlayers() {
        return alivePlayers;
    }

    public Set<UUID> getSpectators() {
        return spectators;
    }

    public int getKills(UUID uuid) {
        return kills.getOrDefault(uuid, 0);
    }

    public boolean isGracePeriod() {
        return state == GameState.GRACE_PERIOD;
    }
}
