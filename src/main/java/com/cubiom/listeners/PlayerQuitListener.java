package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.gamemodes.duels.DuelGame;
import com.cubiom.gamemodes.sg.SGGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final Cubiom plugin;

    public PlayerQuitListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getSGManager().isInGame(player)) {
            plugin.getSGManager().leaveGame(player);
        }

        if (plugin.getDuelManager().isInQueue(player)) {
            plugin.getDuelManager().leaveQueue(player);
        }

        if (plugin.getDuelManager().isInDuel(player)) {
            DuelGame duel = plugin.getDuelManager().getPlayerDuel(player);
            if (duel != null) {
                Player opponent = plugin.getServer().getPlayer(
                    duel.getPlayer1().equals(player.getUniqueId()) ? duel.getPlayer2() : duel.getPlayer1()
                );
                plugin.getDuelManager().endDuel(duel, opponent);
            }
        }
    }
}
