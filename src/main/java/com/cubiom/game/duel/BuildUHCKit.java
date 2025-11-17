package com.cubiom.game.duel;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class BuildUHCKit extends Kit {

    public BuildUHCKit() {
        super(
            "BuildUHC",
            "&6&lBuildUHC",
            Arrays.asList(
                "&7Diamond armor + Prot 1",
                "&7Building materials",
                "&7Bow + Arrows",
                "&7Golden Apples + Lava"
            ),
            Material.WOOD
        );
    }

    @Override
    public void applyKit(Player player) {
        clearPlayer(player);

        giveEnchantedDiamondArmor(player, 1);

        giveSword(player, Material.DIAMOND_SWORD, 1);

        ItemStack bow = createItem(Material.BOW);
        addEnchant(bow, Enchantment.ARROW_INFINITE, 1);
        player.getInventory().addItem(bow);

        player.getInventory().addItem(createItem(Material.ARROW, 1));
        player.getInventory().addItem(createItem(Material.GOLDEN_APPLE, 8));
        player.getInventory().addItem(createItem(Material.FISHING_ROD));
        player.getInventory().addItem(createItem(Material.WOOD, 64));
        player.getInventory().addItem(createItem(Material.WOOD, 64));
        player.getInventory().addItem(createItem(Material.COBBLESTONE, 64));
        player.getInventory().addItem(createItem(Material.LAVA_BUCKET));
        player.getInventory().addItem(createItem(Material.WATER_BUCKET));
    }
}
