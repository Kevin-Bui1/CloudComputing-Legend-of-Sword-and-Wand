package com.los.pvpservice.api;

import com.los.pvpservice.dto.*;
import com.los.pvpservice.service.InviteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
public class PvpController {
  private final RestTemplate http;
  private final InviteService invites;
  private final String dataBaseUrl;

  public PvpController(RestTemplate http, InviteService invites, @Value("${services.dataBaseUrl}") String dataBaseUrl) {
    this.http = http;
    this.invites = invites;
    this.dataBaseUrl = dataBaseUrl;
  }

  @GetMapping("/health")
  public Map<String,String> health() { return Map.of("status","ok"); }

  @PostMapping("/pvp/invite")
  public ResponseEntity<?> invite(@RequestBody InviteRequest req) {
    if (req.fromUserId() == null || req.toUsername() == null) {
      return ResponseEntity.badRequest().body(Map.of("error","fromUserId and toUsername required","code","BAD_REQUEST"));
    }
    try {
      Map toUser = http.getForObject(dataBaseUrl + "/users/by-username/" + req.toUsername(), Map.class);
      int toUserId = ((Number) toUser.get("userId")).intValue();

      // both players must have at least one active campaign/party (basic check using load endpoint)
      http.getForObject(dataBaseUrl + "/campaign/load/" + req.fromUserId(), Map.class);
      http.getForObject(dataBaseUrl + "/campaign/load/" + toUserId, Map.class);

      int inviteId = invites.createInvite(req.fromUserId(), toUserId);
      return ResponseEntity.ok(Map.of("inviteId", inviteId, "status", "PENDING"));
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
    }
  }

  @PostMapping("/pvp/accept")
  public ResponseEntity<?> accept(@RequestBody AcceptRequest req) {
    if (req.inviteId() == null || req.toUserId() == null) {
      return ResponseEntity.badRequest().body(Map.of("error","inviteId and toUserId required","code","BAD_REQUEST"));
    }
    try {
      invites.acceptInvite(req.inviteId(), req.toUserId());
      return ResponseEntity.ok(Map.of("status","ACCEPTED"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(Map.of("error", e.getMessage(), "code", "NOT_FOUND"));
    }
  }

  @PostMapping("/pvp/result")
  public ResponseEntity<?> result(@RequestBody ResultRequest req) {
    if (req.winnerUserId() == null || req.loserUserId() == null) {
      return ResponseEntity.badRequest().body(Map.of("error","winnerUserId and loserUserId required","code","BAD_REQUEST"));
    }
    try {
      http.postForObject(dataBaseUrl + "/users/pvp/result", Map.of(
          "winnerUserId", req.winnerUserId(),
          "loserUserId", req.loserUserId()
      ), Map.class);
      return ResponseEntity.ok(Map.of("status","RECORDED"));
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
    }
  }
}
