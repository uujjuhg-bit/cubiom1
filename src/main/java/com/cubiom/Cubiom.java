package com.cubiom;

import com.cubiom.api.ConfigManager;
import com.cubiom.api.DataManager;
import com.cubiom.commands.*;
import com.cubiom.gamemodes.duels.DuelManager;
import com.cubiom.gamemodes.sg.SGManager;
import com.cubiom.inventory.GUIManager;
import com.cubiom.language.LanguageManager;
import com.cubiom.listeners.*;
import com.cubiom.stats.StatsManager;
import com.cubiom.utils.HotbarManager;
import com.cubiom.utils.WorldManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Cubiom extends JavaPlugin {

    private static Cubiom instance;

    private ConfigManager configManager;
    private DataManager dataManager;
    private LanguageManager languageManager;
    private StatsManager statsManager;
    private SGManager sgManager;
    private DuelManager duelManager;
    private GUIManager guiManager;
    private HotbarManager hotbarManager;
    private WorldManager worldManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Enabling Cubiom v1.0...");

        try {
            saveDefaultConfig();

            configManager = new ConfigManager(this);
            configManager.load();

            dataManager = new DataManager(this);
            dataManager.load();

            languageManager = new LanguageManager(this);
            languageManager.load();

            statsManager = new StatsManager(this);
            statsManager.load();

            sgManager = new SGManager(this);
            duelManager = new DuelManager(this);

            guiManager = new GUIManager(this);
            hotbarManager = new HotbarManager(this);
            worldManager = new WorldManager(this);

            registerCommands();
            registerListeners();

            startAutoSave();

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

            if (statsManager != null) {
                statsManager.save();
            }

            if (dataManager != null) {
                dataManager.save();
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
        getCommand("lang").setExecutor(new LanguageCommand(this));
        getCommand("cubiom").setExecutor(new CubiomCommand(this));
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new EntityDamageListener(this), this);
    }

    private void startAutoSave() {
        int saveInterval = getConfig().getInt("stats.save-interval", 300) * 20;

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                statsManager.save();
                dataManager.save();
            } catch (Exception e) {
                getLogger().warning("Auto-save failed: " + e.getMessage());
            }
        }, saveInterval, saveInterval);
    }

    public void reload() {
        try {
            reloadConfig();
            configManager.load();
            dataManager.load();
            languageManager.load();

            getLogger().info("Cubiom reloaded successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to reload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Cubiom getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
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

    public HotbarManager getHotbarManager() {
        return hotbarManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }
}
