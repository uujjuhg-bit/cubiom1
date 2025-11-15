package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.gamemodes.sg.GameState;
import com.cubiom.gamemodes.sg.SGGame;
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
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (plugin.getSGManager().isInGame(player)) {
            SGGame game = plugin.getSGManager().getPlayerGame(player);

            if (game != null) {
                GameState state = game.getState();

                if (state == GameState.LOBBY || state == GameState.COUNTDOWN || state == GameState.GRACE_PERIOD) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (plugin.getSGManager().isInGame(victim) && plugin.getSGManager().isInGame(damager)) {
            SGGame game = plugin.getSGManager().getPlayerGame(victim);

            if (game != null) {
                GameState state = game.getState();

                if (state == GameState.LOBBY || state == GameState.COUNTDOWN || state == GameState.GRACE_PERIOD) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
