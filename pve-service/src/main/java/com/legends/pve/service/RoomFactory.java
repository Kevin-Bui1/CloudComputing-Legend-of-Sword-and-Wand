package com.legends.pve.service;

import com.legends.pve.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Component
public class RoomFactory {

    private static final Random RNG = new Random();

    private static final String[] ENEMY_NAMES = {
            "Goblin", "Orc", "Skeleton", "Troll", "Dark Elf",
            "Wraith", "Bandit", "Wolf", "Spider", "Necromancer"
    };

    /**Generates a room for the given floor and party strength */
    public Room generateRoom(int floor, int cumulativeLevel) {
        int battleChance = Math.min(90, 60 + (cumulativeLevel / 10) * 3);
        boolean isBattle = RNG.nextInt(100) < battleChance;

        if (isBattle) {
            return new BattleRoom(floor, generateEnemyParty(cumulativeLevel));
        }
        return new InnRoom(floor);
    }

    /** Generates a random enemy party scaled to the player's strength. */
    private List<Enemy> generateEnemyParty(int cumulativeLevel) {
        int partySize = 1 + RNG.nextInt(5);
        List<Enemy> enemies = new ArrayList<>();
        int avgLevel = Math.max(1, cumulativeLevel / 5);

        for (int i = 0; i < partySize; i++) {
            int level = Math.max(1, Math.min(10, avgLevel + RNG.nextInt(3) - 1));
            String name = ENEMY_NAMES[RNG.nextInt(ENEMY_NAMES.length)] + " " + (i + 1);
            // Scale stats with level
            int attack  = 5 + level * 2;
            int defense = 3 + level;
            int hp      = 40 + level * 15;
            int mana    = 20;
            enemies.add(new Enemy(name, level, attack, defense, hp, mana));
        }
        return enemies;
    }
}
