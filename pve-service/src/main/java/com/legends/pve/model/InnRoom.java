package com.legends.pve.model;

import java.util.Collections;
import java.util.List;

public class InnRoom extends Room {

    private List<Recruit> recruits = Collections.emptyList();

    public InnRoom(int floor) { super(floor); }

    public List<Recruit> getRecruits()              { return recruits; }
    public void setRecruits(List<Recruit> recruits) { this.recruits = recruits; }

    /**
     * Restores all heroes to full HP and mana.
     * Returns a summary of how much each hero recovered.
     */
    @Override
    public String enter(Party party) {
        StringBuilder sb = new StringBuilder("Welcome to the Inn! ");
        for (Hero h : party.getHeroes()) {
            int hpRestored   = h.getMaxHp()  - h.getHp();
            int manaRestored = h.getMaxMana() - h.getMana();
            h.setHp(h.getMaxHp());
            h.setMana(h.getMaxMana());
            sb.append(h.getName())
                    .append(" restored ").append(hpRestored).append(" HP and ")
                    .append(manaRestored).append(" mana. ");
        }
        return sb.toString().trim();
    }

    /** Represents a hero available for recruitment at this inn. */
    public static class Recruit {
        private String name;
        private String heroClass;
        private int level;
        private int cost;

        public Recruit(String name, String heroClass, int level, int cost) {
            this.name      = name;
            this.heroClass = heroClass;
            this.level     = level;
            this.cost      = cost;
        }

        public String getName()      { return name; }
        public String getHeroClass() { return heroClass; }
        public int getLevel()        { return level; }
        public int getCost()         { return cost; }
    }
}