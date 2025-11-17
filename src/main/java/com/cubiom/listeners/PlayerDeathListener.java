package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.game.duel.DuelGame;
import com.cubiom.game.sg.SGGame;
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

        SGGame sgGame = plugin.getSGManager().getPlayerGame(player);
        if (sgGame != null) {
            event.setDeathMessage("");
            event.getDrops().clear();
            event.setDroppedExp(0);
            sgGame.handleDeath(player, killer);
            return;
        }

        DuelGame duelGame = plugin.getDuelManager().getPlayerGame(player);
        if (duelGame != null) {
            event.setDeathMessage("");
            event.getDrops().clear();
            event.setDroppedExp(0);
            duelGame.handleDeath(player);
        }
    }
}
