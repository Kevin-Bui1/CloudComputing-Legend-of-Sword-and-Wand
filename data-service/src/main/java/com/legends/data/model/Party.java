package com.legends.data.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parties")
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partyId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 80)
    private String partyName;

    private int currentRoom = 0;
    private int gold        = 0;
    private boolean activeCampaign = true;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeroEntity> heroes = new ArrayList<>();

    public Party() {}

    public Long getPartyId()                    { return partyId; }
    public Long getUserId()                     { return userId; }
    public void setUserId(Long id)              { this.userId = id; }
    public String getPartyName()                { return partyName; }
    public void setPartyName(String n)          { this.partyName = n; }
    public int getCurrentRoom()                 { return currentRoom; }
    public void setCurrentRoom(int r)           { this.currentRoom = r; }
    public int getGold()                        { return gold; }
    public void setGold(int gold)               { this.gold = gold; }
    public boolean isActiveCampaign()           { return activeCampaign; }
    public void setActiveCampaign(boolean a)    { this.activeCampaign = a; }
    public List<HeroEntity> getHeroes()         { return heroes; }
    public void setHeroes(List<HeroEntity> h)   { this.heroes = h; }
}
