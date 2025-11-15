package com.cubiom.arenas;

import org.bukkit.Location;

public class DuelArena {

    private String name;
    private String worldName;
    private Location pos1;
    private Location pos2;
    private Location spawn1;
    private Location spawn2;
    private boolean enabled;
    private boolean inUse;

    public DuelArena(String name) {
        this.name = name;
        this.enabled = false;
        this.inUse = false;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
        if (worldName == null && pos1 != null) {
            this.worldName = pos1.getWorld().getName();
        }
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getSpawn1() {
        return spawn1;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }

    public Location getSpawn2() {
        return spawn2;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean isValid() {
        return worldName != null
                && pos1 != null
                && pos2 != null
                && spawn1 != null
                && spawn2 != null;
    }
}
