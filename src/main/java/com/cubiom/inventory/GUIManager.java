package com.cubiom.inventory;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import com.cubiom.gamemodes.duels.Kit;
import com.cubiom.gamemodes.sg.SGGame;
import com.cubiom.language.LanguageManager;
import com.cubiom.stats.PlayerStats;
import com.cubiom.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GUIManager {

    private final Cubiom plugin;
    private final Map<UUID, String> playerMenus = new HashMap<>();

    public GUIManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public void openSGMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        String title = lang.getMessage(player, "gui.sg-menu");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        playerMenus.put(player.getUniqueId(), "SG_MAIN");

        List<Arena> arenas = plugin.getSGManager().getArenas();

        if (arenas.isEmpty()) {
            ItemStack noArenas = new ItemBuilder(Material.BARRIER)
                    .setName("&c&lNo Arenas Available")
                    .setLore("&7No arenas have been", "&7configured yet!")
                    .build();
            inv.setItem(22, noArenas);
        } else {
            int slot = 10;
            for (Arena arena : arenas) {
                if (slot > 34) break;

                SGGame game = plugin.getSGManager().getGameByArena(arena);
                int players = game != null ? game.getPlayers().size() : 0;
                int maxPlayers = arena.getMaxPlayers();
                String state = game != null ? game.getState().toString() : "WAITING";

                Material iconMat = Material.GRASS;
                String statusColor = "&a";

                if (!arena.isEnabled()) {
                    iconMat = Material.BARRIER;
                    statusColor = "&c";
                } else if (players >= maxPlayers) {
                    iconMat = Material.REDSTONE_BLOCK;
                    statusColor = "&c";
                } else if (players > 0) {
                    iconMat = Material.GOLD_BLOCK;
                    statusColor = "&e";
                }

                ItemStack arenaItem = new ItemBuilder(iconMat)
                        .setName("&b&l" + arena.getName())
                        .setLore(
                                "&7Status: " + statusColor + state,
                                "&7Players: &b" + players + "&7/&b" + maxPlayers,
                                "",
                                arena.isEnabled() && players < maxPlayers ? "&e▶ Click to join!" : "&cNot available"
                        )
                        .build();

                inv.setItem(slot, arenaItem);
                slot++;
                if (slot == 17) slot = 19;
                if (slot == 26) slot = 28;
            }
        }

        ItemStack statsItem = new ItemBuilder(Material.PAPER)
                .setName("&a&lYour SG Stats")
                .setLore("&7View your Survival", "&7Games statistics")
                .build();

        ItemStack closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Return to lobby")
                .build();

        inv.setItem(48, statsItem);
        inv.setItem(49, closeItem);

        player.openInventory(inv);
    }

    public void openDuelsMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        String title = lang.getMessage(player, "gui.duels-menu");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        playerMenus.put(player.getUniqueId(), "DUELS_MAIN");

        Kit noDebuff = Kit.createNoDebuffKit();
        ItemStack noDebuffItem = new ItemBuilder(noDebuff.getIcon())
                .setName(noDebuff.getDisplayName())
                .setLore(noDebuff.getDescription())
                .addLore("")
                .addLore("&e▶ Click to select!")
                .build();

        Kit debuff = Kit.createDebuffKit();
        ItemStack debuffItem = new ItemBuilder(debuff.getIcon())
                .setName(debuff.getDisplayName())
                .setLore(debuff.getDescription())
                .addLore("")
                .addLore("&e▶ Click to select!")
                .build();

        Kit buildUHC = Kit.createBuildUHCKit();
        ItemStack buildUHCItem = new ItemBuilder(buildUHC.getIcon())
                .setName(buildUHC.getDisplayName())
                .setLore(buildUHC.getDescription())
                .addLore("")
                .addLore("&e▶ Click to select!")
                .build();

        Kit classic = Kit.createClassicKit();
        ItemStack classicItem = new ItemBuilder(classic.getIcon())
                .setName(classic.getDisplayName())
                .setLore(classic.getDescription())
                .addLore("")
                .addLore("&e▶ Click to select!")
                .build();

        Kit combo = Kit.createComboKit();
        ItemStack comboItem = new ItemBuilder(combo.getIcon())
                .setName(combo.getDisplayName())
                .setLore(combo.getDescription())
                .addLore("")
                .addLore("&e▶ Click to select!")
                .build();

        inv.setItem(11, noDebuffItem);
        inv.setItem(13, debuffItem);
        inv.setItem(15, buildUHCItem);
        inv.setItem(20, classicItem);
        inv.setItem(24, comboItem);

        ItemStack statsItem = new ItemBuilder(Material.PAPER)
                .setName("&a&lYour Duel Stats")
                .setLore("&7View your duel", "&7statistics and ELO")
                .build();

        ItemStack closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Return to lobby")
                .build();

        inv.setItem(48, statsItem);
        inv.setItem(49, closeItem);

        player.openInventory(inv);
    }

    public void openStatsMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        String title = lang.getMessage(player, "gui.stats-menu");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        playerMenus.put(player.getUniqueId(), "STATS");

        PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());

        ItemStack sgStats = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&b&lSurvival Games Stats")
                .setLore(
                        "&7▪ Wins: &b" + stats.getSgWins(),
                        "&7▪ Kills: &b" + stats.getSgKills(),
                        "&7▪ Deaths: &b" + stats.getSgDeaths(),
                        "&7▪ K/D Ratio: &b" + String.format("%.2f", stats.getSgKDR()),
                        "",
                        "&7Games Played: &b" + (stats.getSgWins() + stats.getSgDeaths())
                )
                .build();

        ItemStack duelStats = new ItemBuilder(Material.IRON_SWORD)
                .setName("&d&lDuels Stats")
                .setLore(
                        "&7▪ Wins: &b" + stats.getDuelWins(),
                        "&7▪ Losses: &b" + stats.getDuelLosses(),
                        "&7▪ ELO Rating: &b" + stats.getDuelElo(),
                        "",
                        "&7Win Rate: &b" + String.format("%.1f%%",
                            stats.getDuelWins() + stats.getDuelLosses() > 0 ?
                            (stats.getDuelWins() * 100.0 / (stats.getDuelWins() + stats.getDuelLosses())) : 0)
                )
                .build();

        ItemStack profileItem = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                .setName("&e&l" + player.getName())
                .setLore(
                        "&7Player Profile",
                        "",
                        "&7Total Kills: &b" + (stats.getSgKills() + stats.getDuelWins()),
                        "&7Total Deaths: &b" + (stats.getSgDeaths() + stats.getDuelLosses()),
                        "&7First Played: &7Coming soon",
                        "&7Last Played: &7Now"
                )
                .setSkullOwner(player.getName())
                .build();

        ItemStack closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Return to lobby")
                .build();

        inv.setItem(13, profileItem);
        inv.setItem(21, sgStats);
        inv.setItem(23, duelStats);
        inv.setItem(49, closeItem);

        player.openInventory(inv);
    }

    public void openTopMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        String title = lang.getMessage(player, "gui.leaderboard-menu");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        playerMenus.put(player.getUniqueId(), "LEADERBOARD");

        ItemStack sgWinsBoard = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&b&lTop SG Wins")
                .setLore("&7View the top 10", "&7SG players by wins", "", "&e▶ Click to view!")
                .build();

        ItemStack sgKillsBoard = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&b&lTop SG Kills")
                .setLore("&7View the top 10", "&7SG players by kills", "", "&e▶ Click to view!")
                .build();

        ItemStack duelEloBoard = new ItemBuilder(Material.IRON_SWORD)
                .setName("&d&lTop Duel ELO")
                .setLore("&7View the top 10", "&7duel players by ELO", "", "&e▶ Click to view!")
                .build();

        ItemStack closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Return to lobby")
                .build();

        inv.setItem(20, sgWinsBoard);
        inv.setItem(22, sgKillsBoard);
        inv.setItem(24, duelEloBoard);
        inv.setItem(49, closeItem);

        player.openInventory(inv);
    }

    public void openLeaderboardDetails(Player player, String type) {
        String title = "&b&lLeaderboard - ";
        List<PlayerStats> topPlayers;

        switch (type) {
            case "SG_WINS":
                title += "SG Wins";
                topPlayers = plugin.getStatsManager().getTopSGWins(10);
                break;
            case "SG_KILLS":
                title += "SG Kills";
                topPlayers = plugin.getStatsManager().getTopSGKills(10);
                break;
            case "DUEL_ELO":
                title += "Duel ELO";
                topPlayers = plugin.getStatsManager().getTopDuelElo(10);
                break;
            default:
                return;
        }

        title = ChatColor.translateAlternateColorCodes('&', title);
        Inventory inv = Bukkit.createInventory(null, 54, title);
        playerMenus.put(player.getUniqueId(), "LEADERBOARD_" + type);

        int slot = 10;
        int rank = 1;

        for (PlayerStats stats : topPlayers) {
            if (slot > 34) break;

            String playerName = Bukkit.getOfflinePlayer(stats.getUuid()).getName();
            int value = 0;
            String valueName = "";

            switch (type) {
                case "SG_WINS":
                    value = stats.getSgWins();
                    valueName = "Wins";
                    break;
                case "SG_KILLS":
                    value = stats.getSgKills();
                    valueName = "Kills";
                    break;
                case "DUEL_ELO":
                    value = stats.getDuelElo();
                    valueName = "ELO";
                    break;
            }

            Material mat = rank <= 3 ? Material.GOLD_INGOT : Material.IRON_INGOT;
            String rankColor = rank == 1 ? "&6" : rank == 2 ? "&7" : rank == 3 ? "&c" : "&b";

            ItemStack playerItem = new ItemBuilder(mat)
                    .setName(rankColor + "#" + rank + " &f" + playerName)
                    .setLore(
                            "&7" + valueName + ": &b" + value,
                            "",
                            rank == 1 ? "&6&l★ FIRST PLACE ★" :
                            rank == 2 ? "&7&l★ SECOND PLACE ★" :
                            rank == 3 ? "&c&l★ THIRD PLACE ★" : ""
                    )
                    .build();

            inv.setItem(slot, playerItem);
            slot++;
            rank++;

            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
        }

        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName("&e&lBack")
                .setLore("&7Return to leaderboards")
                .build();

        ItemStack closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Return to lobby")
                .build();

        inv.setItem(48, backItem);
        inv.setItem(49, closeItem);

        player.openInventory(inv);
    }

    public void openLanguageMenu(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        String title = lang.getMessage(player, "gui.language-menu");
        Inventory inv = Bukkit.createInventory(null, 27, title);

        playerMenus.put(player.getUniqueId(), "LANGUAGE");

        String currentLang = plugin.getDataManager().getPlayerLanguage(player.getUniqueId());

        ItemStack englishItem = new ItemBuilder(Material.PAPER)
                .setName("&b&lEnglish")
                .setLore(
                        "&7Language: English",
                        "&7Code: en_US",
                        "",
                        currentLang.equals("en_US") ? "&a✔ Currently selected" : "&e▶ Click to select"
                )
                .build();

        ItemStack danishItem = new ItemBuilder(Material.PAPER)
                .setName("&b&lDansk")
                .setLore(
                        "&7Sprog: Dansk",
                        "&7Kode: da_DK",
                        "",
                        currentLang.equals("da_DK") ? "&a✔ Nuværende valgt" : "&e▶ Klik for at vælge"
                )
                .build();

        ItemStack germanItem = new ItemBuilder(Material.PAPER)
                .setName("&b&lDeutsch")
                .setLore(
                        "&7Sprache: Deutsch",
                        "&7Code: de_DE",
                        "",
                        currentLang.equals("de_DE") ? "&a✔ Aktuell ausgewählt" : "&e▶ Klicken zum Auswählen"
                )
                .build();

        ItemStack spanishItem = new ItemBuilder(Material.PAPER)
                .setName("&b&lEspañol")
                .setLore(
                        "&7Idioma: Español",
                        "&7Código: es_ES",
                        "",
                        currentLang.equals("es_ES") ? "&a✔ Seleccionado actualmente" : "&e▶ Click para seleccionar"
                )
                .build();

        ItemStack closeItem = new ItemBuilder(Material.BARRIER)
                .setName("&c&lClose")
                .setLore("&7Return to lobby")
                .build();

        inv.setItem(10, englishItem);
        inv.setItem(12, danishItem);
        inv.setItem(14, germanItem);
        inv.setItem(16, spanishItem);
        inv.setItem(22, closeItem);

        player.openInventory(inv);
    }

    public String getPlayerMenu(Player player) {
        return playerMenus.get(player.getUniqueId());
    }

    public void removePlayerMenu(Player player) {
        playerMenus.remove(player.getUniqueId());
    }
}
