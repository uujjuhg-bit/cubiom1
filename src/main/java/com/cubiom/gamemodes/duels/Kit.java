package com.cubiom.gamemodes.duels;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class Kit {

    private String name;
    private String displayName;
    private String description;
    private List<ItemStack> items;
    private List<PotionEffect> effects;
    private List<Material> allowedBlocks;

    public Kit(String name) {
        this.name = name;
        this.displayName = name;
        this.description = "";
        this.items = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.allowedBlocks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void addItem(ItemStack item) {
        this.items.add(item);
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }

    public void addEffect(PotionEffect effect) {
        this.effects.add(effect);
    }

    public List<Material> getAllowedBlocks() {
        return allowedBlocks;
    }

    public void addAllowedBlock(Material material) {
        this.allowedBlocks.add(material);
    }

    public boolean isBlockAllowed(Material material) {
        return allowedBlocks.isEmpty() || allowedBlocks.contains(material);
    }

    public void applyKit(org.bukkit.entity.Player player) {
        player.getInventory().clear();

        for (ItemStack item : items) {
            player.getInventory().addItem(item.clone());
        }

        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }
    }

    public static Kit createClassicKit() {
        Kit kit = new Kit("Classic");
        kit.setDisplayName("&bClassic");
        kit.setDescription("Standard PvP kit");

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        kit.addItem(sword);

        ItemStack bow = new ItemStack(Material.BOW);
        kit.addItem(bow);

        kit.addItem(new ItemStack(Material.ARROW, 16));

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        kit.addItem(helmet);

        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        kit.addItem(chestplate);

        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        kit.addItem(leggings);

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        kit.addItem(boots);

        kit.addItem(new ItemStack(Material.COOKED_BEEF, 32));

        return kit;
    }

    public static Kit createNoDebuffKit() {
        Kit kit = new Kit("NoDebuff");
        kit.setDisplayName("&bNoDebuff");
        kit.setDescription("No debuff potions allowed");

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        kit.addItem(sword);

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        kit.addItem(helmet);

        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        kit.addItem(chestplate);

        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        kit.addItem(leggings);

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        kit.addItem(boots);

        kit.addItem(new ItemStack(Material.COOKED_BEEF, 64));

        for (int i = 0; i < 36; i++) {
            kit.addItem(new ItemStack(Material.POTION, 1, (short) 16421));
        }

        return kit;
    }
}
