package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.gamemodes.sg.GameState;
import com.cubiom.gamemodes.sg.SGGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final Cubiom plugin;

    public PlayerMoveListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getSGManager().isInGame(player)) {
            SGGame game = plugin.getSGManager().getPlayerGame(player);
            if (game != null && game.getState() == GameState.COUNTDOWN) {
                if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                    event.setTo(event.getFrom());
                }
            }
        }
    }
}
