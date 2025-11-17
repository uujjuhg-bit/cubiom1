package com.cubiom;

import com.cubiom.arena.ArenaManager;
import com.cubiom.commands.DuelCommand;
import com.cubiom.commands.SGCommand;
import com.cubiom.database.SupabaseManager;
import com.cubiom.game.duel.DuelManager;
import com.cubiom.game.sg.SGManager;
import com.cubiom.language.LanguageManager;
import com.cubiom.listeners.*;
import com.cubiom.player.PlayerManager;
import com.cubiom.ui.GUIManager;
import com.cubiom.ui.ScoreboardManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Cubiom extends JavaPlugin {

    private static Cubiom instance;

    private SupabaseManager supabaseManager;
    private LanguageManager languageManager;
    private PlayerManager playerManager;
    private ArenaManager arenaManager;
    private SGManager sgManager;
    private DuelManager duelManager;
    private GUIManager guiManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Enabling Cubiom v2.0...");

        try {
            saveDefaultConfig();

            supabaseManager = new SupabaseManager(this);

            languageManager = new LanguageManager(this);
            languageManager.load();

            playerManager = new PlayerManager(this);
            arenaManager = new ArenaManager(this);
            sgManager = new SGManager(this);
            duelManager = new DuelManager(this);
            guiManager = new GUIManager(this);
            scoreboardManager = new ScoreboardManager(this);

            arenaManager.loadArenas();

            registerCommands();
            registerListeners();

            getLogger().info("Cubiom enabled successfully!");

        } catch (Exception e) {
            getLogger().severe("Failed to enable Cubiom: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling Cubiom...");

        try {
            if (sgManager != null) {
                sgManager.shutdown();
            }

            if (duelManager != null) {
                duelManager.shutdown();
            }

            if (playerManager != null) {
                playerManager.shutdown();
            }

            if (scoreboardManager != null) {
                scoreboardManager.shutdown();
            }

            if (supabaseManager != null) {
                supabaseManager.shutdown();
            }

            getLogger().info("Cubiom disabled successfully!");

        } catch (Exception e) {
            getLogger().severe("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        getCommand("sg").setExecutor(new SGCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new PlayerInteractEntityListener(this), this);
        pm.registerEvents(new EntityDamageListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);
    }

    public static Cubiom getInstance() {
        return instance;
    }

    public SupabaseManager getSupabaseManager() {
        return supabaseManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public SGManager getSGManager() {
        return sgManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
