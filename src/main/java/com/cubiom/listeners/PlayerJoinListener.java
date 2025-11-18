package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.player.CubiomPlayer;
import com.cubiom.ui.LobbyHotbar;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Cubiom plugin;

    public PlayerJoinListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        CubiomPlayer cubiomPlayer = plugin.getPlayerManager().addPlayer(player);

        plugin.getSupabaseManager().loadPlayerData(player.getUniqueId().toString()).thenAccept(jsonObject -> {
            if (jsonObject != null && jsonObject.has("language")) {
                String loadedLang = jsonObject.get("language").getAsString();
                cubiomPlayer.setLanguage(loadedLang);
            }

            plugin.getSupabaseManager().upsertPlayer(
                player.getUniqueId().toString(),
                player.getName(),
                cubiomPlayer.getLanguage()
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getScoreboardManager().updateScoreboard(player);
            });
        });

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();

        Location lobby = plugin.getConfig().contains("lobby.spawn") ?
            deserializeLocation(plugin.getConfig().getString("lobby.spawn")) :
            Bukkit.getWorlds().get(0).getSpawnLocation();
        player.teleport(lobby);

        LobbyHotbar.giveLobbyItems(player);

        plugin.getScoreboardManager().createScoreboard(player);

        event.setJoinMessage(plugin.getLanguageManager().getMessage(cubiomPlayer.getLanguage(), "general.join-message")
            .replace("{player}", player.getName()));
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
