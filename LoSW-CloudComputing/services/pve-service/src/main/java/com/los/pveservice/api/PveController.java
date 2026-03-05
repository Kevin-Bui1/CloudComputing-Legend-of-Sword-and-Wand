package com.los.pveservice.api;

import com.los.pveservice.dto.*;
import com.los.pveservice.service.CampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@RestController
public class PveController {
  private final CampaignService campaigns;

  public PveController(CampaignService campaigns) { this.campaigns = campaigns; }

  @GetMapping("/health")
  public Map<String,String> health() { return Map.of("status","ok"); }

  @PostMapping("/campaign/start")
  public ResponseEntity<?> start(@RequestBody StartCampaignRequest req) {
    if (req.userId() == null || req.partyName() == null || req.heroes() == null || req.heroes().isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error","userId, partyName, heroes required","code","BAD_REQUEST"));
    }
    return ResponseEntity.ok(campaigns.start(req));
  }

  @PostMapping("/campaign/nextRoom")
  public ResponseEntity<?> nextRoom(@RequestBody Map<String,Integer> body) {
    Integer userId = body.get("userId");
    if (userId == null) return ResponseEntity.badRequest().body(Map.of("error","userId required","code","BAD_REQUEST"));
    try {
      return ResponseEntity.ok(campaigns.nextRoom(userId));
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
    }
  }

  @PostMapping("/campaign/save")
  public ResponseEntity<?> save(@RequestBody Map<String,Integer> body) {
    Integer userId = body.get("userId");
    if (userId == null) return ResponseEntity.badRequest().body(Map.of("error","userId required","code","BAD_REQUEST"));
    return ResponseEntity.ok(campaigns.save(userId));
  }

  @GetMapping("/campaign/load")
  public ResponseEntity<?> load(@RequestParam int userId) {
    try {
      return ResponseEntity.ok(campaigns.load(userId));
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
    }
  }
}
