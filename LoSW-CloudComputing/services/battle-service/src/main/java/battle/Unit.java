package battle;

public abstract class Unit {
    protected String name;
    protected int level;
    protected int attack;
    protected int defense;
    protected int hp;
    protected int maxHp;
    protected int mana;
    protected int maxMana;
    protected boolean stunned;

    public Unit(String name, int level, int attack, int defense, int maxHp, int maxMana) {
        this.name = name;
        this.level = level;
        this.attack = attack;
        this.defense = defense;
        this.maxHp = maxHp;
        this.maxMana = maxMana;
        this.hp = maxHp;
        this.mana = maxMana;
        this.stunned = false;
    }

    public boolean isAlive() {
        return hp > 0;
    }
    public boolean isStunned() {
        return stunned;
    }
    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }
    public void takeDamage(int damage) {
        if (damage < 0) {
            damage = 0;
        }
        hp -= damage;
        if (hp < 0) {
            hp = 0;
        }
    }
    public void restoreHp(int amount) {
        if (amount < 0) return;
        hp += amount;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }
    public void restoreMana(int amount) {
        if (amount < 0) return;
        mana += amount;
        if (mana > maxMana) {
            mana = maxMana;
        }
    }

    public String getName() {
        return name;
    }
    public int getLevel() {
        return level;
    }
    public int getAttack() {
        return attack;
    }
    public int getDefense() {
        return defense;
    }
    public int getHp() {
        return hp;
    }
    public int getMana() {
        return mana;
    }
}

