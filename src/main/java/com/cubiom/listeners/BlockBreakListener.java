package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.gamemodes.sg.GameState;
import com.cubiom.gamemodes.sg.SGGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final Cubiom plugin;

    public BlockBreakListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (plugin.getSGManager().isInGame(player)) {
            SGGame game = plugin.getSGManager().getPlayerGame(player);

            if (game != null) {
                GameState state = game.getState();

                if (state == GameState.WAITING || state == GameState.COUNTDOWN || state == GameState.GRACE_PERIOD) {
                    event.setCancelled(true);
                    return;
                }

                Material type = event.getBlock().getType();
                if (!isAllowedBlock(type)) {
                    event.setCancelled(true);
                } else {
                    game.trackBlockBreak(event.getBlock());
                }
            }
        }

        if (plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isAllowedBlock(Material material) {
        return material == Material.LONG_GRASS ||
               material == Material.YELLOW_FLOWER ||
               material == Material.RED_ROSE ||
               material == Material.BROWN_MUSHROOM ||
               material == Material.RED_MUSHROOM ||
               material == Material.LEAVES ||
               material == Material.LEAVES_2;
    }
}
