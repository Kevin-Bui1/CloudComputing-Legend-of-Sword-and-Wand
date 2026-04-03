package com.legends.pve.service;

import com.legends.pve.dto.*;
import com.legends.pve.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class PveController {

    private final RoomFactory roomFactory;

    /** One Campaign per logged-in user, keyed by userId. */
    private final Map<Long, Campaign> activeCampaigns = new ConcurrentHashMap<>();

    public PveController(RoomFactory roomFactory) {
        this.roomFactory = roomFactory;
    }

    /**
     * Starts a new campaign for the user with the given heroes.
     * Creates fresh Hero objects from the request and puts them in a Party.
     */
    public CampaignResponse startCampaign(Long userId, List<HeroRequest> heroRequests) {
        Party party = new Party();
        for (HeroRequest req : heroRequests) {
            Hero hero = new Hero(req.getName(), req.getHeroClass());
            party.addHero(hero);
        }
        Campaign campaign = new Campaign(party);
        activeCampaigns.put(userId, campaign);
        String message = campaign.start();
        return CampaignResponse.of(campaign, message, null);
    }

    /** Moves the party into the next room and returns what's in it. */
    public CampaignResponse nextRoom(Long userId) {
        Campaign campaign = activeCampaigns.get(userId);
        if (campaign == null) return CampaignResponse.error("No active campaign found.");
        if (campaign.isComplete()) return CampaignResponse.error("Campaign complete! Check your score.");

        campaign.advanceRoom();
        Room room = roomFactory.generateRoom(
                campaign.getCurrentRoom(),
                campaign.getParty().getCumulativeLevel()
        );

        String roomDescription = room.enter(campaign.getParty());
        String roomType = room instanceof BattleRoom ? "BATTLE" : "INN";

        CampaignResponse response = CampaignResponse.of(campaign, roomDescription, roomType);
        if (room instanceof BattleRoom battleRoom) {
            response.setEnemies(battleRoom.getEnemies());
            response.setExpReward(battleRoom.calculateTotalExp());
            response.setGoldReward(battleRoom.calculateTotalGold());
        }
        return response;
    }

    /** Returns the current campaign state without advancing to a new room. */
    public CampaignResponse getCampaign(Long userId) {
        Campaign campaign = activeCampaigns.get(userId);
        if (campaign == null) return CampaignResponse.error("No active campaign.");
        return CampaignResponse.of(campaign, "Campaign info retrieved.", null);
    }

    /** Rebuilds an in-memory Campaign from a saved state loaded from the Data Service. */
    public CampaignResponse restoreCampaign(Long userId, SavedStateRequest savedState) {
        Party party = new Party();
        party.setGold(savedState.getGold());
        for (HeroRequest hr : savedState.getHeroes()) {
            Hero h = new Hero(hr.getName(), hr.getHeroClass());
            h.setLevel(hr.getLevel());
            h.setAttack(hr.getAttack());
            h.setDefense(hr.getDefense());
            h.setHp(hr.getHp());
            h.setMaxHp(hr.getMaxHp());
            h.setMana(hr.getMana());
            h.setMaxMana(hr.getMaxMana());
            party.addHero(h);
        }
        Campaign campaign = new Campaign(party);
        campaign.setCurrentRoom(savedState.getCurrentRoom());
        activeCampaigns.put(userId, campaign);
        return CampaignResponse.of(campaign, "Campaign restored from save.", null);
    }

    /**
     * Calculates the final score at the end of a 30-room campaign.
     *   (sum of hero levels * 100) + (gold * 10)
     */
    public int calculateScore(Long userId) {
        Campaign campaign = activeCampaigns.get(userId);
        if (campaign == null) return 0;
        Party party = campaign.getParty();
        int levelScore = party.getHeroes().stream().mapToInt(h -> h.getLevel() * 100).sum();
        int goldScore  = party.getGold() * 10;
        return levelScore + goldScore;
    }

    /** Removes the campaign session from memory. */
    public void endCampaign(Long userId) {
        activeCampaigns.remove(userId);
    }
}
