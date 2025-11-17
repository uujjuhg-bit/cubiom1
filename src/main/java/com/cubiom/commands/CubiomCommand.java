package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
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

public class CubiomCommand implements CommandExecutor, TabCompleter {

    private final Cubiom plugin;
    private final LanguageManager lang;

    public CubiomCommand(Cubiom plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "reload":
                return handleReload(sender);
            case "version":
            case "ver":
                return handleVersion(sender);
            case "help":
                return handleHelp(sender);
            case "setlobby":
                return handleSetLobby(sender);
            default:
                sender.sendMessage(getMessage(sender, "error.unknown-command"));
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("cubiom.admin")) {
            sender.sendMessage(getMessage(sender, "error.no-permission"));
            return true;
        }

        long startTime = System.currentTimeMillis();
        sender.sendMessage(getMessage(sender, "cubiom.reloading"));

        try {
            plugin.reloadConfig();

            lang.load();

            plugin.getArenaManager().loadArenas();

            long elapsed = System.currentTimeMillis() - startTime;
            sender.sendMessage(getMessage(sender, "cubiom.reloaded").replace("{0}", String.valueOf(elapsed)));

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error during reload: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Cubiom Plugin ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + String.join(", ", plugin.getDescription().getAuthors()));
        sender.sendMessage(ChatColor.YELLOW + "Minecraft: " + ChatColor.WHITE + "1.8.8");
        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + getMessage(sender, "cubiom.help.title") + " ===");
        sender.sendMessage(ChatColor.YELLOW + "/cubiom" + ChatColor.GRAY + " - " + getMessage(sender, "cubiom.help.main"));
        sender.sendMessage(ChatColor.YELLOW + "/cubiom version" + ChatColor.GRAY + " - " + getMessage(sender, "cubiom.help.version"));
        sender.sendMessage(ChatColor.YELLOW + "/cubiom help" + ChatColor.GRAY + " - " + getMessage(sender, "cubiom.help.help"));

        if (sender.hasPermission("cubiom.admin")) {
            sender.sendMessage(ChatColor.GOLD + getMessage(sender, "cubiom.help.admin"));
            sender.sendMessage(ChatColor.YELLOW + "/cubiom reload" + ChatColor.GRAY + " - " + getMessage(sender, "cubiom.help.reload"));
            sender.sendMessage(ChatColor.YELLOW + "/cubiom setlobby" + ChatColor.GRAY + " - " + getMessage(sender, "cubiom.help.setlobby"));
        }

        return true;
    }

    private boolean handleSetLobby(CommandSender sender) {
        if (!sender.hasPermission("cubiom.admin")) {
            sender.sendMessage(getMessage(sender, "error.no-permission"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage(sender, "error.player-only"));
            return true;
        }

        Player player = (Player) sender;
        org.bukkit.Location loc = player.getLocation();

        String locationString = loc.getWorld().getName() + "," +
            loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," +
            loc.getYaw() + "," + loc.getPitch();

        plugin.getConfig().set("lobby.spawn", locationString);
        plugin.saveConfig();

        player.sendMessage(getMessage(sender, "cubiom.lobby-set"));
        return true;
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "CUBIOM");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------");
        sender.sendMessage(ChatColor.YELLOW + "Minecraft PvP Plugin");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------");
        sender.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/cubiom help" + ChatColor.YELLOW + " for commands");
    }

    private String getMessage(CommandSender sender, String key) {
        if (sender instanceof Player) {
            return lang.getMessage((Player) sender, key);
        }
        return lang.getMessage("en_US", key);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "version", "ver"));
            if (sender.hasPermission("cubiom.admin")) {
                completions.addAll(Arrays.asList("reload", "setlobby"));
            }
            return completions.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        return completions;
    }
}
