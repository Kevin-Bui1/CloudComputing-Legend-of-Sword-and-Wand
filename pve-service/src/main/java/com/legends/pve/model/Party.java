package com.legends.pve.model;

import java.util.ArrayList;
import java.util.List;


public class Party {

    private List<Hero> heroes = new ArrayList<>();
    private int gold          = 0;

    public Party() {}

    /** Adds a hero to the party. The 5-hero max is enforced by the caller. */
    public void addHero(Hero hero) { heroes.add(hero); }

    public List<Hero> getHeroes() { return heroes; }

    /**
     * Sums up all hero levels.
     */
    public int getCumulativeLevel() {
        return heroes.stream().mapToInt(Hero::getLevel).sum();
    }

    /** Returns true if at least one hero is still standing (hp > 0). */
    public boolean hasLivingHeroes() {
        return heroes.stream().anyMatch(h -> h.getHp() > 0);
    }

    public int getGold()            { return gold; }
    public void setGold(int gold)   { this.gold = gold; }
    public void addGold(int amount) { this.gold += amount; }

    /** Returns false if the player doesn't have enough gold */
    public boolean deductGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }
}
