package com.legends.battle.controller;

import com.legends.battle.dto.BattleRequest;
import com.legends.battle.dto.BattleResponse;
import com.legends.battle.model.Action;
import com.legends.battle.service.BattleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @PostMapping("/{battleId}/start")
    public ResponseEntity<BattleResponse> startBattle(
            @PathVariable String battleId,
            @RequestBody BattleRequest request) {
        return ResponseEntity.ok(battleService.startBattle(battleId, request));
    }

    @PostMapping("/{battleId}/action")
    public ResponseEntity<BattleResponse> executeAction(
            @PathVariable String battleId,
            @RequestParam Action action) {
        return ResponseEntity.ok(battleService.executeAction(battleId, action));
    }
}
