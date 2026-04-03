package com.legends.battle;
import com.legends.battle.model.Unit;
public class Enemy extends Unit {
    public Enemy(String name, int level, int attack, int defense, int maxHp, int maxMana) {
        super(name, level, attack, defense, maxHp, maxMana);
    }
}