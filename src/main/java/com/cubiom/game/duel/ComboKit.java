package com.cubiom.game.duel;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class ComboKit extends Kit {

    public ComboKit() {
        super(
            "Combo",
            "&e&lCombo",
            Arrays.asList(
                "&7No armor - pure PvP",
                "&7Knockback 1 Stick",
                "&7Speed 2",
                "&7Practice your combos!"
            ),
            Material.STICK
        );
    }

    @Override
    public void applyKit(Player player) {
        clearPlayer(player);

        ItemStack stick = createItem(Material.STICK);
        addEnchant(stick, Enchantment.KNOCKBACK, 1);
        player.getInventory().addItem(stick);

        addPotionEffect(player, PotionEffectType.SPEED, Integer.MAX_VALUE, 1);
    }
}
