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

        if (cp == null) {
            player.sendMessage(ChatColor.RED + "✖ Error: Player data not loaded!");
            return;
        }

        if (!cp.isInLobby()) {
            player.sendMessage(ChatColor.RED + "✖ You must be in the lobby to join a queue!");
            player.sendMessage(ChatColor.GRAY + "Finish your current game first.");
            return;
        }

        if (isInAnyQueue(player)) {
            player.sendMessage(ChatColor.RED + "✖ You are already in a queue!");
            player.sendMessage(ChatColor.GRAY + "Use /duel leave to leave your current queue.");
            return;
        }

        Queue<UUID> queue = queues.get(kitName.toLowerCase());
        if (queue == null) {
            player.sendMessage(ChatColor.RED + "✖ Invalid kit: " + kitName);
            return;
        }

        queue.add(player.getUniqueId());
        cp.setSelectedKit(kitName);

        player.sendMessage(ChatColor.GREEN + "✓ Joined " + ChatColor.YELLOW + kitName + ChatColor.GREEN + " queue!");
        player.sendMessage(ChatColor.GRAY + "Players in queue: " + ChatColor.WHITE + queue.size());

        if (queue.size() == 1) {
            player.sendMessage(ChatColor.YELLOW + "⌛ Waiting for an opponent...");
        } else {
            player.sendMessage(ChatColor.YELLOW + "⚔ Finding match...");
        }

        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);
    }

    private boolean isInAnyQueue(Player player) {
        for (Queue<UUID> queue : queues.values()) {
            if (queue.contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void leaveQueue(Player player) {
        boolean wasInQueue = false;
        for (Queue<UUID> queue : queues.values()) {
            if (queue.remove(player.getUniqueId())) {
                wasInQueue = true;
                break;
            }
        }

        if (wasInQueue) {
            player.sendMessage(ChatColor.YELLOW + "✓ Left queue!");
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
        } else {
            player.sendMessage(ChatColor.RED + "✖ You are not in a queue!");
        }
    }

    public void sendDuelInvite(Player sender, Player target, String kitName) {
        if (sender.equals(target)) {
            sender.sendMessage(ChatColor.RED + "✖ You cannot duel yourself!");
            return;
        }

        CubiomPlayer senderCp = plugin.getPlayerManager().getPlayer(sender);
        CubiomPlayer targetCp = plugin.getPlayerManager().getPlayer(target);

        if (senderCp == null || targetCp == null) {
            sender.sendMessage(ChatColor.RED + "✖ Error: Player data not loaded!");
            return;
        }

        if (senderCp.hasCooldown("duel_invite")) {
            long remaining = senderCp.getRemainingCooldown("duel_invite");
            sender.sendMessage(ChatColor.RED + "✖ Slow down! Wait " + remaining + " seconds before sending another invite.");
            sender.playSound(sender.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!senderCp.isInLobby()) {
            sender.sendMessage(ChatColor.RED + "✖ You must be in the lobby to send duel invites!");
            return;
        }

        if (!targetCp.isInLobby()) {
            sender.sendMessage(ChatColor.RED + "✖ " + target.getName() + " is not available right now!");
            sender.sendMessage(ChatColor.GRAY + "They are currently in a game.");
            return;
        }

        if (pendingInvites.containsKey(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "✖ " + target.getName() + " already has a pending duel invite!");
            sender.sendMessage(ChatColor.GRAY + "Try again in a moment.");
            return;
        }

        if (isInAnyQueue(sender)) {
            sender.sendMessage(ChatColor.RED + "✖ Leave your current queue before sending invites!");
            sender.sendMessage(ChatColor.GRAY + "Use /duel leave first.");
            return;
        }

        if (isInAnyQueue(target)) {
            sender.sendMessage(ChatColor.RED + "✖ " + target.getName() + " is currently in a queue!");
            return;
        }

        Kit kit = Kit.getKit(kitName);
        String kitDisplay = kit != null ? ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()) : kitName;

        DuelInvite invite = new DuelInvite(sender.getUniqueId(), target.getUniqueId(), kitName, System.currentTimeMillis());
        pendingInvites.put(target.getUniqueId(), invite);

        senderCp.setCooldown("duel_invite", 30000);

        sender.sendMessage(ChatColor.GREEN + "✓ Duel invite sent to " + ChatColor.YELLOW + target.getName());
        sender.sendMessage(ChatColor.GRAY + "Kit: " + kitDisplay);
        sender.playSound(sender.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);

        target.sendMessage("");
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⚔ DUEL REQUEST ⚔");
        target.sendMessage("");
        target.sendMessage(ChatColor.YELLOW + "  " + sender.getName() + ChatColor.GRAY + " has challenged you!");
        target.sendMessage(ChatColor.GRAY + "  Kit: " + kitDisplay);
        target.sendMessage("");
        target.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "  [ACCEPT] " + ChatColor.DARK_GRAY + "| " +
                         ChatColor.RED + "" + ChatColor.BOLD + "[DECLINE]");
        target.sendMessage(ChatColor.GRAY + "  /duel accept or /duel decline");
        target.sendMessage(ChatColor.DARK_GRAY + "  Expires in 30 seconds");
        target.sendMessage("");
        target.playSound(target.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            DuelInvite removed = pendingInvites.remove(target.getUniqueId());
            if (removed != null && removed.getSender().equals(sender.getUniqueId())) {
                sender.sendMessage(ChatColor.RED + "✖ Your duel invite to " + target.getName() + " expired.");
                target.sendMessage(ChatColor.RED + "✖ Duel invite from " + sender.getName() + " expired.");
            }
        }, 600L);
    }

    public void acceptDuelInvite(Player player) {
        DuelInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(ChatColor.RED + "✖ You have no pending duel invites!");
            return;
        }

        Player sender = Bukkit.getPlayer(invite.getSender());
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(ChatColor.RED + "✖ That player is no longer online!");
            return;
        }

        CubiomPlayer senderCp = plugin.getPlayerManager().getPlayer(sender);
        CubiomPlayer targetCp = plugin.getPlayerManager().getPlayer(player);

        if (senderCp == null || targetCp == null) {
            player.sendMessage(ChatColor.RED + "✖ Error: Player data not loaded!");
            return;
        }

        if (!senderCp.isInLobby()) {
            player.sendMessage(ChatColor.RED + "✖ " + sender.getName() + " is no longer available!");
            return;
        }

        if (!targetCp.isInLobby()) {
            player.sendMessage(ChatColor.RED + "✖ You must be in the lobby!");
            return;
        }

        DuelArena arena = plugin.getArenaManager().getRandomAvailableDuelArena();
        if (arena == null) {
            player.sendMessage(ChatColor.RED + "✖ No arenas available at the moment!");
            sender.sendMessage(ChatColor.RED + "✖ No arenas available for your duel!");
            return;
        }

        Kit kit = Kit.getKit(invite.getKitName());
        DuelGame game = new DuelGame(plugin, arena, sender, player, kit);
        activeGames.add(game);

        sender.sendMessage(ChatColor.GREEN + "✓ " + player.getName() + " accepted your duel!");
        sender.playSound(sender.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);

        player.sendMessage(ChatColor.GREEN + "✓ Accepted duel from " + sender.getName() + "!");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
    }

    public void declineDuelInvite(Player player) {
        DuelInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(ChatColor.RED + "✖ You have no pending duel invites!");
            return;
        }

        Player sender = Bukkit.getPlayer(invite.getSender());
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(ChatColor.RED + "✖ " + player.getName() + " declined your duel invite.");
        }

        player.sendMessage(ChatColor.YELLOW + "✓ Declined duel invite!");
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
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
                    String kitDisplay = kit != null ? ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()) : entry.getKey();

                    DuelGame game = new DuelGame(plugin, arena, p1, p2, kit);
                    activeGames.add(game);

                    p1.sendMessage("");
                    p1.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "✓ MATCH FOUND!");
                    p1.sendMessage(ChatColor.GRAY + "  Opponent: " + ChatColor.YELLOW + p2.getName());
                    p1.sendMessage(ChatColor.GRAY + "  Kit: " + kitDisplay);
                    p1.sendMessage("");
                    p1.playSound(p1.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);

                    p2.sendMessage("");
                    p2.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "✓ MATCH FOUND!");
                    p2.sendMessage(ChatColor.GRAY + "  Opponent: " + ChatColor.YELLOW + p1.getName());
                    p2.sendMessage(ChatColor.GRAY + "  Kit: " + kitDisplay);
                    p2.sendMessage("");
                    p2.playSound(p2.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
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

    public List<DuelGame> getActiveGames() {
        return activeGames;
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
