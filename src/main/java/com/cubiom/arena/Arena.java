package com.cubiom.arena;

import com.cubiom.core.GameType;
import org.bukkit.Location;

import java.util.List;

public abstract class Arena {

    protected final String name;
    protected final GameType type;
    protected boolean enabled;
    protected String worldName;

    public Arena(String name, GameType type) {
        this.name = name;
        this.type = type;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public GameType getType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public abstract boolean isValid();

    public abstract void saveToConfig();

    public abstract void loadFromConfig();
}
