package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.core.PlayerState;
import com.cubiom.player.CubiomPlayer;
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
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);

        if (cp == null) return;

        if (cp.getState() == PlayerState.LOBBY) {
            if (!player.hasPermission("cubiom.admin")) {
                event.setCancelled(true);
            }
        } else if (cp.getState() == PlayerState.SPECTATING) {
            event.setCancelled(true);
        }
    }
}
