package com.cubiom.ui;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import com.cubiom.core.PlayerState;
import com.cubiom.game.duel.Kit;
import com.cubiom.game.sg.SGGame;
import com.cubiom.player.CubiomPlayer;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIManager {

    private final Cubiom plugin;
    private static final ItemStack BORDER_ITEM = createBorderItem();
    private static final ItemStack BACK_ITEM = createBackItem();

    public GUIManager(Cubiom plugin) {
        this.plugin = plugin;
    }

    public void openGameSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.BOLD + "Game Selector");

        fillBorders(inv, 27);

        long sgPlaying = plugin.getSGManager().getActiveGames().values().stream()
            .mapToInt(game -> game.getPlayers().size())
            .sum();

        long duelsPlaying = plugin.getDuelManager().getActiveGames().size() * 2;

        ItemStack sgItem = createItem(
            Material.DIAMOND_SWORD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "SURVIVAL GAMES",
            "",
            ChatColor.GRAY + "Battle up to 24 players in an",
            ChatColor.GRAY + "epic fight for survival!",
            "",
            ChatColor.YELLOW + "▸ " + ChatColor.WHITE + sgPlaying + ChatColor.GRAY + " playing now",
            "",
            ChatColor.YELLOW + "Click to play!"
        );
        addGlow(sgItem);
        inv.setItem(11, sgItem);

        ItemStack duelItem = createItem(
            Material.IRON_SWORD,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "DUELS",
            "",
            ChatColor.GRAY + "Test your skills in intense",
            ChatColor.GRAY + "1v1 combat across 6 unique kits!",
            "",
            ChatColor.YELLOW + "▸ " + ChatColor.WHITE + duelsPlaying + ChatColor.GRAY + " playing now",
            "",
            ChatColor.YELLOW + "Click to select kit!"
        );
        addGlow(duelItem);
        inv.setItem(15, duelItem);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    public void openSGArenaSelector(Player player) {
        List<SGArena> arenas = plugin.getArenaManager().getEnabledSGArenas();

        if (arenas.isEmpty()) {
            player.sendMessage(ChatColor.RED + "✖ No SG arenas available!");
            player.closeInventory();
            return;
        }

        int size = Math.min(54, Math.max(27, ((arenas.size() + 9) / 9) * 9));
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.GREEN + "" + ChatColor.BOLD + "Survival Games");

        fillBorders(inv, size);

        int slot = 10;
        for (SGArena arena : arenas) {
            SGGame game = plugin.getSGManager().getActiveGames().get(arena.getName());
            int players = game != null ? game.getPlayers().size() : 0;
            String stateStr = game != null ? game.getState().toString() : "WAITING";

            Material icon = players == 0 ? Material.MAP :
                           players < arena.getMaxPlayers() ? Material.EMPTY_MAP : Material.PAPER;

            ChatColor color = players == 0 ? ChatColor.GREEN :
                            players < arena.getMaxPlayers() ? ChatColor.YELLOW : ChatColor.RED;

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Players: " + color + players + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + arena.getMaxPlayers());
            lore.add(ChatColor.GRAY + "Status: " + getStatusColor(stateStr) + stateStr);
            lore.add("");

            if (players >= arena.getMaxPlayers()) {
                lore.add(ChatColor.RED + "✖ Arena Full!");
            } else if (game != null && !stateStr.equals("WAITING")) {
                lore.add(ChatColor.RED + "✖ Game in progress!");
            } else {
                lore.add(ChatColor.YELLOW + "Click to join!");
            }

            ItemStack item = createItem(icon, ChatColor.AQUA + "" + ChatColor.BOLD + arena.getName(),
                lore.toArray(new String[0]));

            inv.setItem(slot, item);
            slot++;
            if (slot % 9 >= 7) slot += 2;
        }

        ItemStack quickJoin = createItem(Material.EMERALD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "QUICK JOIN",
            "",
            ChatColor.GRAY + "Automatically join the best",
            ChatColor.GRAY + "available game!",
            "",
            ChatColor.YELLOW + "Click to quick join!"
        );
        addGlow(quickJoin);
        inv.setItem(size - 5, quickJoin);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    public void openKitSelector(Player player) {
        openKitSelector(player, null);
    }

    public void openKitSelector(Player player, String targetPlayerName) {
        Inventory inv = Bukkit.createInventory(null, 54,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Select Duel Kit");

        fillBorders(inv, 54);

        List<Kit> kits = Kit.getAllKits();
        int[] slots = {19, 21, 23, 25, 28, 30};

        for (int i = 0; i < Math.min(kits.size(), slots.length); i++) {
            Kit kit = kits.get(i);

            JsonObject stats = plugin.getSupabaseManager()
                .loadDuelStats(player.getUniqueId().toString(), kit.getName().toLowerCase())
                .join();

            int elo = stats != null && stats.has("elo") ? stats.get("elo").getAsInt() : 1000;
            int wins = stats != null && stats.has("wins") ? stats.get("wins").getAsInt() : 0;
            int losses = stats != null && stats.has("losses") ? stats.get("losses").getAsInt() : 0;
            double winrate = (wins + losses) > 0 ? (wins * 100.0 / (wins + losses)) : 0;

            ItemStack item = new ItemStack(kit.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));

            List<String> lore = new ArrayList<>();
            lore.add("");
            for (String desc : kit.getDescription()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', desc));
            }
            lore.add("");
            lore.add(ChatColor.GRAY + "Your Stats:");
            lore.add(ChatColor.YELLOW + "  ELO: " + ChatColor.WHITE + elo);
            lore.add(ChatColor.YELLOW + "  Wins: " + ChatColor.WHITE + wins + ChatColor.DARK_GRAY + " | " +
                     ChatColor.YELLOW + "Losses: " + ChatColor.WHITE + losses);
            lore.add(ChatColor.YELLOW + "  Win Rate: " + ChatColor.WHITE + String.format("%.1f%%", winrate));
            lore.add("");

            if (targetPlayerName != null) {
                lore.add(ChatColor.YELLOW + "Click to duel " + targetPlayerName + "!");
            } else {
                lore.add(ChatColor.YELLOW + "Click to join queue!");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slots[i], item);
        }

        if (targetPlayerName != null) {
            ItemStack targetInfo = createItem(Material.SKULL_ITEM,
                ChatColor.GOLD + "" + ChatColor.BOLD + "CHALLENGE",
                "",
                ChatColor.GRAY + "Challenging: " + ChatColor.WHITE + targetPlayerName,
                "",
                ChatColor.GRAY + "Select a kit to send",
                ChatColor.GRAY + "your duel request!"
            );
            inv.setItem(4, targetInfo);
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    public void openPlayerProfile(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            ChatColor.YELLOW + "" + ChatColor.BOLD + player.getName() + "'s Profile");

        fillBorders(inv, 54);

        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwner(player.getName());
        headMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + player.getName());
        headMeta.setLore(Arrays.asList(
            "",
            ChatColor.GRAY + "View your game statistics",
            ChatColor.GRAY + "and achievements!"
        ));
        head.setItemMeta(headMeta);
        inv.setItem(4, head);

        JsonObject sgStats = plugin.getSupabaseManager()
            .loadSGStats(player.getUniqueId().toString())
            .join();

        int sgWins = sgStats != null && sgStats.has("wins") ? sgStats.get("wins").getAsInt() : 0;
        int sgKills = sgStats != null && sgStats.has("kills") ? sgStats.get("kills").getAsInt() : 0;
        int sgDeaths = sgStats != null && sgStats.has("deaths") ? sgStats.get("deaths").getAsInt() : 0;
        int sgGames = sgStats != null && sgStats.has("games_played") ? sgStats.get("games_played").getAsInt() : 0;
        double sgKD = sgDeaths > 0 ? (double) sgKills / sgDeaths : sgKills;

        ItemStack sgItem = createItem(Material.DIAMOND_SWORD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "SURVIVAL GAMES",
            "",
            ChatColor.YELLOW + "Wins: " + ChatColor.WHITE + sgWins,
            ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + sgKills,
            ChatColor.YELLOW + "Deaths: " + ChatColor.WHITE + sgDeaths,
            ChatColor.YELLOW + "K/D: " + ChatColor.WHITE + String.format("%.2f", sgKD),
            ChatColor.YELLOW + "Games: " + ChatColor.WHITE + sgGames
        );
        addGlow(sgItem);
        inv.setItem(20, sgItem);

        int totalElo = 0;
        int totalWins = 0;
        int totalLosses = 0;
        String[] kitTypes = {"nodebuff", "debuff", "classic", "builduhc", "combo", "sg"};

        for (String kitType : kitTypes) {
            JsonObject stats = plugin.getSupabaseManager()
                .loadDuelStats(player.getUniqueId().toString(), kitType)
                .join();

            if (stats != null) {
                totalElo += stats.has("elo") ? stats.get("elo").getAsInt() : 0;
                totalWins += stats.has("wins") ? stats.get("wins").getAsInt() : 0;
                totalLosses += stats.has("losses") ? stats.get("losses").getAsInt() : 0;
            }
        }

        int avgElo = totalElo / kitTypes.length;
        double duelWinrate = (totalWins + totalLosses) > 0 ? (totalWins * 100.0 / (totalWins + totalLosses)) : 0;

        ItemStack duelItem = createItem(Material.IRON_SWORD,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "DUELS",
            "",
            ChatColor.YELLOW + "Average ELO: " + ChatColor.WHITE + avgElo,
            ChatColor.YELLOW + "Total Wins: " + ChatColor.WHITE + totalWins,
            ChatColor.YELLOW + "Total Losses: " + ChatColor.WHITE + totalLosses,
            ChatColor.YELLOW + "Win Rate: " + ChatColor.WHITE + String.format("%.1f%%", duelWinrate),
            "",
            ChatColor.GRAY + "Click for detailed kit stats!"
        );
        addGlow(duelItem);
        inv.setItem(24, duelItem);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    public void openActivePlayers(Player player) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.remove(player);

        if (online.isEmpty()) {
            player.sendMessage(ChatColor.RED + "✖ No other players online!");
            player.closeInventory();
            return;
        }

        int size = Math.min(54, Math.max(27, ((online.size() + 9) / 9) * 9));
        Inventory inv = Bukkit.createInventory(null, size,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Active Players");

        fillBorders(inv, size);

        int slot = 10;
        for (Player p : online) {
            CubiomPlayer cp = plugin.getPlayerManager().getPlayer(p);
            String stateStr = cp != null ? cp.getState().toString() : "UNKNOWN";
            ChatColor stateColor = getStateColor(stateStr);

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwner(p.getName());
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + p.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Status: " + stateColor + stateStr);

            if (cp != null && cp.getCurrentArena() != null) {
                lore.add(ChatColor.GRAY + "Arena: " + ChatColor.WHITE + cp.getCurrentArena());
            }

            lore.add("");

            if (cp != null && cp.isInLobby()) {
                lore.add(ChatColor.YELLOW + "Left-Click: " + ChatColor.WHITE + "View Profile");
                lore.add(ChatColor.YELLOW + "Right-Click: " + ChatColor.WHITE + "Duel Request");
            } else {
                lore.add(ChatColor.RED + "Player is busy!");
            }

            meta.setLore(lore);
            head.setItemMeta(meta);

            inv.setItem(slot, head);
            slot++;
            if (slot % 9 >= 7) slot += 2;
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    public void openLeaderboards(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Leaderboards");

        fillBorders(inv, 27);

        ItemStack sgWins = createItem(Material.DIAMOND_SWORD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "SG WINS",
            "",
            ChatColor.GRAY + "View the top 10 players",
            ChatColor.GRAY + "with the most SG wins!",
            "",
            ChatColor.YELLOW + "Click to view!"
        );
        addGlow(sgWins);
        inv.setItem(11, sgWins);

        ItemStack duelElo = createItem(Material.IRON_SWORD,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "DUEL ELO",
            "",
            ChatColor.GRAY + "View the top 10 players",
            ChatColor.GRAY + "with the highest ELO!",
            "",
            ChatColor.YELLOW + "Click to select kit!"
        );
        addGlow(duelElo);
        inv.setItem(15, duelElo);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    public void openSettings(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
            ChatColor.RED + "" + ChatColor.BOLD + "Settings");

        fillBorders(inv, 27);

        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        String currentLang = cp != null ? cp.getLanguage() : "en_US";
        String langDisplay = getLangDisplay(currentLang);

        ItemStack language = createItem(Material.PAPER,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "LANGUAGE",
            "",
            ChatColor.GRAY + "Current: " + ChatColor.WHITE + langDisplay,
            "",
            ChatColor.GRAY + "Available languages:",
            ChatColor.WHITE + "• English (en_US)",
            ChatColor.WHITE + "• Dansk (da_DK)",
            ChatColor.WHITE + "• Deutsch (de_DE)",
            ChatColor.WHITE + "• Español (es_ES)",
            "",
            ChatColor.YELLOW + "Click to change!"
        );
        inv.setItem(13, language);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    private static void fillBorders(Inventory inv, int size) {
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, BORDER_ITEM);
        }
        for (int i = size - 9; i < size; i++) {
            inv.setItem(i, BORDER_ITEM);
        }
        for (int i = 9; i < size - 9; i += 9) {
            inv.setItem(i, BORDER_ITEM);
            if (i + 8 < size) inv.setItem(i + 8, BORDER_ITEM);
        }
    }

    private static ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "← BACK");
        meta.setLore(Arrays.asList("", ChatColor.GRAY + "Return to previous menu"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    private void addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
    }

    private ChatColor getStatusColor(String status) {
        if (status.equals("WAITING")) return ChatColor.GREEN;
        if (status.equals("COUNTDOWN")) return ChatColor.YELLOW;
        if (status.equals("ACTIVE")) return ChatColor.RED;
        return ChatColor.GRAY;
    }

    private ChatColor getStateColor(String state) {
        if (state.equals("LOBBY")) return ChatColor.GREEN;
        if (state.equals("IN_GAME")) return ChatColor.YELLOW;
        if (state.equals("SPECTATING")) return ChatColor.AQUA;
        return ChatColor.GRAY;
    }

    private String getLangDisplay(String code) {
        switch (code) {
            case "en_US": return "English";
            case "da_DK": return "Dansk";
            case "de_DE": return "Deutsch";
            case "es_ES": return "Español";
            default: return "English";
        }
    }

    public void openLanguageSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Language Selector");

        fillBorders(inv, 27);

        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
        String currentLang = cp != null ? cp.getLanguage() : "en_US";

        ItemStack enUs = createItem(Material.PAPER,
            ChatColor.WHITE + "" + ChatColor.BOLD + "English",
            "",
            ChatColor.GRAY + "Set language to English",
            "",
            currentLang.equals("en_US") ? ChatColor.GREEN + "✓ Currently selected" : ChatColor.YELLOW + "Click to select"
        );
        if (currentLang.equals("en_US")) addGlow(enUs);
        inv.setItem(11, enUs);

        ItemStack daDk = createItem(Material.PAPER,
            ChatColor.WHITE + "" + ChatColor.BOLD + "Dansk",
            "",
            ChatColor.GRAY + "Sæt sprog til Dansk",
            "",
            currentLang.equals("da_DK") ? ChatColor.GREEN + "✓ Valgt i øjeblikket" : ChatColor.YELLOW + "Klik for at vælge"
        );
        if (currentLang.equals("da_DK")) addGlow(daDk);
        inv.setItem(13, daDk);

        ItemStack deDe = createItem(Material.PAPER,
            ChatColor.WHITE + "" + ChatColor.BOLD + "Deutsch",
            "",
            ChatColor.GRAY + "Sprache auf Deutsch setzen",
            "",
            currentLang.equals("de_DE") ? ChatColor.GREEN + "✓ Aktuell ausgewählt" : ChatColor.YELLOW + "Zum Auswählen klicken"
        );
        if (currentLang.equals("de_DE")) addGlow(deDe);
        inv.setItem(15, deDe);

        ItemStack esEs = createItem(Material.PAPER,
            ChatColor.WHITE + "" + ChatColor.BOLD + "Español",
            "",
            ChatColor.GRAY + "Establecer idioma en Español",
            "",
            currentLang.equals("es_ES") ? ChatColor.GREEN + "✓ Actualmente seleccionado" : ChatColor.YELLOW + "Haz clic para seleccionar"
        );
        if (currentLang.equals("es_ES")) addGlow(esEs);
        inv.setItem(22, esEs);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    public void openSGWinsLeaderboard(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Top 10 - SG Wins");

        fillBorders(inv, 54);

        List<JsonObject> topPlayers = plugin.getSupabaseManager()
            .getTopSGWins(10)
            .join();

        int slot = 10;
        int position = 1;
        for (JsonObject playerData : topPlayers) {
            String playerName = playerData.has("player_name") ?
                playerData.get("player_name").getAsString() : "Unknown";
            int wins = playerData.has("wins") ? playerData.get("wins").getAsInt() : 0;
            int kills = playerData.has("kills") ? playerData.get("kills").getAsInt() : 0;
            int deaths = playerData.has("deaths") ? playerData.get("deaths").getAsInt() : 0;
            double kd = deaths > 0 ? (double) kills / deaths : kills;

            Material icon = position == 1 ? Material.GOLD_BLOCK :
                          position == 2 ? Material.IRON_BLOCK :
                          position == 3 ? Material.EMERALD_BLOCK : Material.DIAMOND;

            ChatColor rankColor = position <= 3 ? ChatColor.GOLD : ChatColor.YELLOW;

            ItemStack item = createItem(icon,
                rankColor + "#" + position + " " + ChatColor.WHITE + playerName,
                "",
                ChatColor.YELLOW + "Wins: " + ChatColor.WHITE + wins,
                ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + kills,
                ChatColor.YELLOW + "K/D: " + ChatColor.WHITE + String.format("%.2f", kd)
            );
            if (position <= 3) addGlow(item);

            inv.setItem(slot, item);
            slot++;
            if (slot % 9 >= 7) slot += 2;
            position++;
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
    }

    public void openDuelKitSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Select Leaderboard Kit");

        fillBorders(inv, 27);

        List<Kit> kits = Kit.getAllKits();
        int[] slots = {10, 11, 12, 14, 15, 16};

        for (int i = 0; i < Math.min(kits.size(), slots.length); i++) {
            Kit kit = kits.get(i);
            ItemStack item = new ItemStack(kit.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));
            meta.setLore(Arrays.asList(
                "",
                ChatColor.GRAY + "View top 10 players",
                ChatColor.GRAY + "in this kit!",
                "",
                ChatColor.YELLOW + "Click to view!"
            ));
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    public void openDuelEloLeaderboard(Player player, String kitName) {
        Inventory inv = Bukkit.createInventory(null, 54,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Top 10 - " + kitName.toUpperCase() + " ELO");

        fillBorders(inv, 54);

        List<JsonObject> topPlayers = plugin.getSupabaseManager()
            .getTopDuelElo(kitName, 10)
            .join();

        int slot = 10;
        int position = 1;
        for (JsonObject playerData : topPlayers) {
            String playerName = playerData.has("player_name") ?
                playerData.get("player_name").getAsString() : "Unknown";
            int elo = playerData.has("elo") ? playerData.get("elo").getAsInt() : 1000;
            int wins = playerData.has("wins") ? playerData.get("wins").getAsInt() : 0;
            int losses = playerData.has("losses") ? playerData.get("losses").getAsInt() : 0;
            double winrate = (wins + losses) > 0 ? (wins * 100.0 / (wins + losses)) : 0;

            Material icon = position == 1 ? Material.GOLD_BLOCK :
                          position == 2 ? Material.IRON_BLOCK :
                          position == 3 ? Material.EMERALD_BLOCK : Material.DIAMOND;

            ChatColor rankColor = position <= 3 ? ChatColor.GOLD : ChatColor.YELLOW;

            ItemStack item = createItem(icon,
                rankColor + "#" + position + " " + ChatColor.WHITE + playerName,
                "",
                ChatColor.YELLOW + "ELO: " + ChatColor.WHITE + elo,
                ChatColor.YELLOW + "Wins: " + ChatColor.WHITE + wins,
                ChatColor.YELLOW + "Losses: " + ChatColor.WHITE + losses,
                ChatColor.YELLOW + "Win Rate: " + ChatColor.WHITE + String.format("%.1f%%", winrate)
            );
            if (position <= 3) addGlow(item);

            inv.setItem(slot, item);
            slot++;
            if (slot % 9 >= 7) slot += 2;
            position++;
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
    }

    public void openDuelKitStats(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Your Duel Kit Stats");

        fillBorders(inv, 54);

        List<Kit> kits = Kit.getAllKits();
        int[] slots = {19, 21, 23, 25, 28, 30};

        for (int i = 0; i < Math.min(kits.size(), slots.length); i++) {
            Kit kit = kits.get(i);

            JsonObject stats = plugin.getSupabaseManager()
                .loadDuelStats(player.getUniqueId().toString(), kit.getName().toLowerCase())
                .join();

            int elo = stats != null && stats.has("elo") ? stats.get("elo").getAsInt() : 1000;
            int wins = stats != null && stats.has("wins") ? stats.get("wins").getAsInt() : 0;
            int losses = stats != null && stats.has("losses") ? stats.get("losses").getAsInt() : 0;
            double winrate = (wins + losses) > 0 ? (wins * 100.0 / (wins + losses)) : 0;

            ItemStack item = new ItemStack(kit.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.YELLOW + "ELO: " + ChatColor.WHITE + elo);
            lore.add(ChatColor.YELLOW + "Wins: " + ChatColor.WHITE + wins);
            lore.add(ChatColor.YELLOW + "Losses: " + ChatColor.WHITE + losses);
            lore.add(ChatColor.YELLOW + "Win Rate: " + ChatColor.WHITE + String.format("%.1f%%", winrate));

            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slots[i], item);
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }
}
