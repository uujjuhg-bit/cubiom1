package com.cubiom.gamemodes.duels;

import com.cubiom.Cubiom;
import com.cubiom.arenas.DuelArena;
import com.cubiom.language.LanguageManager;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelManager {

    private final Cubiom plugin;
    private final List<UUID> queue;
    private final Map<UUID, DuelGame> activeDuels;
    private final Map<UUID, UUID> invites;
    private final Map<UUID, String> playerKits;

    public DuelManager(Cubiom plugin) {
        this.plugin = plugin;
        this.queue = new ArrayList<>();
        this.activeDuels = new HashMap<>();
        this.invites = new HashMap<>();
        this.playerKits = new HashMap<>();
    }

    public void setPlayerKit(Player player, String kitName) {
        playerKits.put(player.getUniqueId(), kitName);
    }

    public String getPlayerKit(Player player) {
        return playerKits.getOrDefault(player.getUniqueId(), "NoDebuff");
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

        LanguageManager langManager = plugin.getLanguageManager();
        player1.sendMessage(langManager.getMessageWithPrefix(player1, "duels.match-found"));
        player2.sendMessage(langManager.getMessageWithPrefix(player2, "duels.match-found"));

        duel.start();
    }

    public void endDuel(DuelGame duel) {
        activeDuels.remove(duel.getPlayer1().getUniqueId());
        activeDuels.remove(duel.getPlayer2().getUniqueId());
        duel.cleanup();
    }

    public void sendInvite(Player sender, Player target) {
        invites.put(target.getUniqueId(), sender.getUniqueId());

        LanguageManager langManager = plugin.getLanguageManager();

        Map<String, String> replacements = new HashMap<>();
        replacements.put("player", target.getName());
        sender.sendMessage(langManager.getMessage(sender, "general.prefix") +
                langManager.formatMessage(sender, "duels.invite-sent", replacements));

        replacements.put("player", sender.getName());
        target.sendMessage(langManager.getMessage(target, "general.prefix") +
                langManager.formatMessage(target, "duels.invite-received", replacements));
    }

    public boolean acceptInvite(Player player) {
        UUID senderUuid = invites.remove(player.getUniqueId());
        if (senderUuid == null) {
            return false;
        }

        Player sender = plugin.getServer().getPlayer(senderUuid);
        if (sender == null || !sender.isOnline()) {
            return false;
        }

        DuelArena arena = findAvailableArena();
        if (arena == null) {
            return false;
        }

        Kit kit = getDefaultKit();
        startDuel(sender, player, arena, kit);

        return true;
    }

    public void declineInvite(Player player) {
        invites.remove(player.getUniqueId());
    }

    private DuelArena findAvailableArena() {
        Map<String, DuelArena> arenas = plugin.getDataManager().getDuelArenas();

        for (DuelArena arena : arenas.values()) {
            if (arena.isEnabled() && arena.isValid() && !arena.isInUse()) {
                return arena;
            }
        }

        return null;
    }

    private Kit getDefaultKit() {
        Map<String, Kit> kits = plugin.getDataManager().getKits();

        if (kits.containsKey("Classic")) {
            return kits.get("Classic");
        }

        return Kit.createClassicKit();
    }

    public void shutdown() {
        for (DuelGame duel : new ArrayList<>(activeDuels.values())) {
            duel.cleanup();
        }
        activeDuels.clear();
        queue.clear();
        invites.clear();
    }
}
