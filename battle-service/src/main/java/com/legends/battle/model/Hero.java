package com.legends.battle.model;

public class Hero extends Unit {

    private String heroClass;
    private int experience;

    public Hero(String name, int level, int attack, int defense, int maxHp, int maxMana) {
        super(name, level, attack, defense, maxHp, maxMana);
        this.heroClass = "WARRIOR";
        this.experience = 0;
    }

    public String getHeroClass()            { return heroClass; }
    public void setHeroClass(String c)      { this.heroClass = c; }
    public int getExperience()              { return experience; }
    public void addExperience(int xp)       { this.experience += xp; }
}
