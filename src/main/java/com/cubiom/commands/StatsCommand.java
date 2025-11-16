package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final Cubiom plugin;

    public StatsCommand(Cubiom plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            LanguageManager lang = plugin.getLanguageManager();
            sender.sendMessage(lang.getMessage("en_US", "general.player-only"));
            return true;
        }

        Player player = (Player) sender;
        plugin.getGUIManager().openStatsMenu(player);
        return true;
    }
}
