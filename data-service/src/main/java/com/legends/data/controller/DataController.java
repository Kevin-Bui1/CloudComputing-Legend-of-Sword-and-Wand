package com.legends.data.controller;

import com.legends.data.dto.CampaignState;
import com.legends.data.dto.SaveRequest;
import com.legends.data.model.Party;
import com.legends.data.model.Score;
import com.legends.data.repository.ScoreRepository;
import com.legends.data.service.GameSaveDAO;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final GameSaveDAO gameSaveDAO;
    private final ScoreRepository scoreRepository;

    public DataController(GameSaveDAO gameSaveDAO, ScoreRepository scoreRepository) {
        this.gameSaveDAO = gameSaveDAO;
        this.scoreRepository = scoreRepository;
    }

    /** Save or update a campaign's progress. Enforces 5-party limit. */
    @PostMapping("/campaign/save")
    public ResponseEntity<?> saveCampaign(@RequestBody SaveRequest request) {
        int count = gameSaveDAO.countSavedParties(request.getUserId());
        if (count >= 5) {
            return ResponseEntity.status(409).body(
                    Map.of("error", "Party limit reached", "count", count)
            );
        }
        return ResponseEntity.ok(gameSaveDAO.saveCampaignProgress(request));
    }

    /** Load the active campaign state for a user. */
    @GetMapping("/campaign/{userId}")
    public ResponseEntity<CampaignState> loadCampaign(@PathVariable Long userId) {
        return gameSaveDAO.fetchSavedCampaign(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Mark a campaign as completed. */
    @PatchMapping("/campaign/{userId}/complete")
    public ResponseEntity<Void> completeCampaign(@PathVariable Long userId) {
        gameSaveDAO.completeCampaign(userId);
        return ResponseEntity.ok().build();
    }

    /** Delete a saved party. */
    @DeleteMapping("/party/{partyId}")
    public ResponseEntity<Void> deleteParty(@PathVariable Long partyId) {
        gameSaveDAO.deleteParty(partyId);
        return ResponseEntity.ok().build();
    }

    /** List all saved parties for a user. */
    @GetMapping("/parties/{userId}")
    public ResponseEntity<List<Party>> getSavedParties(@PathVariable Long userId) {
        return ResponseEntity.ok(gameSaveDAO.getSavedParties(userId));
    }

    /** Save a score for a user after completing a campaign. */
    @PostMapping("/scores/{userId}")
    public ResponseEntity<Void> saveScore(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        Score score = new Score();
        score.setUserId(userId);
        score.setUsername(body.get("username") != null ? body.get("username").toString() : "unknown");
        score.setScore((Integer) body.get("score"));
        scoreRepository.save(score);
        return ResponseEntity.ok().build();
    }

    /** Get the best score for a user. */
    @GetMapping("/scores/{userId}/best")
    public ResponseEntity<Map<String, Integer>> getBestScore(@PathVariable Long userId) {
        int best = scoreRepository.findBestScoreByUserId(userId).orElse(0);
        return ResponseEntity.ok(Map.of("bestScore", best));
    }

    /** Get the top N scores across all users. */
    @GetMapping("/scores/top")
    public ResponseEntity<List<Map<String, Object>>> getTopScores(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> results = scoreRepository
                .findTopScores(PageRequest.of(0, limit))
                .stream()
                .map(s -> Map.<String, Object>of(
                        "username", s.getUsername(),
                        "score", s.getScore()))
                .toList();
        return ResponseEntity.ok(results);
    }
}