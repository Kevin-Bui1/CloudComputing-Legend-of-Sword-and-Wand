package com.los.battleservice.api;

import battle.Battle;
import battle.Enemy;
import battle.Hero;
import battle.Unit;
import com.los.battleservice.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class BattleController {

    @GetMapping("/health")
    public Map<String,String> health() { return Map.of("status","ok"); }

    @PostMapping("/battle/simulate")
    public ResponseEntity<?> simulate(@RequestBody SimulateBattleRequest req) {
        if (req.playerParty() == null || req.enemyParty() == null || req.playerParty().isEmpty() || req.enemyParty().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error","playerParty and enemyParty are required","code","BAD_REQUEST"));
        }

        List<Unit> player = req.playerParty().stream()
                .map(u -> new Hero(u.name(), u.level(), u.attack(), u.defense(), u.maxHp(), u.maxMana()))
                .collect(Collectors.toList());

        List<Unit> enemy = req.enemyParty().stream()
                .map(u -> new Enemy(u.name(), u.level(), u.attack(), u.defense(), u.maxHp(), u.maxMana()))
                .collect(Collectors.toList());

        Battle battle = new Battle(player, enemy);
        battle.startBattle(); // runs until over (uses simple logic)

        String winner = battle.getWinner().name();
        List<Unit> survivors = winner.equals("PLAYER") ? player : enemy;
        List<RemainingUnitDto> remaining = survivors.stream()
                .filter(Unit::isAlive)
                .map(u -> new RemainingUnitDto(u.getName(), u.getHp()))
                .toList();

        return ResponseEntity.ok(new SimulateBattleResponse(winner, remaining));
    }
}
