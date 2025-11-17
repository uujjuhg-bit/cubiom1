package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.arena.ArenaSetupSession;
import com.cubiom.arena.DuelArena;
import com.cubiom.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final Cubiom plugin;
    private final LanguageManager lang;

    public DuelCommand(Cubiom plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(lang.getMessage("en_US", "error.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                return handleJoin(player, args);
            case "leave":
                return handleLeave(player);
            case "invite":
                return handleInvite(player, args);
            case "accept":
                return handleAccept(player);
            case "decline":
                return handleDecline(player);
            case "create":
                return handleCreate(player, args);
            case "delete":
                return handleDelete(player, args);
            case "enable":
                return handleEnable(player, args);
            case "disable":
                return handleDisable(player, args);
            case "list":
                return handleList(player);
            case "info":
                return handleInfo(player, args);
            case "setpos1":
                return handleSetPos1(player);
            case "setpos2":
                return handleSetPos2(player);
            case "setspawn1":
                return handleSetSpawn1(player);
            case "setspawn2":
                return handleSetSpawn2(player);
            case "complete":
                return handleComplete(player);
            case "cancel":
                return handleCancel(player);
            default:
                player.sendMessage(lang.getMessage(player, "error.unknown-command"));
                return true;
        }
    }

    private boolean handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getGUIManager().openKitSelector(player);
            return true;
        }

        String kitName = args[1].toLowerCase();
        plugin.getDuelManager().joinQueue(player, kitName);
        return true;
    }

    private boolean handleLeave(Player player) {
        plugin.getDuelManager().leaveQueue(player);
        player.sendMessage(lang.getMessage(player, "duel.left-queue"));
        return true;
    }

    private boolean handleInvite(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(lang.getMessage(player, "duel.usage.invite"));
            return true;
        }

        String targetName = args[1];
        String kitName = args[2].toLowerCase();

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(lang.getMessage(player, "error.player-not-found").replace("{0}", targetName));
            return true;
        }

        plugin.getDuelManager().sendDuelInvite(player, target, kitName);
        return true;
    }

    private boolean handleAccept(Player player) {
        plugin.getDuelManager().acceptDuelInvite(player);
        return true;
    }

    private boolean handleDecline(Player player) {
        plugin.getDuelManager().declineDuelInvite(player);
        return true;
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "duel.usage.create"));
            return true;
        }

        String arenaName = args[1];

        if (plugin.getArenaManager().getDuelArena(arenaName) != null) {
            player.sendMessage(lang.getMessage(player, "duel.error.arena-exists").replace("{0}", arenaName));
            return true;
        }

        plugin.getArenaSetupManager().startSession(player, arenaName, ArenaSetupSession.ArenaType.DUEL);
        player.sendMessage(lang.getMessage(player, "duel.setup.started").replace("{0}", arenaName));
        player.sendMessage(lang.getMessage(player, "duel.setup.help"));
        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "duel.usage.delete"));
            return true;
        }

        String arenaName = args[1];
        DuelArena arena = plugin.getArenaManager().getDuelArena(arenaName);

        if (arena == null) {
            player.sendMessage(lang.getMessage(player, "duel.error.arena-not-found").replace("{0}", arenaName));
            return true;
        }

        plugin.getArenaManager().removeDuelArena(arenaName);
        player.sendMessage(lang.getMessage(player, "duel.arena-deleted").replace("{0}", arenaName));
        return true;
    }

    private boolean handleEnable(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "duel.usage.enable"));
            return true;
        }

        String arenaName = args[1];
        DuelArena arena = plugin.getArenaManager().getDuelArena(arenaName);

        if (arena == null) {
            player.sendMessage(lang.getMessage(player, "duel.error.arena-not-found").replace("{0}", arenaName));
            return true;
        }

        if (!arena.isValid()) {
            player.sendMessage(lang.getMessage(player, "duel.error.arena-invalid"));
            return true;
        }

        arena.setEnabled(true);
        arena.saveToConfig();
        player.sendMessage(lang.getMessage(player, "duel.arena-enabled").replace("{0}", arenaName));
        return true;
    }

    private boolean handleDisable(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "duel.usage.disable"));
            return true;
        }

        String arenaName = args[1];
        DuelArena arena = plugin.getArenaManager().getDuelArena(arenaName);

        if (arena == null) {
            player.sendMessage(lang.getMessage(player, "duel.error.arena-not-found").replace("{0}", arenaName));
            return true;
        }

        arena.setEnabled(false);
        arena.saveToConfig();
        player.sendMessage(lang.getMessage(player, "duel.arena-disabled").replace("{0}", arenaName));
        return true;
    }

    private boolean handleList(Player player) {
        List<DuelArena> arenas = plugin.getArenaManager().getAllDuelArenas();

        if (arenas.isEmpty()) {
            player.sendMessage(lang.getMessage(player, "duel.no-arenas"));
            return true;
        }

        player.sendMessage(lang.getMessage(player, "duel.arena-list-header"));
        for (DuelArena arena : arenas) {
            String status = arena.isEnabled() ?
                ChatColor.GREEN + lang.getMessage(player, "general.enabled") :
                ChatColor.RED + lang.getMessage(player, "general.disabled");
            player.sendMessage(ChatColor.YELLOW + "- " + arena.getName() + " " + status);
        }
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "duel.usage.info"));
            return true;
        }

        String arenaName = args[1];
        DuelArena arena = plugin.getArenaManager().getDuelArena(arenaName);

        if (arena == null) {
            player.sendMessage(lang.getMessage(player, "duel.error.arena-not-found").replace("{0}", arenaName));
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== " + arena.getName() + " ===");
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "duel.info.world") + ": " + ChatColor.WHITE + arena.getWorldName());
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "duel.info.enabled") + ": " + (arena.isEnabled() ? ChatColor.GREEN + lang.getMessage(player, "general.yes") : ChatColor.RED + lang.getMessage(player, "general.no")));
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "duel.info.in-use") + ": " + (arena.isInUse() ? ChatColor.RED + lang.getMessage(player, "general.yes") : ChatColor.GREEN + lang.getMessage(player, "general.no")));
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "duel.info.valid") + ": " + (arena.isValid() ? ChatColor.GREEN + lang.getMessage(player, "general.yes") : ChatColor.RED + lang.getMessage(player, "general.no")));
        return true;
    }

    private boolean handleSetPos1(Player player) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.DUEL) {
            player.sendMessage(lang.getMessage(player, "duel.error.no-setup-session"));
            return true;
        }

        session.setCorner1(player.getLocation());
        return true;
    }

    private boolean handleSetPos2(Player player) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.DUEL) {
            player.sendMessage(lang.getMessage(player, "duel.error.no-setup-session"));
            return true;
        }

        session.setCorner2(player.getLocation());
        return true;
    }

    private boolean handleSetSpawn1(Player player) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.DUEL) {
            player.sendMessage(lang.getMessage(player, "duel.error.no-setup-session"));
            return true;
        }

        session.setSpawn1(player.getLocation());
        return true;
    }

    private boolean handleSetSpawn2(Player player) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.DUEL) {
            player.sendMessage(lang.getMessage(player, "duel.error.no-setup-session"));
            return true;
        }

        session.setSpawn2(player.getLocation());
        return true;
    }

    private boolean handleComplete(Player player) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.DUEL) {
            player.sendMessage(lang.getMessage(player, "duel.error.no-setup-session"));
            return true;
        }

        if (session.complete()) {
            plugin.getArenaSetupManager().endSession(player);
        }
        return true;
    }

    private boolean handleCancel(Player player) {
        if (!player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (!plugin.getArenaSetupManager().hasSession(player)) {
            player.sendMessage(lang.getMessage(player, "duel.error.no-setup-session"));
            return true;
        }

        plugin.getArenaSetupManager().endSession(player);
        player.sendMessage(lang.getMessage(player, "duel.setup-cancelled"));
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== " + lang.getMessage(player, "duel.help.title") + " ===");
        player.sendMessage(ChatColor.YELLOW + "/duel join [kit]" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.join"));
        player.sendMessage(ChatColor.YELLOW + "/duel leave" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.leave"));
        player.sendMessage(ChatColor.YELLOW + "/duel invite <player> <kit>" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.invite"));
        player.sendMessage(ChatColor.YELLOW + "/duel accept" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.accept"));
        player.sendMessage(ChatColor.YELLOW + "/duel decline" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.decline"));
        player.sendMessage(ChatColor.YELLOW + "/duel list" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.list"));

        if (player.hasPermission("cubiom.admin.duel")) {
            player.sendMessage(ChatColor.GOLD + lang.getMessage(player, "duel.help.admin"));
            player.sendMessage(ChatColor.YELLOW + "/duel create <name>" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.create"));
            player.sendMessage(ChatColor.YELLOW + "/duel delete <name>" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.delete"));
            player.sendMessage(ChatColor.YELLOW + "/duel enable <name>" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.enable"));
            player.sendMessage(ChatColor.YELLOW + "/duel disable <name>" + ChatColor.GRAY + " - " + lang.getMessage(player, "duel.help.disable"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("join", "leave", "invite", "accept", "decline", "list", "info"));
            if (sender.hasPermission("cubiom.admin.duel")) {
                completions.addAll(Arrays.asList("create", "delete", "enable", "disable",
                    "setpos1", "setpos2", "setspawn1", "setspawn2", "complete", "cancel"));
            }
            return completions.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("invite")) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            if (subCmd.equals("join")) {
                return Arrays.asList("nodebuff", "debuff", "classic", "builduhc", "sg", "combo");
            }
            if (subCmd.equals("info") || subCmd.equals("delete") || subCmd.equals("enable") || subCmd.equals("disable")) {
                return plugin.getArenaManager().getAllDuelArenas().stream()
                    .map(DuelArena::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("invite")) {
            return Arrays.asList("nodebuff", "debuff", "classic", "builduhc", "sg", "combo");
        }

        return completions;
    }
}
