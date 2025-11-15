package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LanguageCommand implements CommandExecutor {

    private final Cubiom plugin;

    public LanguageCommand(Cubiom plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        LanguageManager langManager = plugin.getLanguageManager();

        if (args.length == 0) {
            player.sendMessage("§b§lAvailable Languages:");
            for (String lang : langManager.getSupportedLanguages()) {
                player.sendMessage("§7- §b" + lang + " §8(§7" + langManager.getLanguageName(lang) + "§8)");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("set") && args.length >= 2) {
            String language = args[1];

            if (!langManager.isValidLanguage(language)) {
                player.sendMessage("§cInvalid language! Use /lang to see available languages.");
                return true;
            }

            plugin.getDataManager().setPlayerLanguage(player.getUniqueId(), language);
            player.sendMessage(langManager.getMessageWithPrefix(language, "general.reload-success"));

        } else if (subCommand.equals("list")) {
            player.sendMessage("§b§lAvailable Languages:");
            for (String lang : langManager.getSupportedLanguages()) {
                player.sendMessage("§7- §b" + lang + " §8(§7" + langManager.getLanguageName(lang) + "§8)");
            }
        } else {
            return false;
        }

        return true;
    }
}
