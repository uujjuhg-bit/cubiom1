package com.cubiom.game.duel;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class NoDebuffKit extends Kit {

    public NoDebuffKit() {
        super(
            "NoDebuff",
            "&d&lNoDebuff",
            Arrays.asList(
                "&7Diamond armor + Prot 1",
                "&7Sharp 2 Diamond Sword",
                "&736 Health Potions",
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

        for (int i = 0; i < 36; i++) {
            givePotion(player, (short) 16421, 1);
        }

        addPotionEffect(player, PotionEffectType.SPEED, Integer.MAX_VALUE, 1);
    }
}
