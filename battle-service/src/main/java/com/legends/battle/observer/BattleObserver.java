package com.legends.battle.observer;
import com.legends.battle.model.Action;
import com.legends.battle.model.Unit;

public interface BattleObserver {
    default void onTurnStart(Unit unit) {}
    default void onAction(Unit actor, Action action) {}
    default void onDamage(Unit attacker, Unit target, int damage) {}
    default void onBattleEnd(String winner) {}
    void onAbilityResult(Unit actor, String result);
}
