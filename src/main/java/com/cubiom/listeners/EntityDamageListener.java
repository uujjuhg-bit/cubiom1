package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.core.PlayerState;
import com.cubiom.game.sg.SGGame;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final Cubiom plugin;

    public EntityDamageListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);

        if (cp == null) return;

        if (cp.getState() == PlayerState.LOBBY) {
            event.setCancelled(true);
            return;
        }

        if (cp.getState() == PlayerState.SPECTATING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        SGGame sgGame = plugin.getSGManager().getPlayerGame(victim);
        if (sgGame != null && sgGame.isGracePeriod()) {
            event.setCancelled(true);
            damager.sendMessage("§cYou cannot damage players during grace period!");
        }
    }
}
