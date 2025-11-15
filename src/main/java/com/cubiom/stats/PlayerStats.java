package com.cubiom.stats;

import java.util.UUID;

public class PlayerStats {

    private UUID uuid;

    private int sgWins;
    private int sgKills;
    private int sgDeaths;
    private int sgGamesPlayed;

    private int duelWins;
    private int duelLosses;
    private int duelElo;

    private long lastPlayed;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
        this.sgWins = 0;
        this.sgKills = 0;
        this.sgDeaths = 0;
        this.sgGamesPlayed = 0;
        this.duelWins = 0;
        this.duelLosses = 0;
        this.duelElo = 1000;
        this.lastPlayed = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getSgWins() {
        return sgWins;
    }

    public void addSgWin() {
        this.sgWins++;
    }

    public int getSgKills() {
        return sgKills;
    }

    public void addSgKill() {
        this.sgKills++;
    }

    public int getSgDeaths() {
        return sgDeaths;
    }

    public void addSgDeath() {
        this.sgDeaths++;
    }

    public int getSgGamesPlayed() {
        return sgGamesPlayed;
    }

    public void addSgGamePlayed() {
        this.sgGamesPlayed++;
    }

    public double getSgKDR() {
        if (sgDeaths == 0) {
            return sgKills;
        }
        return (double) sgKills / sgDeaths;
    }

    public int getDuelWins() {
        return duelWins;
    }

    public void addDuelWin() {
        this.duelWins++;
    }

    public int getDuelLosses() {
        return duelLosses;
    }

    public void addDuelLoss() {
        this.duelLosses++;
    }

    public int getDuelElo() {
        return duelElo;
    }

    public void setDuelElo(int elo) {
        this.duelElo = Math.max(0, elo);
    }

    public void addDuelElo(int change) {
        this.duelElo = Math.max(0, this.duelElo + change);
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    public void updateLastPlayed() {
        this.lastPlayed = System.currentTimeMillis();
    }
}
