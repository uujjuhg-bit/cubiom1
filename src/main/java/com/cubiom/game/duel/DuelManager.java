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
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.error.data-not-loaded"));
            return;
        }

        if (!cp.isInLobby()) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.error.must-be-in-lobby"));
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.error.finish-current-game"));
            return;
        }

        if (isInAnyQueue(player)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.error.already-in-queue"));
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.error.leave-queue-first"));
            return;
        }

        Queue<UUID> queue = queues.get(kitName.toLowerCase());
        if (queue == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.error.invalid-kit")
                .replace("{0}", kitName));
            return;
        }

        queue.add(player.getUniqueId());
        cp.setSelectedKit(kitName);

        player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.queue.joined")
            .replace("{0}", kitName));
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.queue.players-in-queue")
            .replace("{0}", String.valueOf(queue.size())));

        if (queue.size() == 1) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.queue.waiting"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.queue.finding-match"));
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
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.queue.left"));
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.queue.not-in-queue"));
        }
    }

    public void sendDuelInvite(Player sender, Player target, String kitName) {
        if (sender.equals(target)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.cannot-duel-self"));
            return;
        }

        CubiomPlayer senderCp = plugin.getPlayerManager().getPlayer(sender);
        CubiomPlayer targetCp = plugin.getPlayerManager().getPlayer(target);

        if (senderCp == null || targetCp == null) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.data-not-loaded"));
            return;
        }

        if (senderCp.hasCooldown("duel_invite")) {
            long remaining = senderCp.getRemainingCooldown("duel_invite");
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.invite-cooldown")
                .replace("{0}", String.valueOf(remaining)));
            sender.playSound(sender.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!senderCp.isInLobby()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.must-be-in-lobby-invite"));
            return;
        }

        if (!targetCp.isInLobby()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.target-not-available")
                .replace("{0}", target.getName()));
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.target-in-game"));
            return;
        }

        if (pendingInvites.containsKey(target.getUniqueId())) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.target-has-invite")
                .replace("{0}", target.getName()));
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.try-again"));
            return;
        }

        if (isInAnyQueue(sender)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.leave-queue-before-invite"));
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.use-duel-leave"));
            return;
        }

        if (isInAnyQueue(target)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.error.target-in-queue")
                .replace("{0}", target.getName()));
            return;
        }

        Kit kit = Kit.getKit(kitName);
        String kitDisplay = kit != null ? ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()) : kitName;

        DuelInvite invite = new DuelInvite(sender.getUniqueId(), target.getUniqueId(), kitName, System.currentTimeMillis());
        pendingInvites.put(target.getUniqueId(), invite);

        senderCp.setCooldown("duel_invite", 30000);

        sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.invite.sent")
            .replace("{0}", target.getName()));
        sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.invite.kit-display")
            .replace("{0}", kitDisplay));
        sender.playSound(sender.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);

        target.sendMessage("");
        target.sendMessage(plugin.getLanguageManager().getMessage(target, "duels.invite.received-header"));
        target.sendMessage(plugin.getLanguageManager().getMessage(target, "duels.invite.received-from")
            .replace("{0}", sender.getName()));
        target.sendMessage(plugin.getLanguageManager().getMessage(target, "duels.invite.received-kit")
            .replace("{0}", kitDisplay));
        target.sendMessage(plugin.getLanguageManager().getMessage(target, "duels.invite.received-actions"));
        target.sendMessage(plugin.getLanguageManager().getMessage(target, "duels.invite.received-commands"));
        target.sendMessage(plugin.getLanguageManager().getMessage(target, "duels.invite.received-expires"));
        target.sendMessage("");
        target.playSound(target.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            DuelInvite removed = pendingInvites.remove(target.getUniqueId());
            if (removed != null && removed.getSender().equals(sender.getUniqueId())) {
                sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.invite.expired-sender")
                    .replace("{0}", target.getName()));
                target.sendMessage(plugin.getLanguageManager().getMessage(target, "duels.invite.expired-target")
                    .replace("{0}", sender.getName()));
            }
        }, 600L);
    }

    public void acceptDuelInvite(Player player) {
        DuelInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.invite.no-pending"));
            return;
        }

        Player sender = Bukkit.getPlayer(invite.getSender());
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.error.target-not-available")
                .replace("{0}", "sender"));
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

        sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.invite-accepted")
            .replace("{player}", player.getName()));
        sender.playSound(sender.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);

        player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.invite-accepted")
            .replace("{player}", sender.getName()));
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
    }

    public void declineDuelInvite(Player player) {
        DuelInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.invite.no-pending"));
            return;
        }

        Player sender = Bukkit.getPlayer(invite.getSender());
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender, "duels.invite-declined")
                .replace("{player}", player.getName()));
        }

        player.sendMessage(plugin.getLanguageManager().getMessage(player, "duels.invite-declined-you"));
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
