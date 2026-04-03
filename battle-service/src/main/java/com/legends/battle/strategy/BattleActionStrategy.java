package com.legends.battle.strategy;
import com.legends.battle.Battle;
import com.legends.battle.model.Unit;

public interface BattleActionStrategy { void execute(Battle battle, Unit actor); }