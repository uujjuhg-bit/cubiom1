package com.cubiom.utils;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TabListManager {

    private final Cubiom plugin;

    public TabListManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public void updateTabList(Player player) {
        LanguageManager lang = plugin.getLanguageManager();

        Map<String, String> replacements = new HashMap<>();
        replacements.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        replacements.put("ping", String.valueOf(getPing(player)));

        String header = lang.formatMessage(player, "tablist.header", replacements);
        String footer = lang.formatMessage(player, "tablist.footer", replacements);

        setHeaderFooter(player, header, footer);
    }

    public void updateAllTabLists() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player);
        }
    }

    private void setHeaderFooter(Player player, String header, String footer) {
        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = craftPlayer.getClass().getField("playerConnection").get(craftPlayer);

            Class<?> chatClass = getNMSClass("IChatBaseComponent");
            Class<?> chatSerializerClass = chatClass.getDeclaredClasses()[0];
            Method serializeMethod = chatSerializerClass.getMethod("a", String.class);

            Object headerComponent = serializeMethod.invoke(null, "{\"text\":\"" + header.replace("&", "§") + "\"}");
            Object footerComponent = serializeMethod.invoke(null, "{\"text\":\"" + footer.replace("&", "§") + "\"}");

            Class<?> packetClass = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
            Constructor<?> constructor = packetClass.getConstructor(chatClass);
            Object packet = constructor.newInstance(headerComponent);

            Field footerField = packet.getClass().getDeclaredField("b");
            footerField.setAccessible(true);
            footerField.set(packet, footerComponent);

            Method sendPacket = connection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
            sendPacket.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getPing(Player player) {
        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Field pingField = craftPlayer.getClass().getDeclaredField("ping");
            return pingField.getInt(craftPlayer);
        } catch (Exception e) {
            return 0;
        }
    }

    private Class<?> getNMSClass(String name) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
