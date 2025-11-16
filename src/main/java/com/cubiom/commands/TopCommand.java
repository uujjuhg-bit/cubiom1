package com.cubiom.commands;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import com.cubiom.stats.LeaderboardManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TopCommand implements CommandExecutor {

    private final Cubiom plugin;

    public TopCommand(Cubiom plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cPlayer only command!");
            return true;
        }

        Player player = (Player) sender;
        LanguageManager lang = plugin.getLanguageManager();

        if (args.length == 0) {
            showLeaderboard(player, LeaderboardManager.LeaderboardType.SG_WINS);
            return true;
        }

        String type = args[0].toLowerCase();
        switch (type) {
            case "sgwins":
            case "sg":
                showLeaderboard(player, LeaderboardManager.LeaderboardType.SG_WINS);
                break;
            case "sgkills":
            case "kills":
                showLeaderboard(player, LeaderboardManager.LeaderboardType.SG_KILLS);
                break;
            case "duel":
            case "elo":
                showLeaderboard(player, LeaderboardManager.LeaderboardType.DUEL_ELO);
                break;
            default:
                player.sendMessage("§cUsage: /top [sgwins|sgkills|duel]");
        }

        return true;
    }

    private void showLeaderboard(Player player, LeaderboardManager.LeaderboardType type) {
        LanguageManager lang = plugin.getLanguageManager();
        LeaderboardManager lb = plugin.getLeaderboardManager();

        player.sendMessage(lang.getMessage(player, "leaderboard.title"));

        String titleKey = "";
        switch (type) {
            case SG_WINS:
                titleKey = "leaderboard.sg-wins";
                break;
            case SG_KILLS:
                titleKey = "leaderboard.sg-kills";
                break;
            case DUEL_ELO:
                titleKey = "leaderboard.duel-elo";
                break;
        }
        player.sendMessage(lang.getMessage(player, titleKey));

        List<Map.Entry<UUID, Integer>> top = null;
        switch (type) {
            case SG_WINS:
                top = lb.getTopSGWins(10);
                break;
            case SG_KILLS:
                top = lb.getTopSGKills(10);
                break;
            case DUEL_ELO:
                top = lb.getTopDuelElo(10);
                break;
        }

        if (top == null || top.isEmpty()) {
            player.sendMessage("§7No data available");
            return;
        }

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : top) {
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            Map<String, String> replacements = new HashMap<>();
            replacements.put("rank", String.valueOf(rank));
            replacements.put("player", playerName != null ? playerName : "Unknown");
            replacements.put("value", String.valueOf(entry.getValue()));
            player.sendMessage(lang.formatMessage(player, "leaderboard.entry", replacements));
            rank++;
        }

        int playerRank = lb.getPlayerRank(player.getUniqueId(), type);
        if (playerRank > 0) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("rank", String.valueOf(playerRank));
            player.sendMessage(lang.formatMessage(player, "leaderboard.your-rank", replacements));
        }
    }
}
