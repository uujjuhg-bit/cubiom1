package com.cubiom.gamemodes.duels;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Kit {

    private String name;
    private String displayName;
    private Material icon;
    private List<String> description;
    private List<ItemStack> items;
    private ItemStack[] armor;
    private List<PotionEffect> effects;
    private List<Material> allowedBlocks;
    private boolean buildingAllowed;

    public Kit(String name, Material icon) {
        this.name = name;
        this.displayName = name;
        this.icon = icon;
        this.description = new ArrayList<>();
        this.items = new ArrayList<>();
        this.armor = new ItemStack[4];
        this.effects = new ArrayList<>();
        this.allowedBlocks = new ArrayList<>();
        this.buildingAllowed = false;
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

    public Material getIcon() {
        return icon;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public void addDescription(String line) {
        this.description.add(line);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void addItem(ItemStack item) {
        this.items.add(item);
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public void setHelmet(ItemStack helmet) {
        this.armor[3] = helmet;
    }

    public void setChestplate(ItemStack chestplate) {
        this.armor[2] = chestplate;
    }

    public void setLeggings(ItemStack leggings) {
        this.armor[1] = leggings;
    }

    public void setBoots(ItemStack boots) {
        this.armor[0] = boots;
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

    public boolean isBuildingAllowed() {
        return buildingAllowed;
    }

    public void setBuildingAllowed(boolean buildingAllowed) {
        this.buildingAllowed = buildingAllowed;
    }

    public boolean isBlockAllowed(Material material) {
        if (!buildingAllowed) return false;
        return allowedBlocks.isEmpty() || allowedBlocks.contains(material);
    }

    public void applyKit(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        for (ItemStack item : items) {
            if (item != null) {
                player.getInventory().addItem(item.clone());
            }
        }

        player.getInventory().setArmorContents(armor);

        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }

        player.updateInventory();
    }

    public static Kit createNoDebuffKit() {
        Kit kit = new Kit("NoDebuff", Material.POTION);
        kit.setDisplayName("&d&lNoDebuff");
        kit.addDescription("&7Diamond armor with Prot 1");
        kit.addDescription("&7Sharp 2 Diamond Sword");
        kit.addDescription("&736x Health Potions");
        kit.addDescription("&7Speed 2");

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        kit.addItem(sword);

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        helmet.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        chestplate.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        leggings.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        boots.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setBoots(boots);

        kit.addItem(new ItemStack(Material.COOKED_BEEF, 64));

        for (int i = 0; i < 36; i++) {
            kit.addItem(new ItemStack(Material.POTION, 1, (short) 16421));
        }

        kit.addEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

        return kit;
    }

    public static Kit createDebuffKit() {
        Kit kit = new Kit("Debuff", Material.POTION);
        kit.setDisplayName("&c&lDebuff");
        kit.addDescription("&7Diamond armor with Prot 1");
        kit.addDescription("&7Sharp 2 Diamond Sword");
        kit.addDescription("&7Debuff & Health Potions");
        kit.addDescription("&7Speed 2");

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        kit.addItem(sword);

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        helmet.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        chestplate.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        leggings.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        boots.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setBoots(boots);

        kit.addItem(new ItemStack(Material.COOKED_BEEF, 64));

        for (int i = 0; i < 27; i++) {
            kit.addItem(new ItemStack(Material.POTION, 1, (short) 16421));
        }

        for (int i = 0; i < 3; i++) {
            kit.addItem(new ItemStack(Material.POTION, 1, (short) 16392));
        }

        for (int i = 0; i < 3; i++) {
            kit.addItem(new ItemStack(Material.POTION, 1, (short) 16424));
        }

        for (int i = 0; i < 3; i++) {
            kit.addItem(new ItemStack(Material.POTION, 1, (short) 16428));
        }

        kit.addEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

        return kit;
    }

    public static Kit createBuildUHCKit() {
        Kit kit = new Kit("BuildUHC", Material.WOOD);
        kit.setDisplayName("&6&lBuildUHC");
        kit.addDescription("&7Diamond armor with Prot 1");
        kit.addDescription("&7Sharp 1 Diamond Sword");
        kit.addDescription("&7Building & Crafting");
        kit.addDescription("&764 Cobblestone, Crafting Table");

        kit.setBuildingAllowed(true);
        kit.addAllowedBlock(Material.COBBLESTONE);
        kit.addAllowedBlock(Material.WOOD);
        kit.addAllowedBlock(Material.WORKBENCH);

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        kit.addItem(sword);

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        helmet.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        chestplate.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        leggings.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        boots.addEnchantment(Enchantment.DURABILITY, 3);
        kit.setBoots(boots);

        kit.addItem(new ItemStack(Material.BOW));
        kit.addItem(new ItemStack(Material.ARROW, 16));
        kit.addItem(new ItemStack(Material.GOLDEN_APPLE, 8));
        kit.addItem(new ItemStack(Material.FISHING_ROD));
        kit.addItem(new ItemStack(Material.COBBLESTONE, 64));
        kit.addItem(new ItemStack(Material.WOOD, 32));
        kit.addItem(new ItemStack(Material.WORKBENCH));
        kit.addItem(new ItemStack(Material.WATER_BUCKET));
        kit.addItem(new ItemStack(Material.LAVA_BUCKET));

        return kit;
    }

    public static Kit createClassicKit() {
        Kit kit = new Kit("Classic", Material.DIAMOND_SWORD);
        kit.setDisplayName("&b&lClassic");
        kit.addDescription("&7Diamond armor unenchanted");
        kit.addDescription("&7Sharp 1 Diamond Sword");
        kit.addDescription("&7Bow & Arrows");
        kit.addDescription("&7No potion effects");

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        kit.addItem(sword);

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        kit.setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        kit.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        kit.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        kit.setBoots(boots);

        kit.addItem(new ItemStack(Material.BOW));
        kit.addItem(new ItemStack(Material.ARROW, 32));
        kit.addItem(new ItemStack(Material.COOKED_BEEF, 64));

        return kit;
    }

    public static Kit createComboKit() {
        Kit kit = new Kit("Combo", Material.STICK);
        kit.setDisplayName("&e&lCombo");
        kit.addDescription("&7No armor - pure PvP");
        kit.addDescription("&7Knockback 1 Stick");
        kit.addDescription("&7Speed 2, Strength 1");
        kit.addDescription("&7Practice your combos!");

        ItemStack stick = new ItemStack(Material.STICK);
        stick.addEnchantment(Enchantment.KNOCKBACK, 1);
        kit.addItem(stick);

        kit.addItem(new ItemStack(Material.COOKED_BEEF, 64));

        kit.addEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        kit.addEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));

        return kit;
    }
}
