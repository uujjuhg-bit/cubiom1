package com.cubiom.world;

import com.cubiom.Cubiom;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class RollbackManager {

    private final Cubiom plugin;
    private final Map<String, WorldSnapshot> snapshots;
    private final Map<String, WorldSnapshot> changedBlocks;

    public RollbackManager(Cubiom plugin) {
        this.plugin = plugin;
        this.snapshots = new HashMap<>();
        this.changedBlocks = new HashMap<>();
    }

    public void createSnapshot(String arenaName, Location corner1, Location corner2) {
        WorldSnapshot snapshot = new WorldSnapshot();
        snapshot.captureArea(corner1, corner2);
        snapshots.put(arenaName, snapshot);

        changedBlocks.put(arenaName, new WorldSnapshot());
    }

    public void trackBlockChange(String arenaName, Block block) {
        WorldSnapshot changes = changedBlocks.get(arenaName);
        if (changes != null) {
            changes.captureBlock(block);
        }
    }

    public void restoreArena(String arenaName) {
        WorldSnapshot changes = changedBlocks.get(arenaName);
        if (changes != null) {
            changes.restore();
            changes.clear();
        }

        WorldSnapshot snapshot = snapshots.get(arenaName);
        if (snapshot != null) {
            snapshot.restore();
        }
    }

    public void clearSnapshot(String arenaName) {
        WorldSnapshot snapshot = snapshots.remove(arenaName);
        if (snapshot != null) {
            snapshot.clear();
        }

        WorldSnapshot changes = changedBlocks.remove(arenaName);
        if (changes != null) {
            changes.clear();
        }
    }

    public boolean hasSnapshot(String arenaName) {
        return snapshots.containsKey(arenaName);
    }

    public void shutdown() {
        snapshots.clear();
        changedBlocks.clear();
    }
}
