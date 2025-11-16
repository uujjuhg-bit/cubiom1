package com.cubiom.listeners;

import com.cubiom.Cubiom;
import com.cubiom.arenas.Arena;
import com.cubiom.language.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final Cubiom plugin;

    public InventoryClickListener(Cubiom plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String menuType = plugin.getGUIManager().getPlayerMenu(player);

        if (menuType == null) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();
        Material type = clicked.getType();

        if (type == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        switch (menuType) {
            case "SG_MAIN":
                handleSGMenu(player, slot, clicked);
                break;

            case "DUELS_MAIN":
                handleDuelsMenu(player, slot, clicked);
                break;

            case "STATS":
                if (slot == 49) player.closeInventory();
                break;

            case "LEADERBOARD":
                handleLeaderboardMenu(player, slot);
                break;

            case "LANGUAGE":
                handleLanguageMenu(player, slot);
                break;

            default:
                if (menuType.startsWith("LEADERBOARD_")) {
                    if (slot == 48) {
                        plugin.getGUIManager().openTopMenu(player);
                    } else if (slot == 49) {
                        player.closeInventory();
                    }
                } else if (menuType.startsWith("DUEL_INVITE:")) {
                    handleDuelInviteMenu(player, slot, menuType);
                }
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            plugin.getGUIManager().removePlayerMenu(player);
        }
    }

    private void handleSGMenu(Player player, int slot, ItemStack clicked) {
        if (slot == 48 && clicked.getType() == Material.PAPER) {
            plugin.getGUIManager().openStatsMenu(player);
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if ((slot >= 10 && slot <= 16) || (slot >= 19 && slot <= 25) || (slot >= 28 && slot <= 34)) {
            if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                String arenaName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                Arena arena = plugin.getSGManager().getArenaByName(arenaName);

                if (arena != null && arena.isEnabled()) {
                    player.closeInventory();
                    boolean joined = plugin.getSGManager().joinGame(player, arena);

                    LanguageManager lang = plugin.getLanguageManager();
                    if (joined) {
                        player.sendMessage(lang.getMessageWithPrefix(player, "sg.join-success"));
                    } else {
                        player.sendMessage(lang.getMessageWithPrefix(player, "sg.game-full"));
                    }
                }
            }
        }
    }

    private void handleDuelsMenu(Player player, int slot, ItemStack clicked) {
        if (slot == 48 && clicked.getType() == Material.PAPER) {
            plugin.getGUIManager().openStatsMenu(player);
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        LanguageManager lang = plugin.getLanguageManager();
        String kitName = null;

        if (slot == 11) {
            kitName = "NoDebuff";
        } else if (slot == 13) {
            kitName = "Debuff";
        } else if (slot == 15) {
            kitName = "BuildUHC";
        } else if (slot == 20) {
            kitName = "Classic";
        } else if (slot == 24) {
            kitName = "Combo";
        }

        if (kitName != null) {
            player.closeInventory();
            plugin.getDuelManager().setPlayerKit(player, kitName);
            plugin.getDuelManager().joinQueue(player);
            player.sendMessage(lang.getMessageWithPrefix(player, "duels.join-queue"));
        }
    }

    private void handleLeaderboardMenu(Player player, int slot) {
        if (slot == 20) {
            plugin.getGUIManager().openLeaderboardDetails(player, "SG_WINS");
        } else if (slot == 22) {
            plugin.getGUIManager().openLeaderboardDetails(player, "SG_KILLS");
        } else if (slot == 24) {
            plugin.getGUIManager().openLeaderboardDetails(player, "DUEL_ELO");
        } else if (slot == 49) {
            player.closeInventory();
        }
    }

    private void handleLanguageMenu(Player player, int slot) {
        LanguageManager lang = plugin.getLanguageManager();
        String newLang = null;

        if (slot == 10) {
            newLang = "en_US";
        } else if (slot == 12) {
            newLang = "da_DK";
        } else if (slot == 14) {
            newLang = "de_DE";
        } else if (slot == 16) {
            newLang = "es_ES";
        } else if (slot == 22) {
            player.closeInventory();
            return;
        }

        if (newLang != null) {
            plugin.getDataManager().setPlayerLanguage(player.getUniqueId(), newLang);
            player.closeInventory();

            plugin.getHotbarManager().giveHotbarItems(player);
            plugin.getScoreboardManager().setLobbyScoreboard(player);

            player.sendMessage(lang.getMessageWithPrefix(player, "language.changed"));
        }
    }

    private void handleDuelInviteMenu(Player player, int slot, String menuType) {
        if (slot == 22) {
            player.closeInventory();
            return;
        }

        String kitName = null;
        if (slot == 10) {
            kitName = "NoDebuff";
        } else if (slot == 11) {
            kitName = "Debuff";
        } else if (slot == 12) {
            kitName = "BuildUHC";
        } else if (slot == 14) {
            kitName = "Classic";
        } else if (slot == 15) {
            kitName = "Combo";
        }

        if (kitName != null) {
            String targetUUID = menuType.substring("DUEL_INVITE:".length());
            Player target = plugin.getServer().getPlayer(java.util.UUID.fromString(targetUUID));

            if (target != null && target.isOnline()) {
                plugin.getDuelManager().sendDuelInvite(player, target, kitName);
                player.closeInventory();
            } else {
                LanguageManager lang = plugin.getLanguageManager();
                player.sendMessage(lang.getMessageWithPrefix(player, "duels.target-offline"));
                player.closeInventory();
            }
        }
    }
}
