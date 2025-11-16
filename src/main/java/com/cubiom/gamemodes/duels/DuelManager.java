package com.cubiom.gamemodes.duels;

import com.cubiom.Cubiom;
import com.cubiom.arenas.DuelArena;
import com.cubiom.language.LanguageManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelManager {

    private final Cubiom plugin;
    private final List<UUID> queue;
    private final Map<UUID, DuelGame> activeDuels;
    private final Map<UUID, DuelInvite> pendingInvites;
    private final Map<UUID, String> playerKits;
    private final Map<UUID, Long> inviteCooldowns;

    public DuelManager(Cubiom plugin) {
        this.plugin = plugin;
        this.queue = new ArrayList<>();
        this.activeDuels = new HashMap<>();
        this.pendingInvites = new HashMap<>();
        this.playerKits = new HashMap<>();
        this.inviteCooldowns = new HashMap<>();
    }

    public void setPlayerKit(Player player, String kitName) {
        playerKits.put(player.getUniqueId(), kitName);
    }

    public String getPlayerKit(Player player) {
        return playerKits.getOrDefault(player.getUniqueId(), "NoDebuff");
    }

    public boolean canInvite(Player player) {
        Long lastInvite = inviteCooldowns.get(player.getUniqueId());
        if (lastInvite == null) return true;
        return (System.currentTimeMillis() - lastInvite) >= 30000;
    }

    public int getRemainingCooldown(Player player) {
        Long lastInvite = inviteCooldowns.get(player.getUniqueId());
        if (lastInvite == null) return 0;
        long elapsed = System.currentTimeMillis() - lastInvite;
        return (int) Math.max(0, (30000 - elapsed) / 1000);
    }

    public void sendDuelInvite(Player sender, Player target, String kitName) {
        LanguageManager lang = plugin.getLanguageManager();

        if (!canInvite(sender)) {
            int remaining = getRemainingCooldown(sender);
            Map<String, String> rep = new HashMap<>();
            rep.put("time", String.valueOf(remaining));
            sender.sendMessage(lang.formatMessage(sender, "duels.cooldown", rep));
            return;
        }

        if (isInDuel(target) || isInQueue(target)) {
            sender.sendMessage(lang.getMessageWithPrefix(sender, "duels.target-busy"));
            return;
        }

        if (pendingInvites.containsKey(target.getUniqueId())) {
            sender.sendMessage(lang.getMessageWithPrefix(sender, "duels.target-has-invite"));
            return;
        }

        DuelInvite invite = new DuelInvite(sender.getUniqueId(), target.getUniqueId(), kitName);
        pendingInvites.put(target.getUniqueId(), invite);
        inviteCooldowns.put(sender.getUniqueId(), System.currentTimeMillis());

        Map<String, String> senderRep = new HashMap<>();
        senderRep.put("player", target.getName());
        senderRep.put("kit", kitName);
        sender.sendMessage(lang.formatMessage(sender, "duels.invite-sent", senderRep));

        sendInviteMessage(sender, target, kitName);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (pendingInvites.get(target.getUniqueId()) == invite) {
                pendingInvites.remove(target.getUniqueId());
                sender.sendMessage(lang.getMessageWithPrefix(sender, "duels.invite-expired"));
                target.sendMessage(lang.getMessageWithPrefix(target, "duels.invite-expired"));
            }
        }, 600L);
    }

    private void sendInviteMessage(Player sender, Player target, String kitName) {
        LanguageManager lang = plugin.getLanguageManager();

        Map<String, String> rep = new HashMap<>();
        rep.put("player", sender.getName());
        rep.put("kit", kitName);

        String message = lang.formatMessage(target, "duels.invite-received", rep);
        target.sendMessage(message);

        TextComponent acceptButton = new TextComponent(lang.getMessage(target, "duels.invite-accept-button"));
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + sender.getName()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(lang.getMessage(target, "duels.invite-accept-hover")).create()));

        TextComponent space = new TextComponent(" ");

        TextComponent declineButton = new TextComponent(lang.getMessage(target, "duels.invite-decline-button"));
        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel decline " + sender.getName()));
        declineButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(lang.getMessage(target, "duels.invite-decline-hover")).create()));

        target.spigot().sendMessage(acceptButton, space, declineButton);

        String cancelMsg = lang.formatMessage(sender, "duels.invite-can-cancel", rep);
        sender.sendMessage(cancelMsg);
    }

    public void acceptInvite(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        DuelInvite invite = pendingInvites.remove(player.getUniqueId());

        if (invite == null) {
            player.sendMessage(lang.getMessageWithPrefix(player, "duels.no-invite"));
            return;
        }

        Player sender = plugin.getServer().getPlayer(invite.getSender());
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(lang.getMessageWithPrefix(player, "duels.inviter-offline"));
            return;
        }

        if (isInDuel(sender)) {
            player.sendMessage(lang.getMessageWithPrefix(player, "duels.inviter-busy"));
            return;
        }

        DuelArena arena = findAvailableArena();
        if (arena == null) {
            player.sendMessage(lang.getMessageWithPrefix(player, "duels.no-arena"));
            sender.sendMessage(lang.getMessageWithPrefix(sender, "duels.no-arena"));
            return;
        }

        Kit kit = getKitByName(invite.getKitName());
        startDuel(sender, player, arena, kit);

        Map<String, String> rep = new HashMap<>();
        rep.put("player", player.getName());
        sender.sendMessage(lang.formatMessage(sender, "duels.invite-accepted", rep));
    }

    public void declineInvite(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        DuelInvite invite = pendingInvites.remove(player.getUniqueId());

        if (invite == null) {
            player.sendMessage(lang.getMessageWithPrefix(player, "duels.no-invite"));
            return;
        }

        Player sender = plugin.getServer().getPlayer(invite.getSender());
        if (sender != null && sender.isOnline()) {
            Map<String, String> rep = new HashMap<>();
            rep.put("player", player.getName());
            sender.sendMessage(lang.formatMessage(sender, "duels.invite-declined", rep));
        }

        player.sendMessage(lang.getMessageWithPrefix(player, "duels.invite-declined-you"));
    }

    public void cancelInvite(Player sender, Player target) {
        LanguageManager lang = plugin.getLanguageManager();
        DuelInvite invite = pendingInvites.get(target.getUniqueId());

        if (invite == null || !invite.getSender().equals(sender.getUniqueId())) {
            sender.sendMessage(lang.getMessageWithPrefix(sender, "duels.no-active-invite"));
            return;
        }

        pendingInvites.remove(target.getUniqueId());
        Map<String, String> rep = new HashMap<>();
        rep.put("player", target.getName());
        sender.sendMessage(lang.formatMessage(sender, "duels.invite-cancelled", rep));
        target.sendMessage(lang.formatMessage(target, "duels.invite-cancelled-by-sender", rep));
    }

    public boolean joinQueue(Player player) {
        if (isInQueue(player) || isInDuel(player)) {
            return false;
        }

        queue.add(player.getUniqueId());

        LanguageManager langManager = plugin.getLanguageManager();
        player.sendMessage(langManager.getMessageWithPrefix(player, "duels.join-queue"));

        tryMatchPlayers();

        return true;
    }

    public void leaveQueue(Player player) {
        queue.remove(player.getUniqueId());

        LanguageManager langManager = plugin.getLanguageManager();
        player.sendMessage(langManager.getMessageWithPrefix(player, "duels.leave-queue"));
    }

    public boolean isInQueue(Player player) {
        return queue.contains(player.getUniqueId());
    }

    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }

    public DuelGame getPlayerDuel(Player player) {
        return activeDuels.get(player.getUniqueId());
    }

    private void tryMatchPlayers() {
        if (queue.size() < 2) {
            return;
        }

        UUID uuid1 = queue.remove(0);
        UUID uuid2 = queue.remove(0);

        Player player1 = plugin.getServer().getPlayer(uuid1);
        Player player2 = plugin.getServer().getPlayer(uuid2);

        if (player1 == null || player2 == null || !player1.isOnline() || !player2.isOnline()) {
            return;
        }

        DuelArena arena = findAvailableArena();
        if (arena == null) {
            queue.add(0, uuid1);
            queue.add(1, uuid2);
            return;
        }

        String kitName = getPlayerKit(player1);
        Kit kit = getKitByName(kitName);
        startDuel(player1, player2, arena, kit);
    }

    private Kit getKitByName(String name) {
        switch (name) {
            case "NoDebuff":
                return Kit.createNoDebuffKit();
            case "Debuff":
                return Kit.createDebuffKit();
            case "BuildUHC":
                return Kit.createBuildUHCKit();
            case "Classic":
                return Kit.createClassicKit();
            case "Combo":
                return Kit.createComboKit();
            default:
                return Kit.createNoDebuffKit();
        }
    }

    private void startDuel(Player player1, Player player2, DuelArena arena, Kit kit) {
        DuelGame duel = new DuelGame(plugin, arena, player1, player2, kit);

        activeDuels.put(player1.getUniqueId(), duel);
        activeDuels.put(player2.getUniqueId(), duel);

        duel.start();
    }

    private DuelArena findAvailableArena() {
        return null;
    }

    public void endDuel(DuelGame duel, Player winner) {
        activeDuels.remove(duel.getPlayer1().getUniqueId());
        activeDuels.remove(duel.getPlayer2().getUniqueId());

        duel.getArena().setInUse(false);
    }

    private static class DuelInvite {
        private final UUID sender;
        private final UUID target;
        private final String kitName;
        private final long timestamp;

        public DuelInvite(UUID sender, UUID target, String kitName) {
            this.sender = sender;
            this.target = target;
            this.kitName = kitName;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getSender() {
            return sender;
        }

        public UUID getTarget() {
            return target;
        }

        public String getKitName() {
            return kitName;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
