package com.cubiom.listeners;

import com.cubiom.Cubiom;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractEntityListener implements Listener {

    private final Cubiom plugin;

    public PlayerInteractEntityListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null) {
            return;
        }

        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player target = (Player) event.getRightClicked();
        Material type = item.getType();

        if (type == Material.IRON_SWORD) {
            event.setCancelled(true);
            plugin.getGUIManager().openDuelInviteMenu(player, target);
        } else if (type == Material.COMPASS) {
            event.setCancelled(true);
            plugin.getSGManager().sendPartyInvite(player, target);
        }
    }
}
