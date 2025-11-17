package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.arena.DuelArena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {

    private final Cubiom plugin;

    public DuelCommand(Cubiom plugin) {
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
            player.sendMessage(ChatColor.GOLD + "=== Duel Commands ===");
            player.sendMessage(ChatColor.YELLOW + "/duel <player> - Challenge player");
            player.sendMessage(ChatColor.YELLOW + "/duel accept - Accept invite");
            player.sendMessage(ChatColor.YELLOW + "/duel decline - Decline invite");
            player.sendMessage(ChatColor.YELLOW + "/duel queue <kit> - Join queue");
            player.sendMessage(ChatColor.YELLOW + "/duel leave - Leave queue");
            player.sendMessage(ChatColor.YELLOW + "/duel create <arena> - Create arena (Admin)");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "accept":
                plugin.getDuelManager().acceptDuelInvite(player);
                break;

            case "decline":
                plugin.getDuelManager().declineDuelInvite(player);
                break;

            case "queue":
                if (args.length < 2) {
                    plugin.getGUIManager().openKitSelector(player);
                    return true;
                }
                plugin.getDuelManager().joinQueue(player, args[1]);
                break;

            case "leave":
                plugin.getDuelManager().leaveQueue(player);
                break;

            case "create":
                if (!player.hasPermission("cubiom.admin")) {
                    player.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /duel create <arena>");
                    return true;
                }
                DuelArena arena = new DuelArena(args[1]);
                arena.setWorldName(player.getWorld().getName());
                plugin.getArenaManager().addDuelArena(arena);
                player.sendMessage(ChatColor.GREEN + "Created Duel arena: " + args[1]);
                break;

            default:
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage(ChatColor.RED + "You cannot duel yourself!");
                    return true;
                }
                plugin.getGUIManager().openKitSelector(player);
                break;
        }

        return true;
    }
}
