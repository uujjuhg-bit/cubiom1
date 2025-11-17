package com.cubiom.arena;

import com.cubiom.Cubiom;
import com.cubiom.language.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ArenaSetupSession {

    private final UUID playerUUID;
    private final String arenaName;
    private final ArenaType type;
    private final Cubiom plugin;

    private SGArena sgArena;
    private DuelArena duelArena;

    private Location corner1;
    private Location corner2;

    public ArenaSetupSession(Player player, String arenaName, ArenaType type, Cubiom plugin) {
        this.playerUUID = player.getUniqueId();
        this.arenaName = arenaName;
        this.type = type;
        this.plugin = plugin;

        if (type == ArenaType.SURVIVAL_GAMES) {
            this.sgArena = new SGArena(arenaName);
        } else {
            this.duelArena = new DuelArena(arenaName);
        }
    }

    public void setLobbySpawn(Location location) {
        if (type == ArenaType.SURVIVAL_GAMES && sgArena != null) {
            sendMessage("sg.setup.lobby-set");
        }
    }

    public void addPlayerSpawn(Location location) {
        if (type == ArenaType.SURVIVAL_GAMES && sgArena != null) {
            sgArena.addPlayerSpawn(location);
            sendMessage("sg.setup.spawn-added", sgArena.getPlayerSpawns().size());
        }
    }

    public void addTier1Chest(Block chest) {
        if (type == ArenaType.SURVIVAL_GAMES && sgArena != null) {
            sgArena.addTier1Chest(chest.getLocation());
            sendMessage("sg.setup.tier1-added", sgArena.getTier1Chests().size());
        }
    }

    public void addTier2Chest(Block chest) {
        if (type == ArenaType.SURVIVAL_GAMES && sgArena != null) {
            sgArena.addTier2Chest(chest.getLocation());
            sendMessage("sg.setup.tier2-added", sgArena.getTier2Chests().size());
        }
    }

    public void setDeathmatchSpawn(Location location) {
        if (type == ArenaType.SURVIVAL_GAMES && sgArena != null) {
            sgArena.addDeathmatchSpawn(location);
            sendMessage("sg.setup.deathmatch-set");
        }
    }

    public void setSpectatorSpawn(Location location) {
        if (type == ArenaType.SURVIVAL_GAMES && sgArena != null) {
            sgArena.setSpectatorSpawn(location);
            sendMessage("sg.setup.spectator-set");
        }
    }

    public void setCorner1(Location location) {
        this.corner1 = location;
        sendMessage("arena.setup.corner1-set");
    }

    public void setCorner2(Location location) {
        this.corner2 = location;
        sendMessage("arena.setup.corner2-set");
    }

    public void setSpawn1(Location location) {
        if (type == ArenaType.DUEL && duelArena != null) {
            duelArena.setSpawn1(location);
            sendMessage("duel.setup.spawn1-set");
        }
    }

    public void setSpawn2(Location location) {
        if (type == ArenaType.DUEL && duelArena != null) {
            duelArena.setSpawn2(location);
            sendMessage("duel.setup.spawn2-set");
        }
    }

    public void setMinPlayers(int min) {
        if (type == ArenaType.SURVIVAL_GAMES && sgArena != null) {
            sgArena.setMinPlayers(min);
            sendMessage("sg.setup.min-set", min);
        }
    }

    public void setMaxPlayers(int max) {
        if (type == ArenaType.SURVIVAL_GAMES && sgArena != null) {
            sgArena.setMaxPlayers(max);
            sendMessage("sg.setup.max-set", max);
        }
    }

    public boolean complete() {
        if (type == ArenaType.SURVIVAL_GAMES) {
            if (!sgArena.isValid()) {
                sendMessage("sg.setup.incomplete");
                return false;
            }
            sgArena.setWorldName(getPlayer().getWorld().getName());
            plugin.getArenaManager().addSGArena(sgArena);
            sendMessage("sg.setup.complete", arenaName);
            return true;
        } else {
            if (!duelArena.isValid()) {
                sendMessage("duel.setup.incomplete");
                return false;
            }
            if (corner1 != null && corner2 != null) {
                duelArena.setCorner1(corner1);
                duelArena.setCorner2(corner2);
            }
            duelArena.setWorldName(getPlayer().getWorld().getName());
            plugin.getArenaManager().addDuelArena(duelArena);
            sendMessage("duel.setup.complete", arenaName);
            return true;
        }
    }

    public Player getPlayer() {
        return plugin.getServer().getPlayer(playerUUID);
    }

    public String getArenaName() {
        return arenaName;
    }

    public ArenaType getType() {
        return type;
    }

    public SGArena getSGArena() {
        return sgArena;
    }

    public DuelArena getDuelArena() {
        return duelArena;
    }

    private void sendMessage(String key, Object... args) {
        Player player = getPlayer();
        if (player != null) {
            LanguageManager lm = plugin.getLanguageManager();
            String message = lm.getMessage(player, key);
            for (int i = 0; i < args.length; i++) {
                message = message.replace("{" + i + "}", String.valueOf(args[i]));
            }
            player.sendMessage(message);
        }
    }

    public enum ArenaType {
        SURVIVAL_GAMES,
        DUEL
    }
}
