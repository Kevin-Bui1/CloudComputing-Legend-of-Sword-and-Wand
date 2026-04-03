package com.legends.battle.strategy.impl;
import com.legends.battle.Battle;
import com.legends.battle.model.Unit;
import com.legends.battle.strategy.BattleActionStrategy;

public class AttackStrategy implements BattleActionStrategy {
    @Override public void execute(Battle battle, Unit actor) {
        battle.doAttack(actor);
    }
}