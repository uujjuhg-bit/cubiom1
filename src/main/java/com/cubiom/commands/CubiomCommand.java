package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CubiomCommand implements CommandExecutor {

    private final Cubiom plugin;

    public CubiomCommand(Cubiom plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§b§lCubiom §8v1.0");
            sender.sendMessage("§7/cubiom reload §8- §7Reload plugin");
            sender.sendMessage("§7/cubiom version §8- §7Show version");
            sender.sendMessage("§7/cubiom help §8- §7Show help");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("cubiom.admin")) {
                    if (sender instanceof Player) {
                        LanguageManager langManager = plugin.getLanguageManager();
                        sender.sendMessage(langManager.getMessageWithPrefix((Player) sender, "general.no-permission"));
                    } else {
                        sender.sendMessage("§cNo permission!");
                    }
                    return true;
                }
                handleReload(sender);
                break;

            case "version":
                sender.sendMessage("§b§lCubiom §8v1.0");
                sender.sendMessage("§7Running on Spigot 1.8.8");
                break;

            case "help":
                sender.sendMessage("§b§lCubiom Commands:");
                sender.sendMessage("§7/sg join §8- §7Join Survival Games");
                sender.sendMessage("§7/duel join §8- §7Join Duel Queue");
                sender.sendMessage("§7/lang set <code> §8- §7Change language");
                break;

            default:
                sender.sendMessage("§cUnknown command. Use /cubiom help");
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.reload();
            if (sender instanceof Player) {
                LanguageManager langManager = plugin.getLanguageManager();
                sender.sendMessage(langManager.getMessageWithPrefix((Player) sender, "general.reload-success"));
            } else {
                sender.sendMessage("§aPlugin reloaded successfully!");
            }
        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
