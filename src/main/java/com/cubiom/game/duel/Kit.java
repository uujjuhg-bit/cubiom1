package com.cubiom.game.duel;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public abstract class Kit {

    private final String name;
    private final String displayName;
    private final List<String> description;
    private final Material icon;

    public Kit(String name, String displayName, List<String> description, Material icon) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public abstract void applyKit(Player player);

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    protected void clearPlayer(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
    }

    protected ItemStack createItem(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    protected ItemStack createItem(Material material) {
        return createItem(material, 1);
    }

    protected ItemStack createItem(Material material, int amount, short durability) {
        return new ItemStack(material, amount, durability);
    }

    protected void addEnchant(ItemStack item, Enchantment enchant, int level) {
        item.addUnsafeEnchantment(enchant, level);
    }

    protected void giveArmor(Player player, Material helmet, Material chestplate, Material leggings, Material boots) {
        PlayerInventory inv = player.getInventory();
        if (helmet != null) inv.setHelmet(createItem(helmet));
        if (chestplate != null) inv.setChestplate(createItem(chestplate));
        if (leggings != null) inv.setLeggings(createItem(leggings));
        if (boots != null) inv.setBoots(createItem(boots));
    }

    protected void giveEnchantedDiamondArmor(Player player, int protection) {
        ItemStack helmet = createItem(Material.DIAMOND_HELMET);
        ItemStack chestplate = createItem(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = createItem(Material.DIAMOND_LEGGINGS);
        ItemStack boots = createItem(Material.DIAMOND_BOOTS);

        addEnchant(helmet, Enchantment.PROTECTION_ENVIRONMENTAL, protection);
        addEnchant(chestplate, Enchantment.PROTECTION_ENVIRONMENTAL, protection);
        addEnchant(leggings, Enchantment.PROTECTION_ENVIRONMENTAL, protection);
        addEnchant(boots, Enchantment.PROTECTION_ENVIRONMENTAL, protection);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    protected void giveSword(Player player, Material type, int sharpness) {
        ItemStack sword = createItem(type);
        if (sharpness > 0) {
            addEnchant(sword, Enchantment.DAMAGE_ALL, sharpness);
        }
        player.getInventory().addItem(sword);
    }

    protected void givePotion(Player player, short data, int amount) {
        ItemStack potion = createItem(Material.POTION, amount, data);
        player.getInventory().addItem(potion);
    }

    protected void addPotionEffect(Player player, PotionEffectType type, int duration, int amplifier) {
        player.addPotionEffect(new PotionEffect(type, duration, amplifier));
    }

    public static Kit getKit(String name) {
        switch (name.toLowerCase()) {
            case "nodebuff":
                return new NoDebuffKit();
            case "debuff":
                return new DebuffKit();
            case "classic":
                return new ClassicKit();
            case "builduhc":
                return new BuildUHCKit();
            case "sg":
                return new SGKit();
            case "combo":
                return new ComboKit();
            default:
                return new NoDebuffKit();
        }
    }

    public static List<Kit> getAllKits() {
        List<Kit> kits = new ArrayList<>();
        kits.add(new NoDebuffKit());
        kits.add(new DebuffKit());
        kits.add(new ClassicKit());
        kits.add(new BuildUHCKit());
        kits.add(new SGKit());
        kits.add(new ComboKit());
        return kits;
    }
}
