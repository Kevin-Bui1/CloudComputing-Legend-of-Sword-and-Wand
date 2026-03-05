package com.los.dataservice.dto;

public record HeroDto(
        Integer heroId,
        String heroName,
        Integer heroLevel,
        Integer maxHp,
        Integer attack,
        Integer defense
) {}
