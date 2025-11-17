package com.cubiom.language;

import com.cubiom.Cubiom;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LanguageManager {

    private final Cubiom plugin;
    private final Map<String, YamlConfiguration> languages;
    private final String[] supportedLanguages = {"en_US", "da_DK", "de_DE", "es_ES"};

    public LanguageManager(Cubiom plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
    }

    public void load() {
        plugin.getLogger().info("Loading language files...");

        File languagesFolder = new File(plugin.getDataFolder(), "languages");
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs();
        }

        for (String lang : supportedLanguages) {
            File langFile = new File(languagesFolder, lang + ".yml");

            if (!langFile.exists()) {
                plugin.saveResource("languages/" + lang + ".yml", false);
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
            languages.put(lang, config);
            plugin.getLogger().info("Loaded language: " + lang);
        }
    }

    public String getMessage(Player player, String key) {
        String lang = "en_US";
        if (plugin.getPlayerManager() != null) {
            com.cubiom.player.CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
            if (cp != null) {
                lang = cp.getLanguage();
            }
        }
        return getMessage(lang, key);
    }

    public String getMessage(String language, String key) {
        YamlConfiguration config = languages.get(language);

        if (config == null) {
            config = languages.get("en_US");
        }

        String message = config.getString(key);

        if (message == null) {
            return ChatColor.RED + "Missing translation: " + key;
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessageWithPrefix(Player player, String key) {
        return getMessage(player, "general.prefix") + getMessage(player, key);
    }

    public String getMessageWithPrefix(String language, String key) {
        return getMessage(language, "general.prefix") + getMessage(language, key);
    }

    public String formatMessage(Player player, String key, Map<String, String> replacements) {
        String message = getMessage(player, key);
        return formatString(message, replacements);
    }

    public String formatMessage(String language, String key, Map<String, String> replacements) {
        String message = getMessage(language, key);
        return formatString(message, replacements);
    }

    private String formatString(String message, Map<String, String> replacements) {
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return message;
    }

    public String[] getSupportedLanguages() {
        return supportedLanguages;
    }

    public String getLanguageName(String code) {
        YamlConfiguration config = languages.get(code);
        if (config == null) {
            return code;
        }
        return config.getString("language.name", code);
    }

    public boolean isValidLanguage(String code) {
        for (String lang : supportedLanguages) {
            if (lang.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public java.util.List<String> getMessageList(Player player, String key) {
        String lang = "en_US";
        if (plugin.getPlayerManager() != null) {
            com.cubiom.player.CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
            if (cp != null) {
                lang = cp.getLanguage();
            }
        }
        return getMessageList(lang, key);
    }

    public java.util.List<String> getMessageList(String language, String key) {
        YamlConfiguration config = languages.get(language);

        if (config == null) {
            config = languages.get("en_US");
        }

        java.util.List<String> messages = config.getStringList(key);

        if (messages == null || messages.isEmpty()) {
            return java.util.Collections.singletonList(ChatColor.RED + "Missing translation: " + key);
        }

        java.util.List<String> colored = new java.util.ArrayList<>();
        for (String line : messages) {
            colored.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return colored;
    }
}
