package com.los.battleservice.dto;

public record UnitDto(
        String name,
        int level,
        int attack,
        int defense,
        int maxHp,
        int maxMana
) {}
