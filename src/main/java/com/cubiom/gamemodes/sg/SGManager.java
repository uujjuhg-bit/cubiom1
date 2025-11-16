package com.cubiom.gamemodes.sg;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import com.cubiom.language.LanguageManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.*;

public class SGManager {

    private final Cubiom plugin;
    private final Map<String, SGGame> activeGames;
    private final Map<UUID, SGGame> playerGames;
    private final Map<UUID, SGParty> parties;
    private final Map<UUID, SGPartyInvite> partyInvites;
    private final Map<UUID, Long> inviteCooldowns;

    public SGManager(Cubiom plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
        this.playerGames = new HashMap<>();
        this.parties = new HashMap<>();
        this.partyInvites = new HashMap<>();
        this.inviteCooldowns = new HashMap<>();
    }

    public boolean joinGame(Player player) {
        if (isInGame(player)) {
            return false;
        }

        SGParty party = getPlayerParty(player);
        if (party != null && !party.isLeader(player.getUniqueId())) {
            return false;
        }

        SGGame game = findAvailableGame();
        if (game == null) {
            return false;
        }

        if (party != null) {
            return joinGameWithParty(party, game);
        } else {
            if (game.addPlayer(player)) {
                playerGames.put(player.getUniqueId(), game);
                return true;
            }
        }

        return false;
    }

    private boolean joinGameWithParty(SGParty party, SGGame game) {
        List<Player> partyPlayers = new ArrayList<>();

        for (UUID uuid : party.getMembers()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline() && !isInGame(p)) {
                partyPlayers.add(p);
            }
        }

        if (game.getPlayers().size() + partyPlayers.size() > game.getArena().getMaxPlayers()) {
            return false;
        }

        for (Player p : partyPlayers) {
            if (game.addPlayer(p)) {
                playerGames.put(p.getUniqueId(), game);
            }
        }

