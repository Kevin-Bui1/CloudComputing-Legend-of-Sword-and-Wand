package com.legends.battle.model;

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
    protected int shield;   

    public Unit(String name, int level, int attack, int defense, int maxHp, int maxMana) {
        this.name     = name;
        this.level    = level;
        this.attack   = attack;
        this.defense  = defense;
        this.hp       = maxHp;
        this.maxHp    = maxHp;
        this.mana     = maxMana;
        this.maxMana  = maxMana;
        this.stunned  = false;
        this.shield   = 0;
    }

    public boolean isAlive() { return hp > 0; }

    public boolean isStunned() { return stunned; }

    public void setStunned(boolean stunned) { this.stunned = stunned; }

    public void takeDamage(int damage) {
        if (damage <= 0) return;
        if (shield > 0) {
            int absorbed = Math.min(shield, damage);
            shield -= absorbed;
            damage -= absorbed;
        }
        hp = Math.max(0, hp - damage);
    }

    public void restoreHp(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public void restoreMana(int amount) {
        mana = Math.min(maxMana, mana + amount);
    }

    public boolean spendMana(int cost) {
        if (mana < cost) return false;
        mana -= cost;
        return true;
    }

    public void addShield(int amount) { shield += amount; }

    public String getName()   { return name; }
    public int getLevel()     { return level; }
    public int getAttack()    { return attack; }
    public int getDefense()   { return defense; }
    public int getHp()        { return hp; }
    public int getMaxHp()     { return maxHp; }
    public int getMana()      { return mana; }
    public int getMaxMana()   { return maxMana; }
    public int getShield()    { return shield; }

    public void setHp(int hp)       { this.hp = Math.max(0, Math.min(maxHp, hp)); }
    public void setMana(int mana)   { this.mana = Math.max(0, Math.min(maxMana, mana)); }
    public void setAttack(int a)    { this.attack = a; }
    public void setDefense(int d)   { this.defense = d; }
}
