package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.arenas.DuelArena;
import com.cubiom.language.LanguageManager;
import com.cubiom.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DuelCommand implements CommandExecutor {

    private final Cubiom plugin;
    private final Map<String, DuelArena> setupArenas;

    public DuelCommand(Cubiom plugin) {
        this.plugin = plugin;
        this.setupArenas = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        LanguageManager langManager = plugin.getLanguageManager();

        if (args.length == 0) {
            player.sendMessage("§c§lDuels Commands:");
            player.sendMessage("§7/duel join §f- Join queue");
            player.sendMessage("§7/duel leave §f- Leave queue");
            player.sendMessage("§7/duel invite <player> §f- Invite to duel");
            player.sendMessage("§7/duel accept §f- Accept invitation");
            player.sendMessage("§7/duel decline §f- Decline invitation");
            player.sendMessage("§7/duel stats §f- View your stats");
            player.sendMessage("§7/duel top §f- View top players");
            if (player.hasPermission("cubiom.admin.duel")) {
                player.sendMessage("");
                player.sendMessage("§c§lAdmin Commands:");
                player.sendMessage("§7/duel create <name> §f- Create arena");
                player.sendMessage("§7/duel setpos1 §f- Set corner 1");
                player.sendMessage("§7/duel setpos2 §f- Set corner 2");
                player.sendMessage("§7/duel setspawn1 §f- Set spawn 1");
                player.sendMessage("§7/duel setspawn2 §f- Set spawn 2");
                player.sendMessage("§7/duel enable §f- Enable arena");
                player.sendMessage("§7/duel delete <name> §f- Delete arena");
                player.sendMessage("§7/duel info <name> §f- Arena info");
                player.sendMessage("§7/duel list §f- List arenas");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                handleJoin(player, langManager);
                break;

            case "leave":
                handleLeave(player, langManager);
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /duel invite <player>");
                    return true;
                }
                handleInvite(player, args[1], langManager);
                break;

            case "accept":
                handleAccept(player, args, langManager);
                break;

            case "decline":
                handleDecline(player, args, langManager);
                break;

            case "stats":
                handleStats(player, langManager);
                break;

            case "top":
                handleTop(player, langManager);
                break;

            case "create":
                if (!player.hasPermission("cubiom.admin.duel")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /duel create <name>");
                    return true;
                }
                handleCreate(player, args[1]);
                break;

            case "setpos1":
                if (!player.hasPermission("cubiom.admin.duel")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleSetPos1(player);
                break;

            case "setpos2":
                if (!player.hasPermission("cubiom.admin.duel")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleSetPos2(player);
                break;

            case "setspawn1":
                if (!player.hasPermission("cubiom.admin.duel")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleSetSpawn1(player);
                break;

            case "setspawn2":
                if (!player.hasPermission("cubiom.admin.duel")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleSetSpawn2(player);
                break;

            case "enable":
                if (!player.hasPermission("cubiom.admin.duel")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleEnable(player);
                break;

            case "delete":
                if (!player.hasPermission("cubiom.admin.duel")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /duel delete <name>");
                    return true;
                }
                handleDelete(player, args[1]);
                break;

            case "info":
                if (!player.hasPermission("cubiom.admin.duel")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /duel info <name>");
                    return true;
                }
                handleInfo(player, args[1]);
                break;

            case "list":
                handleList(player);
                break;

            default:
                player.sendMessage("§cUnknown subcommand. Use /duel for help.");
                break;
        }

        return true;
    }

    private void handleJoin(Player player, LanguageManager langManager) {
        if (plugin.getDuelManager().joinQueue(player)) {
            player.sendMessage(langManager.getMessageWithPrefix(player, "duels.join-queue"));
        } else {
            player.sendMessage(langManager.getMessageWithPrefix(player, "duels.already-in-queue"));
        }
    }

    private void handleLeave(Player player, LanguageManager langManager) {
        if (plugin.getDuelManager().isInQueue(player)) {
            plugin.getDuelManager().leaveQueue(player);
        } else {
            player.sendMessage(langManager.getMessageWithPrefix(player, "duels.not-in-queue"));
        }
    }

    private void handleInvite(Player player, String targetName, LanguageManager langManager) {
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            player.sendMessage(langManager.getMessageWithPrefix(player, "duels.target-offline"));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(langManager.getMessageWithPrefix(player, "duels.cannot-invite-self"));
            return;
        }

        plugin.getGUIManager().openDuelInviteMenu(player, target);
    }

    private void handleAccept(Player player, String[] args, LanguageManager langManager) {
        plugin.getDuelManager().acceptInvite(player);
    }

    private void handleDecline(Player player, String[] args, LanguageManager langManager) {
        plugin.getDuelManager().declineInvite(player);
    }

    private void handleStats(Player player, LanguageManager langManager) {
        PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());

        player.sendMessage("§b§l§m                                    ");
        player.sendMessage(langManager.getMessage(player, "stats.title"));
        player.sendMessage("");

        Map<String, String> replacements = new HashMap<>();
        replacements.put("wins", String.valueOf(stats.getDuelWins()));
        replacements.put("losses", String.valueOf(stats.getDuelLosses()));
        replacements.put("elo", String.valueOf(stats.getDuelElo()));

        player.sendMessage(langManager.formatMessage(player, "stats.duel-wins", replacements));
        player.sendMessage(langManager.formatMessage(player, "stats.duel-losses", replacements));
        player.sendMessage(langManager.formatMessage(player, "stats.duel-elo", replacements));
        player.sendMessage("§b§l§m                                    ");
    }

    private void handleTop(Player player, LanguageManager langManager) {
        player.sendMessage("§b§l§m                                    ");
        player.sendMessage("§b§lTop Duel Players");
        player.sendMessage("");
        player.sendMessage("§7Coming soon...");
        player.sendMessage("§b§l§m                                    ");
    }

    private void handleCreate(Player player, String name) {
        if (plugin.getDataManager().getDuelArenas().containsKey(name)) {
            player.sendMessage("§cArena already exists!");
            return;
        }

        DuelArena arena = new DuelArena(name);
        setupArenas.put(player.getName(), arena);

        player.sendMessage("§a§l✓ §aCreated duel arena: §b" + name);
        player.sendMessage("§7Now configure it:");
        player.sendMessage("§7 1. /duel setpos1 (stand at corner 1)");
        player.sendMessage("§7 2. /duel setpos2 (stand at corner 2)");
        player.sendMessage("§7 3. /duel setspawn1 (player 1 spawn)");
        player.sendMessage("§7 4. /duel setspawn2 (player 2 spawn)");
        player.sendMessage("§7 5. /duel enable");
    }

    private void handleSetPos1(Player player) {
        DuelArena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /duel create <name>");
            return;
        }

        arena.setPos1(player.getLocation());
        player.sendMessage("§a§l✓ §aCorner 1 set!");
    }

    private void handleSetPos2(Player player) {
        DuelArena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /duel create <name>");
            return;
        }

        arena.setPos2(player.getLocation());
        player.sendMessage("§a§l✓ §aCorner 2 set!");
    }

    private void handleSetSpawn1(Player player) {
        DuelArena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /duel create <name>");
            return;
        }

        arena.setSpawn1(player.getLocation());
        player.sendMessage("§a§l✓ §aPlayer 1 spawn set!");
    }

    private void handleSetSpawn2(Player player) {
        DuelArena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /duel create <name>");
            return;
        }

        arena.setSpawn2(player.getLocation());
        player.sendMessage("§a§l✓ §aPlayer 2 spawn set!");
    }

    private void handleEnable(Player player) {
        DuelArena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /duel create <name>");
            return;
        }

        if (!arena.isValid()) {
            player.sendMessage("§c§l✗ §cArena is not valid! Missing:");
            if (arena.getPos1() == null) player.sendMessage("§c  - Corner 1");
            if (arena.getPos2() == null) player.sendMessage("§c  - Corner 2");
            if (arena.getSpawn1() == null) player.sendMessage("§c  - Spawn 1");
            if (arena.getSpawn2() == null) player.sendMessage("§c  - Spawn 2");
            return;
        }

        arena.setEnabled(true);
        plugin.getDataManager().getDuelArenas().put(arena.getName(), arena);
        plugin.getDataManager().save();
        setupArenas.remove(player.getName());

        player.sendMessage("§a§l✓ §aArena enabled and saved!");
        player.sendMessage("§7Players can now queue with §b/duel join");
    }

    private void handleDelete(Player player, String name) {
        if (!plugin.getDataManager().getDuelArenas().containsKey(name)) {
            player.sendMessage("§cArena not found!");
            return;
        }

        plugin.getDataManager().getDuelArenas().remove(name);
        plugin.getDataManager().save();
        player.sendMessage("§a§l✓ §aArena deleted: §b" + name);
    }

    private void handleInfo(Player player, String name) {
        DuelArena arena = plugin.getDataManager().getDuelArenas().get(name);
        if (arena == null) {
            player.sendMessage("§cArena not found!");
            return;
        }

        player.sendMessage("§b§l§m                                    ");
        player.sendMessage("§b§lDuel Arena Info: §f" + arena.getName());
        player.sendMessage("");
        player.sendMessage("§7World: §b" + arena.getWorldName());
        player.sendMessage("§7Status: " + (arena.isEnabled() ? "§a✓ Enabled" : "§c✗ Disabled"));
        player.sendMessage("§7Valid: " + (arena.isValid() ? "§a✓ Yes" : "§c✗ No"));
        player.sendMessage("§7In Use: " + (arena.isInUse() ? "§c✗ Yes" : "§a✓ Available"));
        player.sendMessage("§b§l§m                                    ");
    }

    private void handleList(Player player) {
        Map<String, DuelArena> arenas = plugin.getDataManager().getDuelArenas();

        if (arenas.isEmpty()) {
            player.sendMessage("§cNo arenas found!");
            return;
        }

        player.sendMessage("§b§l§m                                    ");
        player.sendMessage("§b§lDuel Arenas");
        player.sendMessage("");

        for (DuelArena arena : arenas.values()) {
            String status = arena.isEnabled() ? "§a✓ Enabled" : "§c✗ Disabled";
            String valid = arena.isValid() ? "§a(Valid)" : "§c(Invalid)";
            String inUse = arena.isInUse() ? "§c(In Use)" : "§a(Available)";
            player.sendMessage(String.format("§7- §b%s %s %s %s", arena.getName(), status, valid, inUse));
        }

        player.sendMessage("§b§l§m                                    ");
    }
}
