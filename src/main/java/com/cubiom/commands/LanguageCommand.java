package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import com.cubiom.player.CubiomPlayer;
import com.cubiom.ui.LobbyHotbar;
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

public class LanguageCommand implements CommandExecutor, TabCompleter {

    private final Cubiom plugin;
    private final LanguageManager lang;

    public LanguageCommand(Cubiom plugin) {
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
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);

        if (cp == null) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(lang.getMessage(player, "language.current").replace("{0}", cp.getLanguage()));
            player.sendMessage(lang.getMessage(player, "language.available"));
            for (String langCode : lang.getSupportedLanguages()) {
                String langName = lang.getLanguageName(langCode);
                player.sendMessage(ChatColor.YELLOW + "- " + langCode + ChatColor.GRAY + " (" + langName + ")");
            }
            player.sendMessage(lang.getMessage(player, "language.usage"));
            return true;
        }

        String subCmd = args[0].toLowerCase();

        if (subCmd.equals("set") || subCmd.equals("change")) {
            if (args.length < 2) {
                player.sendMessage(lang.getMessage(player, "language.usage"));
                return true;
            }

            String newLang = args[1];

            if (!lang.isValidLanguage(newLang)) {
                player.sendMessage(lang.getMessage(player, "language.invalid").replace("{0}", newLang));
                return true;
            }

            cp.setLanguage(newLang);
            plugin.getSupabaseManager().upsertPlayer(
                player.getUniqueId().toString(),
                player.getName(),
                newLang
            );

            player.sendMessage(lang.getMessage(player, "language.changed").replace("{0}", lang.getLanguageName(newLang)));

            LobbyHotbar.giveLobbyItems(player);

            plugin.getScoreboardManager().updateScoreboard(player);

            return true;
        }

        if (subCmd.equals("list")) {
            player.sendMessage(lang.getMessage(player, "language.available"));
            for (String langCode : lang.getSupportedLanguages()) {
                String langName = lang.getLanguageName(langCode);
                boolean current = langCode.equals(cp.getLanguage());
                String marker = current ? ChatColor.GREEN + "✓ " : "  ";
                player.sendMessage(marker + ChatColor.YELLOW + langCode + ChatColor.GRAY + " - " + langName);
            }
            return true;
        }

        player.sendMessage(lang.getMessage(player, "error.unknown-command"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "list", "change"));
            return completions.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("change"))) {
            return Arrays.stream(lang.getSupportedLanguages())
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return completions;
    }
}
