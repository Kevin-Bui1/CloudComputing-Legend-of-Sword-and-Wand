package com.legends.battle.model;

import java.util.List;

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

    public String castAbility(List<Unit> allies, List<Unit> enemies) {
        switch (heroClass) {

            // ORDER: Protect (shield 10% HP to all) costs 25 mana
            //        Heal (heal lowest HP ally 25% of maxHp) costs 35 mana
            case "ORDER" -> {
                if (!spendMana(35)) return getName() + " has insufficient mana!";
                Unit lowest = null;
                for (Unit a : allies) {
                    if (a.isAlive() && (lowest == null || a.getHp() < lowest.getHp()))
                        lowest = a;
                }
                if (lowest != null) {
                    int heal = lowest.getMaxHp() / 4;
                    lowest.restoreHp(heal);
                    return getName() + " heals " + lowest.getName() + " for " + heal + " HP!";
                }
                return getName() + " found no ally to heal.";
            }

            // CHAOS: Fireball hits up to 3 enemies, costs 30 mana
            case "CHAOS" -> {
                if (!spendMana(30)) return getName() + " has insufficient mana!";
                int dmg = Math.max(1, getAttack());
                int count = 0;
                StringBuilder sb = new StringBuilder(getName() + " launches a Fireball! ");
                for (Unit e : enemies) {
                    if (e.isAlive() && count < 3) {
                        e.takeDamage(dmg);
                        sb.append(e.getName()).append(" takes ").append(dmg).append(" dmg. ");
                        count++;
                    }
                }
                return sb.toString();
            }

            // WARRIOR: Berserker Attack - damages 2 more units for 25% of original damage, costs 60 mana
            case "WARRIOR" -> {
                if (!spendMana(60)) return getName() + " has insufficient mana!";
                Unit primary = null;
                for (Unit e : enemies) {
                    if (e.isAlive()) { primary = e; break; }
                }
                if (primary == null) return getName() + " found no target!";
                int dmg = Math.max(1, getAttack() - primary.getDefense());
                primary.takeDamage(dmg);
                int splash = Math.max(1, dmg / 4);
                int splashCount = 0;
                StringBuilder sb = new StringBuilder(getName() + " uses Berserker Attack on "
                        + primary.getName() + " for " + dmg + " dmg! ");
                for (Unit e : enemies) {
                    if (e.isAlive() && e != primary && splashCount < 2) {
                        e.takeDamage(splash);
                        sb.append(e.getName()).append(" takes ").append(splash).append(" splash dmg. ");
                        splashCount++;
                    }
                }
                return sb.toString();
            }

            // MAGE: Replenish - 30 mana to all allies, 60 to self, costs 80 mana
            case "MAGE" -> {
                if (!spendMana(80)) return getName() + " has insufficient mana!";
                for (Unit a : allies) {
                    if (a.isAlive()) a.restoreMana(30);
                }
                restoreMana(60); // extra 60 to self (total 90)
                return getName() + " casts Replenish! All allies gain 30 mana, self gains 90 mana.";
            }

            default -> {
                return getName() + " has no special ability.";
            }
        }
    }
}