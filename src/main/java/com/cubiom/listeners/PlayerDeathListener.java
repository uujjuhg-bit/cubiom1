package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.gamemodes.duels.DuelGame;
import com.cubiom.gamemodes.sg.SGGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final Cubiom plugin;

    public PlayerDeathListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        if (plugin.getSGManager().isInGame(player)) {
            SGGame game = plugin.getSGManager().getPlayerGame(player);
            if (game != null) {
                game.handlePlayerDeath(player, killer);
                event.setDeathMessage(null);
            }
        }

        if (plugin.getDuelManager().isInDuel(player)) {
            DuelGame duel = plugin.getDuelManager().getPlayerDuel(player);
            if (duel != null) {
                duel.handlePlayerDeath(player);
                plugin.getDuelManager().endDuel(duel);
                event.setDeathMessage(null);
            }
        }
    }
}
