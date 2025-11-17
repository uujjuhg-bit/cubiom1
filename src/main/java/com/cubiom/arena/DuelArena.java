package com.cubiom.arena;

import com.cubiom.Cubiom;
import com.cubiom.core.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DuelArena extends Arena {

    private Location corner1;
    private Location corner2;
    private Location spawn1;
    private Location spawn2;
    private Location spectatorSpawn;
    private boolean inUse;

    public DuelArena(String name) {
        super(name, GameType.DUEL);
        this.inUse = false;
    }

    public Location getCorner1() {
        return corner1;
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
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

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    @Override
    public boolean isValid() {
        return corner1 != null && corner2 != null && spawn1 != null && spawn2 != null && spectatorSpawn != null;
    }

    @Override
    public void saveToConfig() {
        File file = new File(Cubiom.getInstance().getDataFolder(), "arenas/duels/" + name + ".yml");
        file.getParentFile().mkdirs();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("name", name);
        config.set("enabled", enabled);
        config.set("world", worldName);

        if (corner1 != null) config.set("corner1", serializeLocation(corner1));
        if (corner2 != null) config.set("corner2", serializeLocation(corner2));
        if (spawn1 != null) config.set("spawn1", serializeLocation(spawn1));
        if (spawn2 != null) config.set("spawn2", serializeLocation(spawn2));
        if (spectatorSpawn != null) config.set("spectator-spawn", serializeLocation(spectatorSpawn));

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromConfig() {
        File file = new File(Cubiom.getInstance().getDataFolder(), "arenas/duels/" + name + ".yml");
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        enabled = config.getBoolean("enabled", false);
        worldName = config.getString("world");

        if (config.contains("corner1")) corner1 = deserializeLocation(config.getString("corner1"));
        if (config.contains("corner2")) corner2 = deserializeLocation(config.getString("corner2"));
        if (config.contains("spawn1")) spawn1 = deserializeLocation(config.getString("spawn1"));
        if (config.contains("spawn2")) spawn2 = deserializeLocation(config.getString("spawn2"));
        if (config.contains("spectator-spawn")) spectatorSpawn = deserializeLocation(config.getString("spectator-spawn"));
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location deserializeLocation(String s) {
        String[] parts = s.split(",");
        return new Location(
            Bukkit.getWorld(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]),
            Float.parseFloat(parts[4]),
            Float.parseFloat(parts[5])
        );
    }
}
