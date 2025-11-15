package com.cubiom.gamemodes.sg;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WorldSnapshot {

    private final Cubiom plugin;
    private final Arena arena;
    private final Set<BlockSnapshot> modifiedBlocks;
    private final Set<Location> placedBlocks;
    private boolean captured;

    public WorldSnapshot(Cubiom plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.modifiedBlocks = new HashSet<>();
        this.placedBlocks = new HashSet<>();
        this.captured = false;
    }

    public void captureSnapshot() {
        if (captured) {
            return;
        }

        World world = plugin.getServer().getWorld(arena.getWorldName());
        if (world == null) {
            plugin.getLogger().warning("Cannot capture snapshot: World not found");
            return;
        }

        Set<Chunk> chunksToLoad = new HashSet<>();

        for (Location loc : arena.getTier1Chests()) {
            chunksToLoad.add(loc.getChunk());
        }

        for (Location loc : arena.getTier2Chests()) {
            chunksToLoad.add(loc.getChunk());
        }

        for (Chunk chunk : chunksToLoad) {
            if (!chunk.isLoaded()) {
                chunk.load(true);
            }
        }

        for (Location loc : arena.getTier1Chests()) {
            BlockSnapshot snapshot = new BlockSnapshot(loc.getBlock());
            modifiedBlocks.add(snapshot);
        }

        for (Location loc : arena.getTier2Chests()) {
            BlockSnapshot snapshot = new BlockSnapshot(loc.getBlock());
            modifiedBlocks.add(snapshot);
        }

        captured = true;
    }

    public void trackBlockPlace(Location location) {
        placedBlocks.add(location);
    }

    public void trackBlockBreak(Block block) {
        if (!placedBlocks.contains(block.getLocation())) {
            BlockSnapshot snapshot = new BlockSnapshot(block);
            modifiedBlocks.add(snapshot);
        }
    }

    public void restoreAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                restore();
            }
        }.runTaskAsynchronously(plugin);
    }

    public void restore() {
        World world = plugin.getServer().getWorld(arena.getWorldName());
        if (world == null) {
            plugin.getLogger().warning("Cannot restore: World not found");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (BlockSnapshot snapshot : modifiedBlocks) {
                    snapshot.restore();
                }

                for (Location loc : placedBlocks) {
                    Block block = loc.getBlock();
                    block.setType(org.bukkit.Material.AIR);
                }

                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Item) {
                        entity.remove();
                    }
                }

                placedBlocks.clear();
                modifiedBlocks.clear();
                captured = false;
            }
        }.runTask(plugin);
    }

    public boolean isCaptured() {
        return captured;
    }
}
