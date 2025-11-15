package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import com.cubiom.language.LanguageManager;
import com.cubiom.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SGCommand implements CommandExecutor {

    private final Cubiom plugin;
    private final Map<String, Arena> setupArenas;

    public SGCommand(Cubiom plugin) {
        this.plugin = plugin;
        this.setupArenas = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        LanguageManager langManager = plugin.getLanguageManager();

        if (args.length == 0) {
            player.sendMessage("§c§lSurvival Games Commands:");
            player.sendMessage("§7/sg join §f- Join a game");
            player.sendMessage("§7/sg leave §f- Leave current game");
            player.sendMessage("§7/sg stats §f- View your stats");
            player.sendMessage("§7/sg top §f- View top players");
            player.sendMessage("§7/sg list §f- List all arenas");
            if (player.hasPermission("cubiom.admin.sg")) {
                player.sendMessage("");
                player.sendMessage("§c§lAdmin Commands:");
                player.sendMessage("§7/sg create <name> §f- Create new arena");
                player.sendMessage("§7/sg setlobby §f- Set lobby spawn");
                player.sendMessage("§7/sg addspawn §f- Add player spawn");
                player.sendMessage("§7/sg addtier1 §f- Add tier 1 chest");
                player.sendMessage("§7/sg addtier2 §f- Add tier 2 chest");
                player.sendMessage("§7/sg setdm §f- Set deathmatch spawn");
                player.sendMessage("§7/sg setmin <num> §f- Set min players");
                player.sendMessage("§7/sg setmax <num> §f- Set max players");
                player.sendMessage("§7/sg enable §f- Enable arena");
                player.sendMessage("§7/sg disable §f- Disable arena");
                player.sendMessage("§7/sg delete <name> §f- Delete arena");
                player.sendMessage("§7/sg info <name> §f- Arena info");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                handleJoin(player, langManager);
                break;

            case "leave":
                handleLeave(player, langManager);
                break;

            case "stats":
                if (args.length == 1) {
                    handleStats(player, langManager);
                } else {
                    player.sendMessage("§cUsage: /sg stats");
                }
                break;

            case "top":
                handleTop(player, langManager);
                break;

            case "list":
                handleList(player);
                break;

            case "create":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /sg create <name>");
                    return true;
                }
                handleCreate(player, args[1]);
                break;

            case "setlobby":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleSetLobby(player);
                break;

            case "addspawn":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleAddSpawn(player);
                break;

            case "addtier1":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleAddTier1(player);
                break;

            case "addtier2":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleAddTier2(player);
                break;

            case "setdm":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleSetDeathmatch(player);
                break;

            case "setmin":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /sg setmin <number>");
                    return true;
                }
                handleSetMin(player, args[1]);
                break;

            case "setmax":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /sg setmax <number>");
                    return true;
                }
                handleSetMax(player, args[1]);
                break;

            case "enable":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleEnable(player);
                break;

            case "disable":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                handleDisable(player);
                break;

            case "delete":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /sg delete <name>");
                    return true;
                }
                handleDelete(player, args[1]);
                break;

            case "info":
                if (!player.hasPermission("cubiom.admin.sg")) {
                    player.sendMessage(langManager.getMessageWithPrefix(player, "general.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /sg info <name>");
                    return true;
                }
                handleInfo(player, args[1]);
                break;

            default:
                player.sendMessage("§cUnknown subcommand. Use /sg for help.");
                break;
        }

        return true;
    }

    private void handleJoin(Player player, LanguageManager langManager) {
        if (plugin.getSGManager().joinGame(player)) {
            player.sendMessage(langManager.getMessageWithPrefix(player, "sg.join-success"));
        } else {
            player.sendMessage(langManager.getMessageWithPrefix(player, "sg.already-in-game"));
        }
    }

    private void handleLeave(Player player, LanguageManager langManager) {
        if (plugin.getSGManager().isInGame(player)) {
            plugin.getSGManager().leaveGame(player);
            player.sendMessage(langManager.getMessageWithPrefix(player, "sg.leave-success"));
            plugin.getHotbarManager().giveLobbyHotbar(player);
        } else {
            player.sendMessage(langManager.getMessageWithPrefix(player, "sg.not-in-game"));
        }
    }

    private void handleStats(Player player, LanguageManager langManager) {
        PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());

        player.sendMessage("§b§l§m                                    ");
        player.sendMessage(langManager.getMessage(player, "stats.title"));
        player.sendMessage("");

        Map<String, String> replacements = new HashMap<>();
        replacements.put("wins", String.valueOf(stats.getSgWins()));
        replacements.put("kills", String.valueOf(stats.getSgKills()));
        replacements.put("deaths", String.valueOf(stats.getSgDeaths()));
        replacements.put("kdr", String.format("%.2f", stats.getSgKDR()));

        player.sendMessage(langManager.formatMessage(player, "stats.sg-wins", replacements));
        player.sendMessage(langManager.formatMessage(player, "stats.sg-kills", replacements));
        player.sendMessage(langManager.formatMessage(player, "stats.sg-deaths", replacements));
        player.sendMessage(langManager.formatMessage(player, "stats.sg-kdr", replacements));
        player.sendMessage("§b§l§m                                    ");
    }

    private void handleTop(Player player, LanguageManager langManager) {
        player.sendMessage("§b§l§m                                    ");
        player.sendMessage("§b§lTop SG Players");
        player.sendMessage("");
        player.sendMessage("§7Coming soon...");
        player.sendMessage("§b§l§m                                    ");
    }

    private void handleList(Player player) {
        Map<String, Arena> arenas = plugin.getDataManager().getArenas();

        if (arenas.isEmpty()) {
            player.sendMessage("§cNo arenas found!");
            return;
        }

        player.sendMessage("§b§l§m                                    ");
        player.sendMessage("§b§lSG Arenas");
        player.sendMessage("");

        for (Arena arena : arenas.values()) {
            String status = arena.isEnabled() ? "§a✓ Enabled" : "§c✗ Disabled";
            String valid = arena.isValid() ? "§a(Valid)" : "§c(Invalid)";
            player.sendMessage(String.format("§7- §b%s %s %s", arena.getName(), status, valid));
        }

        player.sendMessage("§b§l§m                                    ");
    }

    private void handleCreate(Player player, String name) {
        if (plugin.getDataManager().getArenas().containsKey(name)) {
            player.sendMessage("§cArena already exists!");
            return;
        }

        Arena arena = new Arena(name);
        setupArenas.put(player.getName(), arena);

        player.sendMessage("§a§l✓ §aCreated arena: §b" + name);
        player.sendMessage("§7Now configure it:");
        player.sendMessage("§7 1. /sg setlobby");
        player.sendMessage("§7 2. /sg addspawn (multiple times)");
        player.sendMessage("§7 3. /sg addtier1 (multiple times)");
        player.sendMessage("§7 4. /sg addtier2 (optional)");
        player.sendMessage("§7 5. /sg setdm");
        player.sendMessage("§7 6. /sg setmin <number>");
        player.sendMessage("§7 7. /sg setmax <number>");
        player.sendMessage("§7 8. /sg enable");
    }

    private void handleSetLobby(Player player) {
        Arena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /sg create <name>");
            return;
        }

        arena.setLobbySpawn(player.getLocation());
        player.sendMessage("§a§l✓ §aLobby spawn set!");
    }

    private void handleAddSpawn(Player player) {
        Arena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /sg create <name>");
            return;
        }

        arena.addSpawnPoint(player.getLocation());
        player.sendMessage("§a§l✓ §aPlayer spawn added! §7(Total: " + arena.getSpawnPoints().size() + ")");
    }

    private void handleAddTier1(Player player) {
        Arena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /sg create <name>");
            return;
        }

        Location loc = player.getTargetBlock(null, 5).getLocation();
        arena.addTier1Chest(loc);
        player.sendMessage("§a§l✓ §aTier 1 chest added! §7(Total: " + arena.getTier1Chests().size() + ")");
        player.sendMessage("§7Look at another chest and run the command again");
    }

    private void handleAddTier2(Player player) {
        Arena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /sg create <name>");
            return;
        }

        Location loc = player.getTargetBlock(null, 5).getLocation();
        arena.addTier2Chest(loc);
        player.sendMessage("§a§l✓ §aTier 2 chest added! §7(Total: " + arena.getTier2Chests().size() + ")");
    }

    private void handleSetDeathmatch(Player player) {
        Arena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /sg create <name>");
            return;
        }

        arena.setDeathmatchSpawn(player.getLocation());
        player.sendMessage("§a§l✓ §aDeathmatch spawn set!");
    }

    private void handleSetMin(Player player, String numStr) {
        Arena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /sg create <name>");
            return;
        }

        try {
            int num = Integer.parseInt(numStr);
            arena.setMinPlayers(num);
            player.sendMessage("§a§l✓ §aMin players set to: §b" + num);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number!");
        }
    }

    private void handleSetMax(Player player, String numStr) {
        Arena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /sg create <name>");
            return;
        }

        try {
            int num = Integer.parseInt(numStr);
            arena.setMaxPlayers(num);
            player.sendMessage("§a§l✓ §aMax players set to: §b" + num);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number!");
        }
    }

    private void handleEnable(Player player) {
        Arena arena = setupArenas.get(player.getName());
        if (arena == null) {
            player.sendMessage("§cYou must create an arena first! Use /sg create <name>");
            return;
        }

        if (!arena.isValid()) {
            player.sendMessage("§c§l✗ §cArena is not valid! Missing:");
            if (arena.getLobbySpawn() == null) player.sendMessage("§c  - Lobby spawn");
            if (arena.getSpawnPoints().isEmpty()) player.sendMessage("§c  - Player spawns");
            if (arena.getTier1Chests().isEmpty()) player.sendMessage("§c  - Tier 1 chests");
            if (arena.getDeathmatchSpawn() == null) player.sendMessage("§c  - Deathmatch spawn");
            return;
        }

        arena.setEnabled(true);
        plugin.getDataManager().getArenas().put(arena.getName(), arena);
        plugin.getDataManager().save();
        setupArenas.remove(player.getName());

        player.sendMessage("§a§l✓ §aArena enabled and saved!");
        player.sendMessage("§7Players can now join with §b/sg join");
    }

    private void handleDisable(Player player) {
        player.sendMessage("§cUsage: /sg disable <name>");
    }

    private void handleDelete(Player player, String name) {
        if (!plugin.getDataManager().getArenas().containsKey(name)) {
            player.sendMessage("§cArena not found!");
            return;
        }

        plugin.getDataManager().getArenas().remove(name);
        plugin.getDataManager().save();
        player.sendMessage("§a§l✓ §aArena deleted: §b" + name);
    }

    private void handleInfo(Player player, String name) {
        Arena arena = plugin.getDataManager().getArenas().get(name);
        if (arena == null) {
            player.sendMessage("§cArena not found!");
            return;
        }

        player.sendMessage("§b§l§m                                    ");
        player.sendMessage("§b§lArena Info: §f" + arena.getName());
        player.sendMessage("");
        player.sendMessage("§7World: §b" + arena.getWorldName());
        player.sendMessage("§7Status: " + (arena.isEnabled() ? "§a✓ Enabled" : "§c✗ Disabled"));
        player.sendMessage("§7Valid: " + (arena.isValid() ? "§a✓ Yes" : "§c✗ No"));
        player.sendMessage("§7Min Players: §b" + arena.getMinPlayers());
        player.sendMessage("§7Max Players: §b" + arena.getMaxPlayers());
        player.sendMessage("§7Player Spawns: §b" + arena.getSpawnPoints().size());
        player.sendMessage("§7Tier 1 Chests: §b" + arena.getTier1Chests().size());
        player.sendMessage("§7Tier 2 Chests: §b" + arena.getTier2Chests().size());
        player.sendMessage("§b§l§m                                    ");
    }
}
