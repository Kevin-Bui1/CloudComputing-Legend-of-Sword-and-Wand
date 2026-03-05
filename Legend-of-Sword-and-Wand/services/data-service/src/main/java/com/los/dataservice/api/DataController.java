package com.los.dataservice.api;

import com.los.dataservice.dto.*;
import com.los.dataservice.repo.DataRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class DataController {
    private final DataRepository repo;

    public DataController(DataRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "ok"); }


    @PostMapping("/auth/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error","username and password required","code","BAD_REQUEST"));
        }
        return repo.verifyLogin(username, password)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("status","success","userId",u.userId(), "username", u.username())))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error","Invalid credentials","code","AUTH_FAILED")));
    }


    // Users
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        try {
            int id = repo.createUser(req);
            return ResponseEntity.ok(Map.of("status","success","userId", id));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(409).body(Map.of("error","Username already exists","code","USERNAME_TAKEN"));
        }
    }

    @GetMapping("/users/by-username/{username}")
    public ResponseEntity<?> getByUsername(@PathVariable String username) {
        return repo.findUserByUsername(username)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","User not found","code","NOT_FOUND")));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getById(@PathVariable int userId) {
        return repo.findUserById(userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","User not found","code","NOT_FOUND")));
    }

    // PvP results
    @PostMapping("/users/pvp/result")
    public ResponseEntity<?> recordPvpResult(@RequestBody Map<String,Integer> body) {
        Integer winner = body.get("winnerUserId");
        Integer loser = body.get("loserUserId");
        if (winner == null || loser == null) {
            return ResponseEntity.badRequest().body(Map.of("error","winnerUserId and loserUserId required","code","BAD_REQUEST"));
        }
        boolean ok = repo.incrementPvpWinLoss(winner, loser);
        if (!ok) return ResponseEntity.status(404).body(Map.of("error","User not found","code","NOT_FOUND"));
        return ResponseEntity.ok(Map.of("status","RECORDED"));
    }

    // Campaign save/load
    @PostMapping("/campaign/save")
    public ResponseEntity<?> saveCampaign(@RequestBody SaveCampaignRequest req) {
        int partyId = repo.saveCampaign(req);
        return ResponseEntity.ok(Map.of("status","saved","partyId", partyId));
    }

    @GetMapping("/campaign/load/{userId}")
    public ResponseEntity<?> loadCampaign(@PathVariable int userId) {
        return repo.loadActiveCampaign(userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","No active campaign","code","NOT_FOUND")));
    }
}
