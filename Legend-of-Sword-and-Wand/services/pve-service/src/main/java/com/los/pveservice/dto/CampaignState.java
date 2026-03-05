package com.los.pveservice.dto;

import java.util.List;

public record CampaignState(Integer userId, String partyName, int currentRoom, int gold, List<HeroDto> heroes) {}
