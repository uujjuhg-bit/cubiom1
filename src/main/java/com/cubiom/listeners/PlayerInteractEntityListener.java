package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.core.PlayerState;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
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
        Entity entity = event.getRightClicked();

        if (!(entity instanceof Player)) return;

        Player target = (Player) entity;
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        CubiomPlayer targetCp = plugin.getPlayerManager().getPlayer(target);

        if (cp == null || targetCp == null) return;

        if (!cp.isInLobby() || !targetCp.isInLobby()) return;

        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;

        // Check if it's the iron sword (duel item)
        if (item.getType() == Material.IRON_SWORD) {
            event.setCancelled(true);

            String message = plugin.getLanguageManager().getMessage(player, "duels.invite.sent")
                .replace("{0}", target.getName());
            player.sendMessage(message);

            plugin.getGUIManager().openKitSelector(player, target.getName());
        }
    }
}
