package com.cubiom.utils;

import com.cubiom.Cubiom;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class WorldManager {

    private final Cubiom plugin;
    private boolean multiverseAvailable;

    public WorldManager(Cubiom plugin) {
        this.plugin = plugin;
        this.multiverseAvailable = checkMultiverseCore();
    }

    private boolean checkMultiverseCore() {
        Plugin mv = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        return mv != null && mv.isEnabled();
    }

    public void setupSGWorld(World world) {
        if (world == null) {
            plugin.getLogger().warning("Cannot setup world: World is null");
            return;
        }

        plugin.getLogger().info("Setting up SG world: " + world.getName());

        world.setDifficulty(Difficulty.NORMAL);
        world.setPVP(true);
        world.setSpawnFlags(false, false);
        world.setKeepSpawnInMemory(true);

        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("mobGriefing", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("commandBlockOutput", "false");
        world.setGameRuleValue("logAdminCommands", "false");
        world.setGameRuleValue("showDeathMessages", "false");

        world.setTime(6000);
        world.setStorm(false);
        world.setThundering(false);

        plugin.getLogger().info("World " + world.getName() + " configured for SG");

        if (multiverseAvailable) {
            setupMultiverseFlags(world, "survival");
        }
    }

    public void setupDuelWorld(World world) {
        if (world == null) {
            plugin.getLogger().warning("Cannot setup world: World is null");
            return;
        }

        plugin.getLogger().info("Setting up Duel world: " + world.getName());

        world.setDifficulty(Difficulty.NORMAL);
        world.setPVP(true);
        world.setSpawnFlags(false, false);
        world.setKeepSpawnInMemory(true);

        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("mobGriefing", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("commandBlockOutput", "false");
        world.setGameRuleValue("logAdminCommands", "false");
        world.setGameRuleValue("showDeathMessages", "false");

        world.setTime(6000);
        world.setStorm(false);
        world.setThundering(false);

        plugin.getLogger().info("World " + world.getName() + " configured for Duels");

        if (multiverseAvailable) {
            setupMultiverseFlags(world, "duel");
        }
    }

    private void setupMultiverseFlags(World world, String type) {
        plugin.getLogger().info("Configuring Multiverse flags for: " + world.getName());

        plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "mv modify set pvp true " + world.getName()
        );

        plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "mv modify set monsters false " + world.getName()
        );

        plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "mv modify set animals false " + world.getName()
        );

        plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "mv modify set weather false " + world.getName()
        );

        plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "mv modify set difficulty NORMAL " + world.getName()
        );

        if (type.equals("duel")) {
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(),
                    "mv modify set hunger false " + world.getName()
            );
        }

        plugin.getLogger().info("Multiverse configuration complete for: " + world.getName());
    }

    public boolean isMultiverseAvailable() {
        return multiverseAvailable;
    }

    public void printWorldInfo(World world) {
        if (world == null) return;

        plugin.getLogger().info("=== World Info: " + world.getName() + " ===");
        plugin.getLogger().info("  Difficulty: " + world.getDifficulty());
        plugin.getLogger().info("  PVP: " + world.getPVP());
        plugin.getLogger().info("  Mob Spawning: " + world.getGameRuleValue("doMobSpawning"));
        plugin.getLogger().info("  Fire Tick: " + world.getGameRuleValue("doFireTick"));
        plugin.getLogger().info("  Daylight Cycle: " + world.getGameRuleValue("doDaylightCycle"));
        plugin.getLogger().info("  Weather Cycle: " + world.getGameRuleValue("doWeatherCycle"));
        plugin.getLogger().info("  Mob Griefing: " + world.getGameRuleValue("mobGriefing"));
        plugin.getLogger().info("  Multiverse: " + (multiverseAvailable ? "Available" : "Not available"));
    }
}
