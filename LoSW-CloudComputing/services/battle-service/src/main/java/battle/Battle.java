package battle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Battle {
    private List<Unit> playerParty;
    private List<Unit> enemyParty;

    private Queue<Unit> turnQueue;
    private Queue<Unit> waitQueue;

    private boolean battleOver;

    public Battle(List<Unit> playerParty, List<Unit> enemyParty) {
        this.playerParty = playerParty;
        this.enemyParty = enemyParty;
        this.battleOver = false;
        this.turnQueue = new LinkedList<>();
        this.waitQueue = new LinkedList<>();
        initializeTurnOrder();
    }

    public void startBattle() {
        initializeTurnOrder();
        while (!isBattleOver()) {
            if (turnQueue.isEmpty()) {
                turnQueue.addAll(waitQueue);
                waitQueue.clear();
            }
            Unit current = turnQueue.poll();
            if (current != null) {
                processTurn(current);
            }
        }
        endBattle();
    }

    private void initializeTurnOrder() {
        waitQueue.clear();
        turnQueue.clear();

        List<Unit> allUnits = new ArrayList<>();

        for (Unit u : playerParty) {
            if (u.isAlive()) allUnits.add(u);
        }

        for (Unit u : enemyParty) {
            if (u.isAlive()) allUnits.add(u);
        }
        allUnits.sort((u1, u2) -> {
            if (u2.getLevel() != u1.getLevel()) {
                return u2.getLevel() - u1.getLevel();
            }
            return u2.getAttack() - u1.getAttack();
        });

        turnQueue.addAll(allUnits);

    }

    public Unit getCurrentUnit() {
        if (turnQueue.isEmpty()) return null;
        return turnQueue.peek();
    }

    public void executeAction(Action action) {
        if (battleOver) return;

        Unit current = turnQueue.poll();
        if (current == null || !current.isAlive()) {
            return;
        }

        switch (action) {
            case ATTACK:
                handleAttack(current);
                break;
            case DEFEND:
                handleDefend(current);
                break;
            case WAIT:
                break;
            default:
                break;
        }

        if (isBattleOver()) {
            battleOver = true;
            return;
        }

        if (current.isAlive()) {
            turnQueue.offer(current);
        }
    }

    private void processTurn(Unit unit) {
        if (!unit.isAlive()) {
            return;
        }
        if (unit.isStunned()) {
            unit.setStunned(false);
            waitQueue.add(unit);
            return;
        }

        // Step 3: Decide action (Deliverable 1: always attack)
        Action action = Action.ATTACK;

        // Step 4 & 5: Execute action
        if (action == Action.ATTACK) {
            Unit target = null;

            // Enemy attacks hero
            if (enemyParty.contains(unit)) {
                for (Unit u : playerParty) {
                    if (u.isAlive()) {
                        target = u;
                        break;
                    }
                }
            }
            // Hero attacks enemy
            else {
                for (Unit u : enemyParty) {
                    if (u.isAlive()) {
                        target = u;
                        break;
                    }
                }
            }

            if (target != null) {
                handleAttack(unit);
            }
        }

        // Step 6: Re-queue unit if still alive
        if (unit.isAlive()) {
            waitQueue.add(unit);
        }
    }

    private void handleAttack(Unit attacker) {
        List<Unit> targetParty = enemyParty.contains(attacker)
                ? playerParty
                : enemyParty;

        Unit target = null;
        for (Unit u : targetParty) {
            if (u.isAlive()) {
                target = u;
                break;
            }
        }

        if (target == null) return;

        int damage = attacker.getAttack() - target.getDefense();
        if (damage < 0) damage = 0;

        target.takeDamage(damage);
    }

    private void handleDefend(Unit unit) {
        unit.restoreHp(10);
        unit.restoreMana(5);
    }

    private void handleWait(Unit unit) {

    }

    public boolean isBattleOver() {
        return noLivingUnits(playerParty) || noLivingUnits(enemyParty);
    }

    private boolean noLivingUnits(List<Unit> party) {
        for (Unit u : party) {
            if (u.isAlive()) return false;
        }
        return true;
    }

    public String getWinner() {
        if (!isBattleOver()) return null;

        if (noLivingUnits(enemyParty)) return "Heroes";
        if (noLivingUnits(playerParty)) return "Enemies";

        return null;
    }

    public List<Unit> getTurnOrder() {
        return new ArrayList<>(turnQueue);
    }

    private void endBattle() {
        boolean heroesAlive = false;

        for (Unit u : playerParty) {
            if (u.isAlive()) {
                heroesAlive = true;
                break;
            }
        }
        if (heroesAlive) {
            System.out.println("Heroes win!");
        } else {
            System.out.println("Enemies win!");
        }
    }
}
