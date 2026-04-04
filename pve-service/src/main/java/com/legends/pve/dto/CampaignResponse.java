package com.legends.pve.dto;

import com.legends.pve.model.Campaign;
import com.legends.pve.model.Enemy;
import com.legends.pve.model.InnRoom;
import java.util.List;
import java.util.Map;

/** Full campaign state response returned to the client. */
public class CampaignResponse {
    private boolean success;
    private String message;
    private String roomType;   // "BATTLE", "INN", or null
    private int currentRoom;
    private int gold;
    private int cumulativeLevel;
    private List<HeroRequest> heroes;
    private List<Enemy> enemies;
    private int expReward;
    private int goldReward;
    private List<InnRoom.Recruit> recruits;
    private Map<String, Integer> inventory;

    public static CampaignResponse of(Campaign c, String message, String roomType) {
        CampaignResponse r = new CampaignResponse();
        r.success = true;
        r.message = message;
        r.roomType = roomType;
        r.currentRoom = c.getCurrentRoom();
        r.gold = c.getParty().getGold();
        r.cumulativeLevel = c.getParty().getCumulativeLevel();
        r.inventory = c.getInventory();

        r.heroes = c.getParty().getHeroes().stream().map(h -> {
            HeroRequest hr = new HeroRequest();
            hr.setName(h.getName());
            hr.setHeroClass(h.getHeroClass());
            hr.setLevel(h.getLevel());
            hr.setAttack(h.getAttack());
            hr.setDefense(h.getDefense());
            hr.setHp(h.getHp());
            hr.setMaxHp(h.getMaxHp());
            hr.setMana(h.getMana());
            hr.setMaxMana(h.getMaxMana());
            hr.setExperience(h.getExperienceInCurrentLevel());
            hr.setExpToNextLevel(h.expToNextLevel());
            return hr;
        }).collect(java.util.stream.Collectors.toList());

        return r;
    }

    public static CampaignResponse error(String message) {
        CampaignResponse r = new CampaignResponse();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess()                      { return success; }
    public String getMessage()                      { return message; }
    public String getRoomType()                     { return roomType; }
    public int getCurrentRoom()                     { return currentRoom; }
    public int getGold()                            { return gold; }
    public int getCumulativeLevel()                 { return cumulativeLevel; }
    public List<HeroRequest> getHeroes()            { return heroes; }
    public void setHeroes(List<HeroRequest> h)      { this.heroes = h; }
    public List<Enemy> getEnemies()                 { return enemies; }
    public void setEnemies(List<Enemy> enemies)     { this.enemies = enemies; }
    public int getExpReward()                       { return expReward; }
    public void setExpReward(int expReward)         { this.expReward = expReward; }
    public int getGoldReward()                            { return goldReward; }
    public void setGoldReward(int goldReward)             { this.goldReward = goldReward; }
    public List<InnRoom.Recruit> getRecruits()            { return recruits; }
    public void setRecruits(List<InnRoom.Recruit> r)      { this.recruits = r; }
    public Map<String, Integer> getInventory()              { return inventory; }
    public void setInventory(Map<String, Integer> inv)      { this.inventory = inv; }
}