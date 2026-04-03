package com.legends.battle.service;

import com.legends.battle.model.Action;
import com.legends.battle.model.Enemy;
import com.legends.battle.model.Hero;
import com.legends.battle.model.Unit;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Battle {

    private List<Unit> playerParty;
    private List<Unit> enemyParty;
    private List<String> lastLog = new java.util.ArrayList<>();
    private Queue<Unit> turnQueue  = new LinkedList<>();
    private Queue<Unit> waitQueue  = new LinkedList<>();
    private boolean battleOver = false;

    public void init(List<Unit> playerParty, List<Unit> enemyParty) {
        this.playerParty = new ArrayList<>(playerParty);
        this.enemyParty  = new ArrayList<>(enemyParty);
        this.battleOver  = false;
        initializeTurnOrder();
    }

    public void initializeTurnOrder() {
        turnQueue.clear();
        waitQueue.clear();
        List<Unit> combined = new ArrayList<>();
        combined.addAll(playerParty);
        combined.addAll(enemyParty);
        combined.stream()
                .filter(Unit::isAlive)
                .sorted(Comparator.comparingInt(Unit::getLevel).reversed()
                        .thenComparingInt(Unit::getAttack).reversed())
                .forEach(turnQueue::add);
    }

    public String processTurn(Action action, String ability, int targetIndex) {
        lastLog.clear();
        if (battleOver) return "Battle is already over. Winner: " + getWinner();

        if (turnQueue.isEmpty()) {
            turnQueue.addAll(waitQueue);
            waitQueue.clear();
        }

        Unit current = turnQueue.poll();
        if (current == null) return "No units available.";
        if (!current.isAlive()) return processTurn(action, ability, targetIndex);

        if (current.isStunned()) {
            current.setStunned(false);
            String msg = current.getName() + " is stunned and loses their turn.";
            lastLog.add(msg);
            return msg;
        }

        String result = switch (action) {
            case ATTACK -> handleAttack(current, targetIndex);
            case DEFEND -> handleDefend(current);
            case WAIT   -> handleWait(current);
            case CAST   -> handleCast(current, ability, targetIndex);
        };

        lastLog.add(result);
        isBattleOver();
        return result;
    }

    private String handleAttack(Unit attacker, int targetIndex) {
        List<Unit> targets = getAliveTargetsFor(attacker);
        if (targets.isEmpty()) return attacker.getName() + " has no targets.";
        Unit target = (targetIndex >= 0 && targetIndex < targets.size())
                ? targets.get(targetIndex) : targets.get(0);
        int damage = Math.max(1, attacker.getAttack() - target.getDefense());
        target.takeDamage(damage);
        return String.format("%s attacks %s for %d damage. %s HP: %d/%d",
                attacker.getName(), target.getName(), damage,
                target.getName(), target.getHp(), target.getMaxHp());
    }

    public String handleDefend(Unit unit) {
        unit.restoreHp(10);
        unit.restoreMana(5);
        return String.format("%s defends. HP: %d/%d, Mana: %d/%d",
                unit.getName(), unit.getHp(), unit.getMaxHp(),
                unit.getMana(), unit.getMaxMana());
    }

    public String handleWait(Unit unit) {
        waitQueue.add(unit);
        return unit.getName() + " waits.";
    }

    private String handleCast(Unit unit, String ability, int targetIndex) {
        if (!(unit instanceof Hero hero)) return handleAttack(unit, targetIndex);

        return switch (ability.toUpperCase()) {
            case "HEAL"              -> castHeal(hero);
            case "PROTECT"           -> castProtect(hero);
            case "FIREBALL"          -> castFireball(hero, targetIndex);
            case "CHAIN LIGHTNING",
                 "CHAINLIGHTNING"    -> castChainLightning(hero, targetIndex);
            case "BERSERKER ATTACK",
                 "BERSERKERATTACK"   -> castBerserkerAttack(hero, targetIndex);
            case "REPLENISH"         -> castReplenish(hero);
            default -> switch (hero.getHeroClass().toUpperCase()) {
                case "WARRIOR" -> castBerserkerAttack(hero, targetIndex);
                case "ORDER"   -> castHeal(hero);
                case "CHAOS"   -> castFireball(hero, targetIndex);
                case "MAGE"    -> castReplenish(hero);
                default        -> handleAttack(hero, targetIndex);
            };
        };
    }

    private String castBerserkerAttack(Hero hero) {
        if (!hero.spendMana(60)) return hero.getName() + " has insufficient mana for Berserker Attack.";
        List<Unit> targets = getAliveTargetsFor(hero);
        if (targets.isEmpty()) return hero.getName() + " has no targets.";
        StringBuilder sb = new StringBuilder();
        int primary = Math.max(1, hero.getAttack() - targets.get(0).getDefense());
        targets.get(0).takeDamage(primary);
        sb.append(hero.getName()).append(" Berserker attacks ").append(targets.get(0).getName())
          .append(" for ").append(primary).append(" damage.");
        for (int i = 1; i < Math.min(3, targets.size()); i++) {
            int splash = Math.max(1, primary / 4);
            targets.get(i).takeDamage(splash);
            sb.append(" ").append(targets.get(i).getName()).append(" hit for ").append(splash).append(".");
        }
        return sb.toString();
    }

    private String castHeal(Hero hero) {
        if (!hero.spendMana(35)) return hero.getName() + " has insufficient mana for Heal.";
        List<Unit> allies = getAliveAlliesFor(hero);
        if (allies.isEmpty()) return "No allies to heal.";
        Unit target = allies.stream().min(Comparator.comparingInt(Unit::getHp)).orElse(hero);
        int amount = target.getMaxHp() / 4;
        target.restoreHp(amount);
        return hero.getName() + " heals " + target.getName() + " for " + amount + " HP.";
    }

    private String castFireball(Hero hero, int targetIndex) {
        if (!hero.spendMana(30)) return hero.getName() + " has insufficient mana for Fireball.";
        List<Unit> targets = getAliveTargetsFor(hero);
        if (targets.isEmpty()) return hero.getName() + " has no targets.";
        // Start from chosen target
        if (targetIndex >= 0 && targetIndex < targets.size()) {
            Unit chosen = targets.remove(targetIndex);
            targets.add(0, chosen);
        }
        StringBuilder sb = new StringBuilder(hero.getName() + " casts Fireball:");
        for (int i = 0; i < Math.min(3, targets.size()); i++) {
            int dmg = Math.max(1, hero.getAttack() - targets.get(i).getDefense());
            targets.get(i).takeDamage(dmg);
            sb.append(" ").append(targets.get(i).getName()).append(" -").append(dmg).append("HP;");
        }
        return sb.toString();
    }

    private String castBerserkerAttack(Hero hero, int targetIndex) {
        if (!hero.spendMana(60)) return hero.getName() + " has insufficient mana for Berserker Attack.";
        List<Unit> targets = getAliveTargetsFor(hero);
        if (targets.isEmpty()) return hero.getName() + " has no targets.";
        if (targetIndex >= 0 && targetIndex < targets.size()) {
            Unit chosen = targets.remove(targetIndex);
            targets.add(0, chosen);
        }
        StringBuilder sb = new StringBuilder();
        int primary = Math.max(1, hero.getAttack() - targets.get(0).getDefense());
        targets.get(0).takeDamage(primary);
        sb.append(hero.getName()).append(" Berserker attacks ").append(targets.get(0).getName())
                .append(" for ").append(primary).append(" damage.");
        for (int i = 1; i < Math.min(3, targets.size()); i++) {
            int splash = Math.max(1, primary / 4);
            targets.get(i).takeDamage(splash);
            sb.append(" ").append(targets.get(i).getName()).append(" hit for ").append(splash).append(".");
        }
        return sb.toString();
    }

    private String castReplenish(Hero hero) {
        if (!hero.spendMana(80)) return hero.getName() + " has insufficient mana for Replenish.";
        List<Unit> allies = getAliveAlliesFor(hero);
        allies.forEach(a -> a.restoreMana(30));
        hero.restoreMana(60);
        return hero.getName() + " casts Replenish. All allies +30 mana, self +60 mana.";
    }

    private String castProtect(Hero hero) {
        if (!hero.spendMana(25)) return hero.getName() + " has insufficient mana for Protect.";
        List<Unit> allies = getAliveAlliesFor(hero);
        StringBuilder sb = new StringBuilder(hero.getName() + " casts Protect:");
        for (Unit a : allies) {
            int shield = a.getMaxHp() / 10;
            a.restoreHp(shield);  // using restoreHp to simulate shielding (capped at maxHp)
            sb.append(" ").append(a.getName()).append(" shielded ").append(shield).append("HP;");
        }
        return sb.toString();
    }

    private String castChainLightning(Hero hero, int targetIndex) {
        if (!hero.spendMana(40)) return hero.getName() + " has insufficient mana for Chain Lightning.";
        List<Unit> targets = getAliveTargetsFor(hero);
        if (targets.isEmpty()) return hero.getName() + " has no targets.";

        if (targetIndex >= 0 && targetIndex < targets.size()) {
            Unit chosen = targets.remove(targetIndex);
            targets.add(0, chosen);
        }

        StringBuilder sb = new StringBuilder(hero.getName() + " casts Chain Lightning:");
        int damage = Math.max(1, hero.getAttack() - targets.get(0).getDefense());
        for (Unit t : targets) {
            t.takeDamage(damage);
            sb.append(" ").append(t.getName()).append(" -").append(damage).append("HP;");
            damage = Math.max(1, damage / 4);  // 25% of previous
        }
        return sb.toString();
    }

    private Unit findFirstAliveTarget(Unit attacker) {
        List<Unit> targets = getAliveTargetsFor(attacker);
        return targets.isEmpty() ? null : targets.get(0);
    }

    private List<Unit> getAliveTargetsFor(Unit unit) {
        boolean isPlayer = playerParty.contains(unit);
        return (isPlayer ? enemyParty : playerParty).stream().filter(Unit::isAlive).toList();
    }

    private List<Unit> getAliveAlliesFor(Unit unit) {
        boolean isPlayer = playerParty.contains(unit);
        return (isPlayer ? playerParty : enemyParty).stream().filter(Unit::isAlive).toList();
    }

    public boolean noLivingUnits(List<Unit> party) {
        return party.stream().noneMatch(Unit::isAlive);
    }

    public boolean isBattleOver() {
        battleOver = noLivingUnits(playerParty) || noLivingUnits(enemyParty);
        return battleOver;
    }

    public String getWinner() {
        if (noLivingUnits(enemyParty))  return "Heroes win!";
        if (noLivingUnits(playerParty)) return "Enemies win!";
        return "Battle not over.";
    }

    public List<Unit> getTurnOrder() {
        return new ArrayList<>(turnQueue);
    }

    public List<Unit> getPlayerParty() { return playerParty; }
    public List<Unit> getEnemyParty()  { return enemyParty; }
    public boolean isBattleOverFlag()  { return battleOver; }
    public List<String> getLastLog() { return lastLog; }
}
