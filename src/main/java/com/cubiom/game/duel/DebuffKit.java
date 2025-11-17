package com.cubiom.game.duel;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class DebuffKit extends Kit {

    public DebuffKit() {
        super(
            "Debuff",
            "&c&lDebuff",
            Arrays.asList(
                "&7Diamond armor + Prot 1",
                "&7Sharp 2 Diamond Sword",
                "&727 Health + 9 Debuff Potions",
                "&7Speed 2"
            ),
            Material.POTION
        );
    }

    @Override
    public void applyKit(Player player) {
        clearPlayer(player);

        giveEnchantedDiamondArmor(player, 1);

        giveSword(player, Material.DIAMOND_SWORD, 2);

        for (int i = 0; i < 27; i++) {
            givePotion(player, (short) 16421, 1);
        }

        for (int i = 0; i < 3; i++) {
            givePotion(player, (short) 16388, 1);
        }
        for (int i = 0; i < 3; i++) {
            givePotion(player, (short) 16392, 1);
        }
        for (int i = 0; i < 3; i++) {
            givePotion(player, (short) 16394, 1);
        }

        addPotionEffect(player, PotionEffectType.SPEED, Integer.MAX_VALUE, 1);
    }
}
