package com.cubiom.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class LocationSerializer {

    public static Map<String, Object> serialize(Location location) {
        if (location == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("world", location.getWorld().getName());
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", location.getYaw());
        map.put("pitch", location.getPitch());

        return map;
    }

    public static Location deserialize(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        String worldName = (String) map.get("world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return null;
        }

        double x = getDouble(map, "x");
        double y = getDouble(map, "y");
        double z = getDouble(map, "z");
        float yaw = getFloat(map, "yaw");
        float pitch = getFloat(map, "pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    private static double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private static float getFloat(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0.0f;
    }

    public static String toString(Location location) {
        if (location == null) {
            return null;
        }

        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    public static Location fromString(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        String[] parts = str.split(",");
        if (parts.length != 6) {
            return null;
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
