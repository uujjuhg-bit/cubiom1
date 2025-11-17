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

        String displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
            item.getItemMeta().getDisplayName() : "";

        if (displayName.contains("Quick Play Duels") || item.getType() == Material.IRON_SWORD) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Select a kit to duel " + target.getName());
            player.chat("/duel " + target.getName());
        }
    }
}
