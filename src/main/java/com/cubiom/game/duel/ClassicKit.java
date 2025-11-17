package com.cubiom.game.duel;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ClassicKit extends Kit {

    public ClassicKit() {
        super(
            "Classic",
            "&b&lClassic",
            Arrays.asList(
                "&7Diamond armor unenchanted",
                "&7Sharp 1 Diamond Sword",
                "&7Bow + 32 Arrows",
                "&71 Golden Apple"
            ),
            Material.DIAMOND_SWORD
        );
    }

    @Override
    public void applyKit(Player player) {
        clearPlayer(player);

        giveArmor(player, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
                  Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);

        giveSword(player, Material.DIAMOND_SWORD, 1);

        ItemStack bow = createItem(Material.BOW);
        addEnchant(bow, Enchantment.ARROW_INFINITE, 1);
        player.getInventory().addItem(bow);

        player.getInventory().addItem(createItem(Material.ARROW, 1));
        player.getInventory().addItem(createItem(Material.GOLDEN_APPLE, 1));
        player.getInventory().addItem(createItem(Material.FISHING_ROD));
    }
}
