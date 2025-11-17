package com.cubiom.game.duel;

import com.cubiom.Cubiom;
import com.cubiom.arena.DuelArena;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelManager {

    private final Cubiom plugin;
    private final Map<String, Queue<UUID>> queues;
    private final Map<UUID, DuelInvite> pendingInvites;
    private final List<DuelGame> activeGames;

    public DuelManager(Cubiom plugin) {
        this.plugin = plugin;
        this.queues = new HashMap<>();
        this.pendingInvites = new HashMap<>();
        this.activeGames = new ArrayList<>();

        for (Kit kit : Kit.getAllKits()) {
            queues.put(kit.getName().toLowerCase(), new LinkedList<>());
        }

        startMatchmaking();
    }

    public void joinQueue(Player player, String kitName) {
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        if (cp != null && !cp.isInLobby()) {
            player.sendMessage(ChatColor.RED + "You must be in lobby to queue!");
            return;
        }

        Queue<UUID> queue = queues.get(kitName.toLowerCase());
        if (queue == null) {
            player.sendMessage(ChatColor.RED + "Invalid kit!");
            return;
        }

        if (queue.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in queue!");
            return;
        }

        queue.add(player.getUniqueId());
        cp.setSelectedKit(kitName);
        player.sendMessage(ChatColor.GREEN + "Joined " + kitName + " queue! Players in queue: " + queue.size());
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
    }

    public void leaveQueue(Player player) {
        for (Queue<UUID> queue : queues.values()) {
            if (queue.remove(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Left queue!");
                return;
            }
        }
    }

    public void sendDuelInvite(Player sender, Player target, String kitName) {
        CubiomPlayer senderCp = plugin.getPlayerManager().getPlayer(sender);
        CubiomPlayer targetCp = plugin.getPlayerManager().getPlayer(target);

        if (senderCp.hasCooldown("duel_invite")) {
            long remaining = senderCp.getRemainingCooldown("duel_invite");
            sender.sendMessage(ChatColor.RED + "You must wait " + remaining + " seconds before sending another invite!");
            return;
        }

        if (!senderCp.isInLobby() || !targetCp.isInLobby()) {
            sender.sendMessage(ChatColor.RED + "Both players must be in lobby!");
            return;
        }

        if (pendingInvites.containsKey(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "This player already has a pending invite!");
            return;
        }

        DuelInvite invite = new DuelInvite(sender.getUniqueId(), target.getUniqueId(), kitName, System.currentTimeMillis());
        pendingInvites.put(target.getUniqueId(), invite);

        senderCp.setCooldown("duel_invite", 30000);

        sender.sendMessage(ChatColor.GREEN + "Duel invite sent to " + target.getName() + " for " + kitName + " kit!");

        target.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "--------------------");
        target.sendMessage(ChatColor.YELLOW + sender.getName() + " has challenged you to a " + kitName + " duel!");
        target.sendMessage(ChatColor.GREEN + "[ACCEPT]" + ChatColor.GRAY + " - " + ChatColor.RED + "[DECLINE]");
        target.sendMessage(ChatColor.GRAY + "Type /duel accept or /duel decline");
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "--------------------");
        target.playSound(target.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingInvites.remove(target.getUniqueId()) != null) {
                sender.sendMessage(ChatColor.RED + "Your duel invite to " + target.getName() + " has expired!");
                target.sendMessage(ChatColor.RED + "Duel invite from " + sender.getName() + " has expired!");
            }
        }, 600L);
    }

    public void acceptDuelInvite(Player player) {
        DuelInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(ChatColor.RED + "You have no pending duel invites!");
            return;
        }

        Player sender = Bukkit.getPlayer(invite.getSender());
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(ChatColor.RED + "That player is no longer online!");
            return;
        }

        CubiomPlayer senderCp = plugin.getPlayerManager().getPlayer(sender);
        CubiomPlayer targetCp = plugin.getPlayerManager().getPlayer(player);

        if (!senderCp.isInLobby() || !targetCp.isInLobby()) {
            player.sendMessage(ChatColor.RED + "Both players must be in lobby!");
            return;
        }

        DuelArena arena = plugin.getArenaManager().getRandomAvailableDuelArena();
        if (arena == null) {
            player.sendMessage(ChatColor.RED + "No arenas available!");
            sender.sendMessage(ChatColor.RED + "No arenas available!");
            return;
        }

        Kit kit = Kit.getKit(invite.getKitName());
        DuelGame game = new DuelGame(plugin, arena, sender, player, kit);
        activeGames.add(game);

        sender.sendMessage(ChatColor.GREEN + player.getName() + " accepted your duel invite!");
        player.sendMessage(ChatColor.GREEN + "Accepted duel invite from " + sender.getName() + "!");
    }

    public void declineDuelInvite(Player player) {
        DuelInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(ChatColor.RED + "You have no pending duel invites!");
            return;
        }

        Player sender = Bukkit.getPlayer(invite.getSender());
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(ChatColor.RED + player.getName() + " declined your duel invite!");
        }
        player.sendMessage(ChatColor.YELLOW + "Declined duel invite!");
    }

    private void startMatchmaking() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<String, Queue<UUID>> entry : queues.entrySet()) {
                Queue<UUID> queue = entry.getValue();
                if (queue.size() >= 2) {
                    UUID p1Id = queue.poll();
                    UUID p2Id = queue.poll();

                    Player p1 = Bukkit.getPlayer(p1Id);
                    Player p2 = Bukkit.getPlayer(p2Id);

                    if (p1 == null || !p1.isOnline() || p2 == null || !p2.isOnline()) {
                        continue;
                    }

                    DuelArena arena = plugin.getArenaManager().getRandomAvailableDuelArena();
                    if (arena == null) {
                        queue.add(p1Id);
                        queue.add(p2Id);
                        continue;
                    }

                    Kit kit = Kit.getKit(entry.getKey());
                    DuelGame game = new DuelGame(plugin, arena, p1, p2, kit);
                    activeGames.add(game);

                    p1.sendMessage(ChatColor.GREEN + "Match found! Opponent: " + p2.getName());
                    p2.sendMessage(ChatColor.GREEN + "Match found! Opponent: " + p1.getName());
                }
            }
        }, 20L, 20L);
    }

    public DuelGame getPlayerGame(Player player) {
        for (DuelGame game : activeGames) {
            if (game.hasPlayer(player)) {
                return game;
            }
        }
        return null;
    }

    public void removeGame(DuelGame game) {
        activeGames.remove(game);
    }

    public boolean isInQueue(Player player) {
        for (Queue<UUID> queue : queues.values()) {
            if (queue.contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void shutdown() {
        for (DuelGame game : new ArrayList<>(activeGames)) {
            game.getArena().setInUse(false);
        }
        activeGames.clear();
        queues.clear();
        pendingInvites.clear();
    }

    private static class DuelInvite {
        private final UUID sender;
        private final UUID target;
        private final String kitName;
        private final long timestamp;

        public DuelInvite(UUID sender, UUID target, String kitName, long timestamp) {
            this.sender = sender;
            this.target = target;
            this.kitName = kitName;
            this.timestamp = timestamp;
        }

        public UUID getSender() {
            return sender;
        }

        public String getKitName() {
            return kitName;
        }
    }
}
