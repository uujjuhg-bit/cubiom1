package com.cubiom.ui;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import com.cubiom.game.duel.Kit;
import com.cubiom.game.sg.SGGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final Cubiom plugin;

    public GUIManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public void openGameSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Game Selector");

        ItemStack sgItem = createItem(Material.DIAMOND_SWORD, ChatColor.GREEN + "Survival Games",
            ChatColor.GRAY + "Battle 24 players",
            ChatColor.GRAY + "Last one standing wins!",
            ChatColor.YELLOW + "Click to view arenas");
        inv.setItem(11, sgItem);

        ItemStack duelItem = createItem(Material.IRON_SWORD, ChatColor.LIGHT_PURPLE + "Duels",
            ChatColor.GRAY + "1v1 PvP battles",
            ChatColor.GRAY + "Test your skills!",
            ChatColor.YELLOW + "Click to select kit");
        inv.setItem(15, duelItem);

        player.openInventory(inv);
    }

    public void openSGArenaSelector(Player player) {
        List<SGArena> arenas = plugin.getArenaManager().getEnabledSGArenas();

        int size = Math.min(54, ((arenas.size() + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.GREEN + "Select SG Arena");

        int slot = 0;
        for (SGArena arena : arenas) {
            SGGame game = plugin.getSGManager().getActiveGames().get(arena.getName());
            int players = game != null ? game.getPlayers().size() : 0;
            String status = game != null ? "In Progress" : "Waiting";

            ItemStack item = createItem(Material.MAP, ChatColor.AQUA + arena.getName(),
                ChatColor.GRAY + "Players: " + players + "/" + arena.getMaxPlayers(),
                ChatColor.GRAY + "Status: " + status,
                ChatColor.YELLOW + "Click to join!");
            inv.setItem(slot++, item);
        }

        ItemStack quickJoin = createItem(Material.EMERALD, ChatColor.GREEN + "Quick Join",
            ChatColor.GRAY + "Join any available game");
        inv.setItem(size - 1, quickJoin);

        player.openInventory(inv);
    }

    public void openKitSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Select Duel Kit");

        List<Kit> kits = Kit.getAllKits();
        int slot = 10;

        for (Kit kit : kits) {
            ItemStack item = new ItemStack(kit.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));

            List<String> lore = new ArrayList<>();
            for (String desc : kit.getDescription()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', desc));
            }
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to join queue!");
            meta.setLore(lore);

            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }

    public void openPlayerProfile(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.YELLOW + player.getName() + "'s Profile");

        ItemStack sgStats = createItem(Material.DIAMOND_SWORD, ChatColor.GREEN + "SG Stats",
            ChatColor.GRAY + "Wins: 0",
            ChatColor.GRAY + "Kills: 0",
            ChatColor.GRAY + "Deaths: 0");
        inv.setItem(11, sgStats);

        ItemStack duelStats = createItem(Material.IRON_SWORD, ChatColor.LIGHT_PURPLE + "Duel Stats",
            ChatColor.GRAY + "Overall ELO: 1000",
            ChatColor.GRAY + "Wins: 0",
            ChatColor.GRAY + "Losses: 0");
        inv.setItem(15, duelStats);

        player.openInventory(inv);
    }

    public void openActivePlayers(Player player) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        int size = Math.min(54, ((online.size() + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.GREEN + "Active Players");

        int slot = 0;
        for (Player p : online) {
            if (p.equals(player)) continue;

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
            meta.setOwner(p.getName());
            meta.setDisplayName(ChatColor.YELLOW + p.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click to duel");
            lore.add(ChatColor.GRAY + "Left-click to view profile");
            meta.setLore(lore);
            head.setItemMeta(meta);

            inv.setItem(slot++, head);
        }

        player.openInventory(inv);
    }

    public void openLeaderboards(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Leaderboards");

        ItemStack sgWins = createItem(Material.GOLD_INGOT, ChatColor.GREEN + "SG Wins",
            ChatColor.GRAY + "Top 10 players by wins");
        inv.setItem(10, sgWins);

        ItemStack sgKills = createItem(Material.DIAMOND_SWORD, ChatColor.RED + "SG Kills",
            ChatColor.GRAY + "Top 10 players by kills");
        inv.setItem(12, sgKills);

        ItemStack duelElo = createItem(Material.IRON_SWORD, ChatColor.LIGHT_PURPLE + "Duel ELO",
            ChatColor.GRAY + "Top 10 players by ELO");
        inv.setItem(14, duelElo);

        ItemStack kits = createItem(Material.CHEST, ChatColor.AQUA + "Kit Leaderboards",
            ChatColor.GRAY + "View leaderboards per kit");
        inv.setItem(16, kits);

        player.openInventory(inv);
    }

    public void openSettings(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Settings");

        ItemStack language = createItem(Material.PAPER, ChatColor.YELLOW + "Language",
            ChatColor.GRAY + "Current: English",
            ChatColor.GRAY + "Click to change");
        inv.setItem(13, language);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(java.util.Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}
