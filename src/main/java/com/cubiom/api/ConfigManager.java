package com.cubiom.api;

import com.cubiom.Cubiom;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Cubiom plugin;

    public ConfigManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getLogger().info("Loading configuration...");
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String getPrefix() {
        return plugin.getConfig().getString("cubiom.prefix", "&b&lCUBIOM &8»&r ");
    }

    public String getDefaultLanguage() {
        return plugin.getConfig().getString("cubiom.default-language", "en_US");
    }

    public int getSGMinPlayers() {
        return plugin.getConfig().getInt("survival-games.min-players", 8);
    }

    public int getSGMaxPlayers() {
        return plugin.getConfig().getInt("survival-games.max-players", 24);
    }

    public int getSGGracePeriod() {
        return plugin.getConfig().getInt("survival-games.grace-period", 60);
    }

    public int getSGRefillTime() {
        return plugin.getConfig().getInt("survival-games.refill-time", 180);
    }

    public int getSGDeathmatchTime() {
        return plugin.getConfig().getInt("survival-games.deathmatch-time", 600);
    }

    public int getSGCountdown() {
        return plugin.getConfig().getInt("survival-games.countdown", 10);
    }

    public int getDuelMinElo() {
        return plugin.getConfig().getInt("duels.min-elo", 0);
    }

    public int getDuelDefaultElo() {
        return plugin.getConfig().getInt("duels.default-elo", 1000);
    }

    public int getDuelEloKFactor() {
        return plugin.getConfig().getInt("duels.elo-k-factor", 32);
    }

    public int getDuelTimeout() {
        return plugin.getConfig().getInt("duels.duel-timeout", 600);
    }
}
