package com.cubiom.arena;

import com.cubiom.Cubiom;
import com.cubiom.core.GameType;

import java.io.File;
import java.util.*;

public class ArenaManager {

    private final Cubiom plugin;
    private final Map<String, SGArena> sgArenas;
    private final Map<String, DuelArena> duelArenas;

    public ArenaManager(Cubiom plugin) {
        this.plugin = plugin;
        this.sgArenas = new HashMap<>();
        this.duelArenas = new HashMap<>();
    }

    public void loadArenas() {
        File sgDir = new File(plugin.getDataFolder(), "arenas/sg");
        if (sgDir.exists() && sgDir.isDirectory()) {
            for (File file : sgDir.listFiles()) {
                if (file.getName().endsWith(".yml")) {
                    String name = file.getName().replace(".yml", "");
                    SGArena arena = new SGArena(name);
                    arena.loadFromConfig();
                    sgArenas.put(name, arena);
                }
            }
        }

        File duelDir = new File(plugin.getDataFolder(), "arenas/duels");
        if (duelDir.exists() && duelDir.isDirectory()) {
            for (File file : duelDir.listFiles()) {
                if (file.getName().endsWith(".yml")) {
                    String name = file.getName().replace(".yml", "");
                    DuelArena arena = new DuelArena(name);
                    arena.loadFromConfig();
                    duelArenas.put(name, arena);
                }
            }
        }

        plugin.getLogger().info("Loaded " + sgArenas.size() + " SG arenas and " + duelArenas.size() + " Duel arenas");
    }

    public SGArena getSGArena(String name) {
        return sgArenas.get(name);
    }

    public DuelArena getDuelArena(String name) {
        return duelArenas.get(name);
    }

    public void addSGArena(SGArena arena) {
        sgArenas.put(arena.getName(), arena);
        arena.saveToConfig();
    }

    public void addDuelArena(DuelArena arena) {
        duelArenas.put(arena.getName(), arena);
        arena.saveToConfig();
    }

    public void removeSGArena(String name) {
        sgArenas.remove(name);
        File file = new File(plugin.getDataFolder(), "arenas/sg/" + name + ".yml");
        if (file.exists()) file.delete();
    }

    public void removeDuelArena(String name) {
        duelArenas.remove(name);
        File file = new File(plugin.getDataFolder(), "arenas/duels/" + name + ".yml");
        if (file.exists()) file.delete();
    }

    public List<SGArena> getAllSGArenas() {
        return new ArrayList<>(sgArenas.values());
    }

    public List<DuelArena> getAllDuelArenas() {
        return new ArrayList<>(duelArenas.values());
    }

    public List<SGArena> getEnabledSGArenas() {
        List<SGArena> enabled = new ArrayList<>();
        for (SGArena arena : sgArenas.values()) {
            if (arena.isEnabled()) enabled.add(arena);
        }
        return enabled;
    }

    public List<DuelArena> getAvailableDuelArenas() {
        List<DuelArena> available = new ArrayList<>();
        for (DuelArena arena : duelArenas.values()) {
            if (arena.isEnabled() && !arena.isInUse()) available.add(arena);
        }
        return available;
    }

    public DuelArena getRandomAvailableDuelArena() {
        List<DuelArena> available = getAvailableDuelArenas();
        if (available.isEmpty()) return null;
        return available.get(new Random().nextInt(available.size()));
    }
}
