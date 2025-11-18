package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.arena.SGArena;
import com.cubiom.core.PlayerState;
import com.cubiom.game.duel.Kit;
import com.cubiom.player.CubiomPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryClickListener implements Listener {

    private final Cubiom plugin;

    public InventoryClickListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);

        if (cp == null) return;

        if (cp.getState() == PlayerState.LOBBY || cp.getState() == PlayerState.SPECTATING) {
            event.setCancelled(true);
        }

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        if (title.contains("Game Selector")) {
            event.setCancelled(true);
            if (displayName.contains("Survival Games")) {
                player.closeInventory();
                plugin.getGUIManager().openSGArenaSelector(player);
            } else if (displayName.contains("Duels")) {
                player.closeInventory();
                plugin.getGUIManager().openKitSelector(player);
            }
        } else if (title.contains("Select SG Arena")) {
            event.setCancelled(true);
            String arenaName = ChatColor.stripColor(displayName);
            SGArena arena = plugin.getArenaManager().getSGArena(arenaName);
            if (arena != null) {
                player.closeInventory();
                plugin.getSGManager().joinGame(player, arena.getName());
            }
        } else if (title.contains("Select Duel Kit")) {
            event.setCancelled(true);
            String kitName = ChatColor.stripColor(displayName).split(" ")[0].toLowerCase();
            Kit kit = Kit.getKit(kitName);
            if (kit != null) {
                ItemStack challengeItem = event.getInventory().getItem(4);
                String targetName = null;
                if (challengeItem != null && challengeItem.hasItemMeta() &&
                    challengeItem.getItemMeta().hasDisplayName() &&
                    challengeItem.getItemMeta().getDisplayName().contains("CHALLENGE")) {
                    List<String> lore = challengeItem.getItemMeta().getLore();
                    if (lore != null && lore.size() > 1) {
                        String challengingLine = ChatColor.stripColor(lore.get(1));
                        if (challengingLine.startsWith("Challenging: ")) {
                            targetName = challengingLine.substring("Challenging: ".length());
                        }
                    }
                }

                player.closeInventory();

                if (targetName != null) {
                    Player target = org.bukkit.Bukkit.getPlayer(targetName);
                    if (target != null && target.isOnline()) {
                        plugin.getDuelManager().sendDuelInvite(player, target, kit.getName().toLowerCase());
                    } else {
                        player.sendMessage(ChatColor.RED + "Player " + targetName + " is no longer online!");
                    }
                } else {
                    plugin.getDuelManager().joinQueue(player, kit.getName().toLowerCase());
                }
            }
        } else if (title.contains("Active Players")) {
            event.setCancelled(true);
        } else if (title.contains("Leaderboards")) {
            event.setCancelled(true);
            if (displayName.contains("SG WINS")) {
                player.closeInventory();
                plugin.getGUIManager().openSGWinsLeaderboard(player);
            } else if (displayName.contains("DUEL ELO")) {
                player.closeInventory();
                plugin.getGUIManager().openDuelKitSelector(player);
            }
        } else if (title.contains("Select Leaderboard Kit")) {
            event.setCancelled(true);
            String kitName = ChatColor.stripColor(displayName).split(" ")[0].toLowerCase();
            player.closeInventory();
            plugin.getGUIManager().openDuelEloLeaderboard(player, kitName);
        } else if (title.contains("Top 10")) {
            event.setCancelled(true);
        } else if (title.contains("Settings")) {
            event.setCancelled(true);
            if (displayName.contains("LANGUAGE")) {
                player.closeInventory();
                plugin.getGUIManager().openLanguageSelector(player);
            }
        } else if (title.contains("Language Selector")) {
            event.setCancelled(true);
            String langCode = null;
            if (displayName.contains("English")) langCode = "en_US";
            else if (displayName.contains("Dansk")) langCode = "da_DK";
            else if (displayName.contains("Deutsch")) langCode = "de_DE";
            else if (displayName.contains("Español")) langCode = "es_ES";

            if (langCode != null) {
                CubiomPlayer cp = plugin.getPlayerManager().getPlayer(player);
                cp.setLanguage(langCode);
                plugin.getSupabaseManager().upsertPlayer(
                    player.getUniqueId().toString(),
                    player.getName(),
                    langCode
                );
                player.closeInventory();
                player.sendMessage(plugin.getLanguageManager().getMessage(player, "language.changed")
                    .replace("{0}", plugin.getLanguageManager().getLanguageName(langCode)));
                com.cubiom.ui.LobbyHotbar.giveLobbyItems(player);
                plugin.getScoreboardManager().updateScoreboard(player);
            }
        } else if (title.contains("Profile")) {
            event.setCancelled(true);
            if (displayName.contains("DUELS")) {
                player.closeInventory();
                plugin.getGUIManager().openDuelKitStats(player);
            }
        } else if (title.contains("Duel Kit Stats")) {
            event.setCancelled(true);
        } else if (title.contains("Survival Games")) {
            event.setCancelled(true);
            if (displayName.contains("QUICK JOIN")) {
                player.closeInventory();
                SGArena arena = plugin.getArenaManager().getEnabledSGArenas().stream()
                    .findFirst()
                    .orElse(null);
                if (arena != null) {
                    plugin.getSGManager().joinGame(player, arena.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "No arenas available!");
                }
            } else {
                String arenaName = ChatColor.stripColor(displayName);
                SGArena arena = plugin.getArenaManager().getSGArena(arenaName);
                if (arena != null) {
                    player.closeInventory();
                    plugin.getSGManager().joinGame(player, arena.getName());
                }
            }
        }
    }
}
