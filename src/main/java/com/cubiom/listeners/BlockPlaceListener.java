package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.gamemodes.sg.GameState;
import com.cubiom.gamemodes.sg.SGGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final Cubiom plugin;

    public BlockPlaceListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (plugin.getSGManager().isInGame(player)) {
            SGGame game = plugin.getSGManager().getPlayerGame(player);

            if (game != null) {
                GameState state = game.getState();

                if (state == GameState.WAITING || state == GameState.COUNTDOWN || state == GameState.GRACE_PERIOD) {
                    event.setCancelled(true);
                } else {
                    game.trackBlockPlace(event.getBlock().getLocation());
                }
            }
        }

        if (plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
        }
    }
}
