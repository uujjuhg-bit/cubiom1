package com.cubiom.gamemodes.sg;

import java.util.*;

public class SGParty {

    private final UUID leader;
    private final Set<UUID> members;
    private final long createdAt;

    public SGParty(UUID leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        this.members.add(leader);
        this.createdAt = System.currentTimeMillis();
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public boolean addMember(UUID player) {
        if (members.size() >= 4) {
            return false;
        }
        return members.add(player);
    }

    public void removeMember(UUID player) {
        members.remove(player);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }

    public int getSize() {
        return members.size();
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
