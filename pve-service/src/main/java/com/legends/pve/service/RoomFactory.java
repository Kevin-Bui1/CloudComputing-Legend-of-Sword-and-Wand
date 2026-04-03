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

    private static final String[] HERO_CLASSES = { "WARRIOR", "MAGE", "ORDER", "CHAOS" };

    private static final String[] RECRUIT_NAMES = {
            "Aldric", "Syra", "Brom", "Lira", "Thane",
            "Zara", "Dusk", "Finn", "Mira", "Cade"
    };

    /** Generates a room for the given floor and party strength */
    public Room generateRoom(int floor, int cumulativeLevel) {
        return generateRoom(floor, cumulativeLevel, 0);
    }

    /** Generates a room, also accounting for current party size to gate recruits. */
    public Room generateRoom(int floor, int cumulativeLevel, int partySize) {
        int battleChance = Math.min(90, 60 + (cumulativeLevel / 10) * 3);
        boolean isBattle = RNG.nextInt(100) < battleChance;

        if (isBattle) {
            return new BattleRoom(floor, generateEnemyParty(cumulativeLevel));
        }

        InnRoom inn = new InnRoom(floor);

        // Recruits only appear in rooms 1–10, and only if the party isn't full
        if (floor <= 10 && partySize < 5) {
            inn.setRecruits(generateRecruits());
        }

        return inn;
    }

    /** Generates a small random pool of recruitable heroes (1–3). */
    private List<InnRoom.Recruit> generateRecruits() {
        int count = 1 + RNG.nextInt(3); // 1 to 3 recruits
        List<InnRoom.Recruit> recruits = new ArrayList<>();
        List<String> usedNames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name;
            do {
                name = RECRUIT_NAMES[RNG.nextInt(RECRUIT_NAMES.length)];
            } while (usedNames.contains(name));
            usedNames.add(name);

            String heroClass = HERO_CLASSES[RNG.nextInt(HERO_CLASSES.length)];
            int level = 1 + RNG.nextInt(4); // level 1–4
            int cost  = level == 1 ? 0 : level * 200;
            recruits.add(new InnRoom.Recruit(name, heroClass, level, cost));
        }
        return recruits;
    }

    /** Generates a random enemy party scaled to the player's strength. */
    private List<Enemy> generateEnemyParty(int cumulativeLevel) {
        int maxSize = Math.min(5, Math.max(1, cumulativeLevel / 2 + 1));
        int partySize = 1 + RNG.nextInt(maxSize);

        List<Enemy> enemies = new ArrayList<>();

        // Target cumulative level between (cumulativeLevel - 10) and cumulativeLevel, min 1
        int targetCumulative = Math.max(1, cumulativeLevel + RNG.nextInt(11) - 10);
        int avgLevel = Math.max(1, Math.min(10, targetCumulative / Math.max(1, partySize)));

        for (int i = 0; i < partySize; i++) {
            int level = Math.max(1, Math.min(10, avgLevel + RNG.nextInt(3) - 1));
            String name = ENEMY_NAMES[RNG.nextInt(ENEMY_NAMES.length)] + " " + (i + 1);
            int attack  = 3 + level * 2;   // slightly lower than hero attack scaling
            int defense = 1 + level;       // reduced so early heroes can actually deal damage
            int hp      = 30 + level * 10; // slightly less HP early on
            int mana    = 20;
            enemies.add(new Enemy(name, level, attack, defense, hp, mana));
        }
        return enemies;
    }
}