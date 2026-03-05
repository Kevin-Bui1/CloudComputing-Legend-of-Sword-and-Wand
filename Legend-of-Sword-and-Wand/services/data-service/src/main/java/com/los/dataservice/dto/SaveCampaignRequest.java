package com.los.dataservice.dto;

import java.util.List;

public record SaveCampaignRequest(
        Integer userId,
        String partyName,
        Integer currentRoom,
        Integer gold,
        List<HeroDto> heroes
) {}
