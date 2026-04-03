package com.legends.pve.dto;

import java.util.List;

public class BattleResolveRequest {

    /** levels of each enemy that was in the fight, used to compute exp/gold server-side */
    private List<Integer> enemyLevels;

    private List<HeroSnapshot> heroSnapshots;

    private boolean playerWon;

    // nested snapshot DTOs
    public static class HeroSnapshot {
        private String name;
        private int hp;
        private int mana;

        public String getName()          { return name; }
        public void setName(String n)    { this.name = n; }
        public int getHp()               { return hp; }
        public void setHp(int hp)        { this.hp = hp; }
        public int getMana()             { return mana; }
        public void setMana(int mana)    { this.mana = mana; }
    }

    public List<Integer> getEnemyLevels()                      { return enemyLevels; }
    public void setEnemyLevels(List<Integer> enemyLevels)      { this.enemyLevels = enemyLevels; }
    public List<HeroSnapshot> getHeroSnapshots()               { return heroSnapshots; }
    public void setHeroSnapshots(List<HeroSnapshot> s)         { this.heroSnapshots = s; }
    public boolean isPlayerWon()                               { return playerWon; }
    public void setPlayerWon(boolean playerWon)                { this.playerWon = playerWon; }
}