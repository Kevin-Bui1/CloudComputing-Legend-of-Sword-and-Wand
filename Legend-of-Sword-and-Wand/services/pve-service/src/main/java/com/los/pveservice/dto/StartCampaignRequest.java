package com.los.pveservice.dto;

import java.util.List;

public record StartCampaignRequest(Integer userId, String partyName, List<HeroDto> heroes) {}
