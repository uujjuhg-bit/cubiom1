package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.arena.ArenaSetupSession;
import com.cubiom.arena.SGArena;
import com.cubiom.language.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SGCommand implements CommandExecutor, TabCompleter {

    private final Cubiom plugin;
    private final LanguageManager lang;

    public SGCommand(Cubiom plugin) {
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
            case "setlobby":
                return handleSetLobby(player);
            case "addspawn":
                return handleAddSpawn(player);
            case "addtier1":
                return handleAddTier1(player);
            case "addtier2":
                return handleAddTier2(player);
            case "setdm":
                return handleSetDeathmatch(player);
            case "setspectator":
                return handleSetSpectator(player);
            case "setmin":
                return handleSetMin(player, args);
            case "setmax":
                return handleSetMax(player, args);
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
            plugin.getGUIManager().openSGArenaSelector(player);
            return true;
        }

        String arenaName = args[1];
        plugin.getSGManager().joinGame(player, arenaName);
        return true;
    }

    private boolean handleLeave(Player player) {
        plugin.getSGManager().leaveGame(player);
        player.sendMessage(lang.getMessage(player, "sg.left-game"));
        return true;
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "sg.usage.create"));
            return true;
        }

        String arenaName = args[1];

        if (plugin.getArenaManager().getSGArena(arenaName) != null) {
            player.sendMessage(lang.getMessage(player, "sg.error.arena-exists").replace("{0}", arenaName));
            return true;
        }

        plugin.getArenaSetupManager().startSession(player, arenaName, ArenaSetupSession.ArenaType.SURVIVAL_GAMES);
        player.sendMessage(lang.getMessage(player, "sg.setup.started").replace("{0}", arenaName));
        player.sendMessage(lang.getMessage(player, "sg.setup.help"));
        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "sg.usage.delete"));
            return true;
        }

        String arenaName = args[1];
        SGArena arena = plugin.getArenaManager().getSGArena(arenaName);

        if (arena == null) {
            player.sendMessage(lang.getMessage(player, "sg.error.arena-not-found").replace("{0}", arenaName));
            return true;
        }

        plugin.getArenaManager().removeSGArena(arenaName);
        player.sendMessage(lang.getMessage(player, "sg.arena-deleted").replace("{0}", arenaName));
        return true;
    }

    private boolean handleEnable(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "sg.usage.enable"));
            return true;
        }

        String arenaName = args[1];
        SGArena arena = plugin.getArenaManager().getSGArena(arenaName);

        if (arena == null) {
            player.sendMessage(lang.getMessage(player, "sg.error.arena-not-found").replace("{0}", arenaName));
            return true;
        }

        if (!arena.isValid()) {
            player.sendMessage(lang.getMessage(player, "sg.error.arena-invalid"));
            return true;
        }

        arena.setEnabled(true);
        arena.saveToConfig();
        player.sendMessage(lang.getMessage(player, "sg.arena-enabled").replace("{0}", arenaName));
        return true;
    }

    private boolean handleDisable(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "sg.usage.disable"));
            return true;
        }

        String arenaName = args[1];
        SGArena arena = plugin.getArenaManager().getSGArena(arenaName);

        if (arena == null) {
            player.sendMessage(lang.getMessage(player, "sg.error.arena-not-found").replace("{0}", arenaName));
            return true;
        }

        arena.setEnabled(false);
        arena.saveToConfig();
        player.sendMessage(lang.getMessage(player, "sg.arena-disabled").replace("{0}", arenaName));
        return true;
    }

    private boolean handleList(Player player) {
        List<SGArena> arenas = plugin.getArenaManager().getAllSGArenas();

        if (arenas.isEmpty()) {
            player.sendMessage(lang.getMessage(player, "sg.no-arenas"));
            return true;
        }

        player.sendMessage(lang.getMessage(player, "sg.arena-list-header"));
        for (SGArena arena : arenas) {
            String status = arena.isEnabled() ?
                ChatColor.GREEN + lang.getMessage(player, "general.enabled") :
                ChatColor.RED + lang.getMessage(player, "general.disabled");
            player.sendMessage(ChatColor.YELLOW + "- " + arena.getName() + " " + status);
        }
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "sg.usage.info"));
            return true;
        }

        String arenaName = args[1];
        SGArena arena = plugin.getArenaManager().getSGArena(arenaName);

        if (arena == null) {
            player.sendMessage(lang.getMessage(player, "sg.error.arena-not-found").replace("{0}", arenaName));
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== " + arena.getName() + " ===");
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "sg.info.world") + ": " + ChatColor.WHITE + arena.getWorldName());
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "sg.info.enabled") + ": " + (arena.isEnabled() ? ChatColor.GREEN + lang.getMessage(player, "general.yes") : ChatColor.RED + lang.getMessage(player, "general.no")));
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "sg.info.players") + ": " + ChatColor.WHITE + arena.getMinPlayers() + "-" + arena.getMaxPlayers());
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "sg.info.spawns") + ": " + ChatColor.WHITE + arena.getPlayerSpawns().size());
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "sg.info.tier1") + ": " + ChatColor.WHITE + arena.getTier1Chests().size());
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "sg.info.tier2") + ": " + ChatColor.WHITE + arena.getTier2Chests().size());
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "sg.info.deathmatch") + ": " + ChatColor.WHITE + arena.getDeathmatchSpawns().size());
        player.sendMessage(ChatColor.YELLOW + lang.getMessage(player, "sg.info.valid") + ": " + (arena.isValid() ? ChatColor.GREEN + lang.getMessage(player, "general.yes") : ChatColor.RED + lang.getMessage(player, "general.no")));
        return true;
    }

    private boolean handleSetLobby(Player player) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        session.setLobbySpawn(player.getLocation());
        return true;
    }

    private boolean handleAddSpawn(Player player) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        session.addPlayerSpawn(player.getLocation());
        return true;
    }

    private boolean handleAddTier1(Player player) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        Block target = player.getTargetBlock((java.util.Set<Material>) null, 5);
        if (target == null || target.getType() != Material.CHEST) {
            player.sendMessage(lang.getMessage(player, "sg.error.not-looking-at-chest"));
            return true;
        }

        session.addTier1Chest(target);
        return true;
    }

    private boolean handleAddTier2(Player player) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        Block target = player.getTargetBlock((java.util.Set<Material>) null, 5);
        if (target == null || target.getType() != Material.CHEST) {
            player.sendMessage(lang.getMessage(player, "sg.error.not-looking-at-chest"));
            return true;
        }

        session.addTier2Chest(target);
        return true;
    }

    private boolean handleSetDeathmatch(Player player) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        session.setDeathmatchSpawn(player.getLocation());
        return true;
    }

    private boolean handleSetSpectator(Player player) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        session.setSpectatorSpawn(player.getLocation());
        return true;
    }

    private boolean handleSetMin(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "sg.usage.setmin"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        try {
            int min = Integer.parseInt(args[1]);
            if (min < 2) {
                player.sendMessage(lang.getMessage(player, "sg.error.min-too-low"));
                return true;
            }
            session.setMinPlayers(min);
        } catch (NumberFormatException e) {
            player.sendMessage(lang.getMessage(player, "error.invalid-number"));
        }
        return true;
    }

    private boolean handleSetMax(Player player, String[] args) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(lang.getMessage(player, "sg.usage.setmax"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        try {
            int max = Integer.parseInt(args[1]);
            if (max < 2) {
                player.sendMessage(lang.getMessage(player, "sg.error.max-too-low"));
                return true;
            }
            session.setMaxPlayers(max);
        } catch (NumberFormatException e) {
            player.sendMessage(lang.getMessage(player, "error.invalid-number"));
        }
        return true;
    }

    private boolean handleComplete(Player player) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        ArenaSetupSession session = plugin.getArenaSetupManager().getSession(player);
        if (session == null || session.getType() != ArenaSetupSession.ArenaType.SURVIVAL_GAMES) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        if (session.complete()) {
            plugin.getArenaSetupManager().endSession(player);
        }
        return true;
    }

    private boolean handleCancel(Player player) {
        if (!player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(lang.getMessage(player, "error.no-permission"));
            return true;
        }

        if (!plugin.getArenaSetupManager().hasSession(player)) {
            player.sendMessage(lang.getMessage(player, "sg.error.no-setup-session"));
            return true;
        }

        plugin.getArenaSetupManager().endSession(player);
        player.sendMessage(lang.getMessage(player, "sg.setup-cancelled"));
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== " + lang.getMessage(player, "sg.help.title") + " ===");
        player.sendMessage(ChatColor.YELLOW + "/sg join [arena]" + ChatColor.GRAY + " - " + lang.getMessage(player, "sg.help.join"));
        player.sendMessage(ChatColor.YELLOW + "/sg leave" + ChatColor.GRAY + " - " + lang.getMessage(player, "sg.help.leave"));
        player.sendMessage(ChatColor.YELLOW + "/sg list" + ChatColor.GRAY + " - " + lang.getMessage(player, "sg.help.list"));
        player.sendMessage(ChatColor.YELLOW + "/sg info <arena>" + ChatColor.GRAY + " - " + lang.getMessage(player, "sg.help.info"));

        if (player.hasPermission("cubiom.admin.sg")) {
            player.sendMessage(ChatColor.GOLD + lang.getMessage(player, "sg.help.admin"));
            player.sendMessage(ChatColor.YELLOW + "/sg create <name>" + ChatColor.GRAY + " - " + lang.getMessage(player, "sg.help.create"));
            player.sendMessage(ChatColor.YELLOW + "/sg delete <name>" + ChatColor.GRAY + " - " + lang.getMessage(player, "sg.help.delete"));
            player.sendMessage(ChatColor.YELLOW + "/sg enable <name>" + ChatColor.GRAY + " - " + lang.getMessage(player, "sg.help.enable"));
            player.sendMessage(ChatColor.YELLOW + "/sg disable <name>" + ChatColor.GRAY + " - " + lang.getMessage(player, "sg.help.disable"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("join", "leave", "list", "info"));
            if (sender.hasPermission("cubiom.admin.sg")) {
                completions.addAll(Arrays.asList("create", "delete", "enable", "disable",
                    "setlobby", "addspawn", "addtier1", "addtier2", "setdm", "setspectator",
                    "setmin", "setmax", "complete", "cancel"));
            }
            return completions.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("join") || subCmd.equals("info") || subCmd.equals("delete") ||
                subCmd.equals("enable") || subCmd.equals("disable")) {
                return plugin.getArenaManager().getAllSGArenas().stream()
                    .map(SGArena::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }

        return completions;
    }
}
