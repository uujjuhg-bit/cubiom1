package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SGCommand implements CommandExecutor {

    private final Cubiom plugin;

    public SGCommand(Cubiom plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== SG Commands ===");
            player.sendMessage(ChatColor.YELLOW + "/sg join <arena> - Join arena");
            player.sendMessage(ChatColor.YELLOW + "/sg leave - Leave game");
            player.sendMessage(ChatColor.YELLOW + "/sg create <arena> - Create arena (Admin)");
            player.sendMessage(ChatColor.YELLOW + "/sg setuparena <arena> - Setup arena (Admin)");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /sg join <arena>");
                    return true;
                }
                plugin.getSGManager().joinGame(player, args[1]);
                break;

            case "leave":
                plugin.getSGManager().leaveGame(player);
                player.sendMessage(ChatColor.YELLOW + "Left SG game!");
                break;

            case "create":
                if (!player.hasPermission("cubiom.admin")) {
                    player.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /sg create <arena>");
                    return true;
                }
                SGArena arena = new SGArena(args[1]);
                arena.setWorldName(player.getWorld().getName());
                plugin.getArenaManager().addSGArena(arena);
                player.sendMessage(ChatColor.GREEN + "Created SG arena: " + args[1]);
                break;

            case "setuparena":
                if (!player.hasPermission("cubiom.admin")) {
                    player.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /sg setuparena <arena>");
                    return true;
                }
                player.sendMessage(ChatColor.YELLOW + "Setup mode coming soon!");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand!");
                break;
        }

        return true;
    }
}
