package com.cubiom.arena;

import com.cubiom.core.GameType;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;

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
    }

    @Override
    public void loadFromConfig() {
    }

    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("worldName", worldName);
        json.addProperty("enabled", enabled);
        json.addProperty("inUse", inUse);

        if (corner1 != null) json.add("corner1", locationToJSON(corner1));
        if (corner2 != null) json.add("corner2", locationToJSON(corner2));
        if (spawn1 != null) json.add("spawn1", locationToJSON(spawn1));
        if (spawn2 != null) json.add("spawn2", locationToJSON(spawn2));
        if (spectatorSpawn != null) json.add("spectatorSpawn", locationToJSON(spectatorSpawn));

        return json;
    }

    public static DuelArena fromJSON(String name, JsonObject json) {
        try {
            DuelArena arena = new DuelArena(name);
            arena.worldName = json.get("worldName").getAsString();
            arena.enabled = json.get("enabled").getAsBoolean();
            arena.inUse = json.has("inUse") ? json.get("inUse").getAsBoolean() : false;

            if (json.has("corner1")) arena.corner1 = locationFromJSON(json.getAsJsonObject("corner1"));
            if (json.has("corner2")) arena.corner2 = locationFromJSON(json.getAsJsonObject("corner2"));
            if (json.has("spawn1")) arena.spawn1 = locationFromJSON(json.getAsJsonObject("spawn1"));
            if (json.has("spawn2")) arena.spawn2 = locationFromJSON(json.getAsJsonObject("spawn2"));
            if (json.has("spectatorSpawn")) arena.spectatorSpawn = locationFromJSON(json.getAsJsonObject("spectatorSpawn"));

            return arena;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonObject locationToJSON(Location loc) {
        JsonObject json = new JsonObject();
        json.addProperty("world", loc.getWorld().getName());
        json.addProperty("x", loc.getX());
        json.addProperty("y", loc.getY());
        json.addProperty("z", loc.getZ());
        json.addProperty("yaw", loc.getYaw());
        json.addProperty("pitch", loc.getPitch());
        return json;
    }

    private static Location locationFromJSON(JsonObject json) {
        return new Location(
            Bukkit.getWorld(json.get("world").getAsString()),
            json.get("x").getAsDouble(),
            json.get("y").getAsDouble(),
            json.get("z").getAsDouble(),
            json.get("yaw").getAsFloat(),
            json.get("pitch").getAsFloat()
        );
    }
}
