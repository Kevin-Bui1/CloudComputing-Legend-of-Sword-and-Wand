package com.los.battleservice.dto;

import java.util.List;

public record SimulateBattleResponse(String winner, List<RemainingUnitDto> remainingUnits) {}
