package com.legends.pvp.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PvpService {

    private final AtomicInteger inviteCounter = new AtomicInteger(1);

    private final Map<Integer, int[]> leagueTable = new ConcurrentHashMap<>();
    // int[] = [wins, losses]

    private final Map<Integer, Invite> invites = new ConcurrentHashMap<>();

    private static class Invite {
        int fromUserId;
        String toUsername;
        String status;
        Invite(int from, String to) { this.fromUserId = from; this.toUsername = to; this.status = "PENDING"; }
    }

    public int createInvite(int fromUserId, String toUsername) {
        int inviteId = inviteCounter.getAndIncrement();
        invites.put(inviteId, new Invite(fromUserId, toUsername));
        return inviteId;
    }

    public boolean acceptInvite(int inviteId, int toUserId) {
        Invite invite = invites.get(inviteId);
        if (invite == null || !invite.status.equals("PENDING")) return false;
        invite.status = "ACCEPTED";
        return true;
    }

    public Map<String, Object> recordResult(int winnerUserId, int loserUserId) {
        leagueTable.computeIfAbsent(winnerUserId, k -> new int[]{0, 0})[0]++;
        leagueTable.computeIfAbsent(loserUserId,  k -> new int[]{0, 0})[1]++;
        return Map.of(
                "status", "RECORDED",
                "winnerUserId", winnerUserId,
                "loserUserId", loserUserId
        );
    }

    public Map<Integer, int[]> getLeagueTable() {
        return Collections.unmodifiableMap(leagueTable);
    }
}