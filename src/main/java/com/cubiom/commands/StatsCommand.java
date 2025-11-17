package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatsCommand implements CommandExecutor, TabCompleter {

    private final Cubiom plugin;
    private final LanguageManager lang;

    public StatsCommand(Cubiom plugin) {
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
        Player target = player;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(lang.getMessage(player, "error.player-not-found").replace("{0}", args[0]));
                return true;
            }
        }

        plugin.getGUIManager().openPlayerProfile(target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
