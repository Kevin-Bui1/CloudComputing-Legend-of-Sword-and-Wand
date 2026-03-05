package com.los.pvpservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InviteService {
  private final AtomicInteger nextId = new AtomicInteger(1);
  private final Map<Integer, Map<String,Object>> invites = new ConcurrentHashMap<>();

  public int createInvite(int fromUserId, int toUserId) {
    int id = nextId.getAndIncrement();
    invites.put(id, Map.of("inviteId", id, "fromUserId", fromUserId, "toUserId", toUserId, "status", "PENDING"));
    return id;
  }

  public Map<String,Object> getInvite(int id) {
    Map<String,Object> inv = invites.get(id);
    if (inv == null) throw new IllegalArgumentException("Invite not found");
    return inv;
  }

  public void acceptInvite(int id, int toUserId) {
    Map<String,Object> inv = invites.get(id);
    if (inv == null) throw new IllegalArgumentException("Invite not found");
    if (((Number)inv.get("toUserId")).intValue() != toUserId) throw new IllegalArgumentException("Invite user mismatch");
    invites.put(id, Map.of("inviteId", id, "fromUserId", inv.get("fromUserId"), "toUserId", inv.get("toUserId"), "status", "ACCEPTED"));
  }
}
