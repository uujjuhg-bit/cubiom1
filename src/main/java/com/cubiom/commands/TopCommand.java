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

public class TopCommand implements CommandExecutor, TabCompleter {

    private final Cubiom plugin;
    private final LanguageManager lang;

    public TopCommand(Cubiom plugin) {
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
        plugin.getGUIManager().openLeaderboards(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
