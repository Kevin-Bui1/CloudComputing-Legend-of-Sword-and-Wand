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
     * Returns a 409 error if the user already has an active campaign in progress.
     */
    public CampaignResponse startCampaign(Long userId, List<HeroRequest> heroRequests) {
        if (activeCampaigns.containsKey(userId)) {
            return CampaignResponse.error("You already have a campaign in progress. Complete or save-and-exit it before starting a new one.");
        }
        Party party = new Party();
        party.setGold(200); // Starting gold
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
                campaign.getParty().getCumulativeLevel(),
                campaign.getParty().getHeroes().size()
        );

        String roomDescription = room.enter(campaign.getParty());
        String roomType = room instanceof BattleRoom ? "BATTLE" : "INN";

        CampaignResponse response = CampaignResponse.of(campaign, roomDescription, roomType);
        if (room instanceof BattleRoom battleRoom) {
            response.setEnemies(battleRoom.getEnemies());
            response.setExpReward(battleRoom.calculateTotalExp());
            response.setGoldReward(battleRoom.calculateTotalGold());
        } else if (room instanceof InnRoom innRoom) {
            response.setRecruits(innRoom.getRecruits());
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

    /**
     * Handles an inn shop purchase. Deducts gold and applies the item effect.
     */
    public CampaignResponse buyItem(Long userId, String itemName) {
        Campaign campaign = activeCampaigns.get(userId);
        if (campaign == null) return CampaignResponse.error("No active campaign.");

        Party party = campaign.getParty();
        int cost;
        switch (itemName) {
            case "Bread"       -> cost = 200;
            case "Potion"      -> cost = 350;
            case "Elixir"      -> cost = 500;
            case "Power Shard" -> cost = 400;
            case "Iron Shield" -> cost = 400;
            default            -> { return CampaignResponse.error("Unknown item: " + itemName); }
        }

        if (!party.deductGold(cost)) {
            return CampaignResponse.error("Not enough gold. You need " + cost + "g but only have " + party.getGold() + "g.");
        }

        // Apply effect to all living heroes
        for (Hero h : party.getHeroes()) {
            if (h.getHp() <= 0) continue;
            switch (itemName) {
                case "Bread"       -> h.setHp(Math.min(h.getMaxHp(), h.getHp() + 20));
                case "Potion"      -> h.setHp(Math.min(h.getMaxHp(), h.getHp() + 50));
                case "Elixir"      -> { h.setHp(Math.min(h.getMaxHp(), h.getHp() + 80));
                    h.setMana(Math.min(h.getMaxMana(), h.getMana() + 40)); }
                case "Power Shard" -> h.setAttack(h.getAttack() + 10);
                case "Iron Shield" -> h.setDefense(h.getDefense() + 8);
            }
        }
        return CampaignResponse.of(campaign, "Purchased " + itemName + " for " + cost + "g.", null);
    }

    /**
     * Handles recruiting a hero from the inn.
     * Level 1 heroes are free; higher levels cost 200g per level.
     * Max party size is 5.
     */
    public CampaignResponse recruitHero(Long userId, String heroName, String heroClass, int heroLevel) {
        Campaign campaign = activeCampaigns.get(userId);
        if (campaign == null) return CampaignResponse.error("No active campaign.");

        Party party = campaign.getParty();
        if (party.getHeroes().size() >= 5) {
            return CampaignResponse.error("Your party is full (5/5).");
        }

        int cost = heroLevel == 1 ? 0 : heroLevel * 200;
        if (cost > 0 && !party.deductGold(cost)) {
            return CampaignResponse.error("Not enough gold. Recruiting " + heroName + " costs " + cost + "g but you only have " + party.getGold() + "g.");
        }

        Hero hero = new Hero(heroName, heroClass);
        hero.setLevel(heroLevel);
        // Scale stats to match level
        hero.setAttack(5 + (heroLevel - 1) * 2);
        hero.setDefense(5 + (heroLevel - 1));
        hero.setMaxHp(100 + (heroLevel - 1) * 10);
        hero.setHp(hero.getMaxHp());
        hero.setMaxMana(50 + (heroLevel - 1) * 5);
        hero.setMana(hero.getMaxMana());
        party.addHero(hero);

        String msg = cost == 0
                ? heroName + " joined your party for free!"
                : heroName + " joined your party for " + cost + "g.";
        return CampaignResponse.of(campaign, msg, null);
    }

    /** Removes the campaign session from memory. */
    public void endCampaign(Long userId) {
        activeCampaigns.remove(userId);
    }
}