package com.los.battleservice.dto;

import java.util.List;

public record SimulateBattleRequest(
        java.util.List<UnitDto> playerParty,
        java.util.List<UnitDto> enemyParty
) {}
