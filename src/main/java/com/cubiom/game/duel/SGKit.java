package com.cubiom.game.duel;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class SGKit extends Kit {

    public SGKit() {
        super(
            "SG",
            "&e&lSurvival Games",
            Arrays.asList(
                "&7Iron armor",
                "&7Sharp 1 Iron Sword",
                "&7Bow + Arrows",
                "&7Fishing Rod"
            ),
            Material.IRON_SWORD
        );
    }

    @Override
    public void applyKit(Player player) {
        clearPlayer(player);

        giveArmor(player, Material.IRON_HELMET, Material.IRON_CHESTPLATE,
                  Material.IRON_LEGGINGS, Material.IRON_BOOTS);

        giveSword(player, Material.IRON_SWORD, 1);

        ItemStack bow = createItem(Material.BOW);
        addEnchant(bow, Enchantment.ARROW_INFINITE, 1);
        player.getInventory().addItem(bow);

        player.getInventory().addItem(createItem(Material.ARROW, 1));
        player.getInventory().addItem(createItem(Material.FISHING_ROD));
    }
}