        return !partyPlayers.isEmpty();
    }

    public void leaveGame(Player player) {
        SGGame game = playerGames.get(player.getUniqueId());

        if (game != null) {
            game.removePlayer(player);
            playerGames.remove(player.getUniqueId());
        }
    }

    public boolean isInGame(Player player) {
        return playerGames.containsKey(player.getUniqueId());
    }

    public SGGame getPlayerGame(Player player) {
        return playerGames.get(player.getUniqueId());
    }

    private SGGame findAvailableGame() {
        for (SGGame game : activeGames.values()) {
            GameState state = game.getState();
            if ((state == GameState.WAITING || state == GameState.COUNTDOWN)
                    && game.getPlayers().size() < game.getArena().getMaxPlayers()) {
                return game;
            }
        }

        Arena arena = findAvailableArena();
        if (arena != null) {
            SGGame newGame = new SGGame(plugin, arena);
            activeGames.put(arena.getName(), newGame);
            return newGame;
        }

        return null;
    }

    private Arena findAvailableArena() {
        Map<String, Arena> arenas = plugin.getDataManager().getArenas();

        for (Arena arena : arenas.values()) {
            if (arena.isEnabled() && arena.isValid() && !activeGames.containsKey(arena.getName())) {
                return arena;
            }
        }

        return null;
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

    public void sendPartyInvite(Player sender, Player target) {
        LanguageManager lang = plugin.getLanguageManager();

        if (!canInvite(sender)) {
            int remaining = getRemainingCooldown(sender);
            Map<String, String> rep = new HashMap<>();
            rep.put("time", String.valueOf(remaining));
            sender.sendMessage(lang.formatMessage(sender, "sg.party.cooldown", rep));
            return;
        }

        if (isInGame(target)) {
            sender.sendMessage(lang.getMessageWithPrefix(sender, "sg.party.target-busy"));
            return;
        }

        SGParty senderParty = getPlayerParty(sender);
        if (senderParty != null && !senderParty.isLeader(sender.getUniqueId())) {
            sender.sendMessage(lang.getMessageWithPrefix(sender, "sg.party.not-leader"));
            return;
        }

        SGParty targetParty = getPlayerParty(target);
        if (targetParty != null) {
            sender.sendMessage(lang.getMessageWithPrefix(sender, "sg.party.target-in-party"));
            return;
        }

        if (partyInvites.containsKey(target.getUniqueId())) {
            sender.sendMessage(lang.getMessageWithPrefix(sender, "sg.party.target-has-invite"));
            return;
        }

        if (senderParty != null && senderParty.getSize() >= 4) {
            sender.sendMessage(lang.getMessageWithPrefix(sender, "sg.party.party-full"));
            return;
        }

        SGPartyInvite invite = new SGPartyInvite(sender.getUniqueId(), target.getUniqueId());
        partyInvites.put(target.getUniqueId(), invite);
        inviteCooldowns.put(sender.getUniqueId(), System.currentTimeMillis());

        Map<String, String> senderRep = new HashMap<>();
        senderRep.put("player", target.getName());
        sender.sendMessage(lang.formatMessage(sender, "sg.party.invite-sent", senderRep));

        sendPartyInviteMessage(sender, target);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (partyInvites.get(target.getUniqueId()) == invite) {
                partyInvites.remove(target.getUniqueId());
                sender.sendMessage(lang.getMessageWithPrefix(sender, "sg.party.invite-expired"));
                target.sendMessage(lang.getMessageWithPrefix(target, "sg.party.invite-expired"));
            }
        }, 600L);
    }

    private void sendPartyInviteMessage(Player sender, Player target) {
        LanguageManager lang = plugin.getLanguageManager();

        Map<String, String> rep = new HashMap<>();
        rep.put("player", sender.getName());

        String message = lang.formatMessage(target, "sg.party.invite-received", rep);
        target.sendMessage(message);

        TextComponent acceptButton = new TextComponent(lang.getMessage(target, "sg.party.invite-accept-button"));
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sg party accept " + sender.getName()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(lang.getMessage(target, "sg.party.invite-accept-hover")).create()));

        TextComponent space = new TextComponent(" ");

        TextComponent declineButton = new TextComponent(lang.getMessage(target, "sg.party.invite-decline-button"));
        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sg party decline " + sender.getName()));
        declineButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(lang.getMessage(target, "sg.party.invite-decline-hover")).create()));

        target.spigot().sendMessage(acceptButton, space, declineButton);
    }

    public void acceptPartyInvite(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        SGPartyInvite invite = partyInvites.remove(player.getUniqueId());

        if (invite == null) {
            player.sendMessage(lang.getMessageWithPrefix(player, "sg.party.no-invite"));
            return;
        }

        Player sender = plugin.getServer().getPlayer(invite.getSender());
        if (sender == null || !sender.isOnline()) {
            player.sendMessage(lang.getMessageWithPrefix(player, "sg.party.inviter-offline"));
            return;
        }

        SGParty party = getPlayerParty(sender);
        if (party == null) {
            party = new SGParty(sender.getUniqueId());
            parties.put(sender.getUniqueId(), party);
            for (UUID member : party.getMembers()) {
                parties.put(member, party);
            }
        }

        if (party.getSize() >= 4) {
            player.sendMessage(lang.getMessageWithPrefix(player, "sg.party.party-full"));
            return;
        }

        party.addMember(player.getUniqueId());
        parties.put(player.getUniqueId(), party);

        Map<String, String> rep = new HashMap<>();
        rep.put("player", player.getName());

        for (UUID member : party.getMembers()) {
            Player p = plugin.getServer().getPlayer(member);
            if (p != null && p.isOnline()) {
                p.sendMessage(lang.formatMessage(p, "sg.party.player-joined", rep));
            }
        }
    }

    public void declinePartyInvite(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        SGPartyInvite invite = partyInvites.remove(player.getUniqueId());

        if (invite == null) {
            player.sendMessage(lang.getMessageWithPrefix(player, "sg.party.no-invite"));
            return;
        }

        Player sender = plugin.getServer().getPlayer(invite.getSender());
        if (sender != null && sender.isOnline()) {
            Map<String, String> rep = new HashMap<>();
            rep.put("player", player.getName());
            sender.sendMessage(lang.formatMessage(sender, "sg.party.invite-declined", rep));
        }

        player.sendMessage(lang.getMessageWithPrefix(player, "sg.party.invite-declined-you"));
    }

    public void leaveParty(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        SGParty party = getPlayerParty(player);

        if (party == null) {
            player.sendMessage(lang.getMessageWithPrefix(player, "sg.party.not-in-party"));
            return;
        }

        if (party.isLeader(player.getUniqueId())) {
            disbandParty(party);
        } else {
            party.removeMember(player.getUniqueId());
            parties.remove(player.getUniqueId());

            Map<String, String> rep = new HashMap<>();
            rep.put("player", player.getName());

            for (UUID member : party.getMembers()) {
                Player p = plugin.getServer().getPlayer(member);
                if (p != null && p.isOnline()) {
                    p.sendMessage(lang.formatMessage(p, "sg.party.player-left", rep));
                }
            }

            player.sendMessage(lang.getMessageWithPrefix(player, "sg.party.you-left"));
        }
    }

    public void disbandParty(SGParty party) {
        LanguageManager lang = plugin.getLanguageManager();

        for (UUID member : party.getMembers()) {
            Player p = plugin.getServer().getPlayer(member);
            if (p != null && p.isOnline()) {
                p.sendMessage(lang.getMessageWithPrefix(p, "sg.party.disbanded"));
            }
            parties.remove(member);
        }
    }

    public void kickFromParty(Player leader, Player target) {
        LanguageManager lang = plugin.getLanguageManager();
        SGParty party = getPlayerParty(leader);

        if (party == null) {
            leader.sendMessage(lang.getMessageWithPrefix(leader, "sg.party.not-in-party"));
            return;
        }

        if (!party.isLeader(leader.getUniqueId())) {
            leader.sendMessage(lang.getMessageWithPrefix(leader, "sg.party.not-leader"));
            return;
        }

        if (!party.isMember(target.getUniqueId())) {
            leader.sendMessage(lang.getMessageWithPrefix(leader, "sg.party.not-member"));
            return;
        }

        party.removeMember(target.getUniqueId());
        parties.remove(target.getUniqueId());

        Map<String, String> rep = new HashMap<>();
        rep.put("player", target.getName());
        rep.put("leader", leader.getName());

        for (UUID member : party.getMembers()) {
            Player p = plugin.getServer().getPlayer(member);
            if (p != null && p.isOnline()) {
                p.sendMessage(lang.formatMessage(p, "sg.party.player-kicked", rep));
            }
        }

        target.sendMessage(lang.formatMessage(target, "sg.party.you-were-kicked", rep));
    }

    public SGParty getPlayerParty(Player player) {
        return parties.get(player.getUniqueId());
    }

    public void shutdown() {
        for (SGGame game : activeGames.values()) {
            for (UUID uuid : new ArrayList<>(game.getPlayers().keySet())) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null) {
                    game.removePlayer(player);
                }
            }
        }
        activeGames.clear();
        playerGames.clear();
        parties.clear();
        partyInvites.clear();
    }

    public Map<String, SGGame> getActiveGames() {
        return activeGames;
    }

    public List<Arena> getArenas() {
        Map<String, Arena> arenas = plugin.getDataManager().getArenas();
        return new ArrayList<>(arenas.values());
    }

    public Arena getArenaByName(String name) {
        Map<String, Arena> arenas = plugin.getDataManager().getArenas();
        return arenas.get(name);
    }

    public SGGame getGameByArena(Arena arena) {
        return activeGames.get(arena.getName());
    }

    public boolean joinGame(Player player, Arena arena) {
        if (isInGame(player)) {
            return false;
        }

        SGGame game = activeGames.get(arena.getName());

        if (game == null) {
            game = new SGGame(plugin, arena);
            activeGames.put(arena.getName(), game);
        }

        SGParty party = getPlayerParty(player);
        if (party != null && party.isLeader(player.getUniqueId())) {
            return joinGameWithParty(party, game);
        }

        if (game.addPlayer(player)) {
            playerGames.put(player.getUniqueId(), game);
            return true;
        }

        return false;
    }

    private static class SGPartyInvite {
        private final UUID sender;
        private final UUID target;
        private final long timestamp;

        public SGPartyInvite(UUID sender, UUID target) {
            this.sender = sender;
            this.target = target;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getSender() {
            return sender;
        }

        public UUID getTarget() {
            return target;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
