package com.legends.battle;

import com.legends.battle.model.*;
import com.legends.battle.service.Battle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BattleServiceTests {

    private Battle battle;
    private Hero   hero;
    private Enemy  enemy;

    @BeforeEach
    void setUp() {
        hero  = new Hero("Arthur", 3, 15, 5, 100, 80);
        enemy = new Enemy("Goblin", 1, 8, 3, 50, 20);
        battle = new Battle();
        battle.init(List.of(hero), List.of(enemy));
    }

    // BS-TC-01
    @Test
    void testAttack_reducesEnemyHp() {
        int hpBefore = enemy.getHp();
        battle.processTurn(Action.ATTACK);
        assertTrue(enemy.getHp() < hpBefore, "Enemy HP should decrease after being attacked");
    }

    // BS-TC-02
    @Test
    void testAttack_hpNeverBelowZero() {
        Hero strongHero = new Hero("Titan", 10, 999, 5, 200, 100);
        Enemy weakEnemy = new Enemy("Rat", 1, 2, 0, 5, 0);
        Battle b = new Battle();
        b.init(List.of(strongHero), List.of(weakEnemy));
        b.processTurn(Action.ATTACK);
        assertEquals(0, weakEnemy.getHp(), "HP should not go below 0");
    }

    // BS-TC-03
    @Test
    void testDefend_restoresHpAndMana() {
        hero.setHp(50);
        hero.setMana(10);
        battle.handleDefend(hero);
        assertEquals(60, hero.getHp(),   "Defend should restore +10 HP");
        assertEquals(15, hero.getMana(), "Defend should restore +5 mana");
    }

    // BS-TC-04
    @Test
    void testWait_placesUnitInWaitQueue() {
        String result = battle.handleWait(hero);
        assertTrue(result.contains("waits"), "WAIT action message should say 'waits'");
    }

    // BS-TC-05
    @Test
    void testBattleOver_whenEnemyDefeated() {
        enemy.takeDamage(999);
        assertTrue(battle.isBattleOver(), "Battle should be over when enemy has 0 HP");
        assertEquals("Heroes win!", battle.getWinner());
    }

    // BS-TC-06
    @Test
    void testBattleOver_whenHeroDefeated() {
        hero.takeDamage(999);
        assertTrue(battle.isBattleOver(), "Battle should be over when hero has 0 HP");
        assertEquals("Enemies win!", battle.getWinner());
    }

    // BS-TC-07
    @Test
    void testNoLivingUnits_false_whenPartyAlive() {
        assertFalse(battle.noLivingUnits(List.of(hero)),
                "noLivingUnits should return false when hero is alive");
    }

    // BS-TC-08
    @Test
    void testDamageFormula_correctCalculation() {
        int hpBefore = enemy.getHp();
        battle.processTurn(Action.ATTACK);
        int expectedDamage = Math.max(1, hero.getAttack() - enemy.getDefense());
        assertEquals(hpBefore - expectedDamage, enemy.getHp(),
                "Damage should equal attacker.attack - defender.defense");
    }

    // BS-TC-09
    @Test
    void testTurnOrder_highLevelFirst() {
        Hero lowLevel  = new Hero("Novice", 1, 5, 5, 100, 50);
        Hero highLevel = new Hero("Master", 9, 20, 8, 200, 80);
        Battle b = new Battle();
        b.init(List.of(lowLevel, highLevel), List.of(new Enemy("e", 1, 5, 5, 50, 0)));
        List<com.legends.battle.model.Unit> order = b.getTurnOrder();
        assertEquals("Master", order.get(0).getName(),
                "Highest level unit should act first");
    }

    // BS-TC-10
    @Test
    void testStun_skipsUnitTurn() {
        hero.setStunned(true);
        String result = battle.processTurn(Action.ATTACK);
        assertTrue(result.contains("stunned"), "Stunned unit message should mention stun");
        assertFalse(hero.isStunned(), "Stun flag should be cleared after skipped turn");
    }

    // BS-TC-11
    @Test
    void testShield_absorbesDamageFirst() {
        hero.addShield(20);
        hero.takeDamage(15);
        assertEquals(100, hero.getHp(), "Shield should absorb damage before HP");
        assertEquals(5,   hero.getShield(), "Remaining shield should be 5");
    }

    // BS-TC-12
    @Test
    void testCast_insufficientMana_blockedAction() {
        hero.setMana(0);
        String result = battle.processTurn(Action.CAST);
        assertTrue(result.contains("insufficient mana"),
                "CAST should fail when unit has no mana");
    }
}
