package com.legends.pve.model;


public class Campaign {

    private Party party;
    private int currentRoom = 0;
    private static final int MAX_ROOMS = 30;

    public Campaign(Party party) {
        this.party = party;
    }

    /** Returns the campaign start message listing all party members. */
    public String start() {
        return "Campaign started! Your party: " + party.getHeroes().stream()
                .map(h -> h.getName() + " (Lvl " + h.getLevel() + ")")
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
    }

    public Party getParty() { return party; }

    public int getCurrentRoom()             { return currentRoom; }
    public void setCurrentRoom(int room)    { this.currentRoom = room; }
    public void advanceRoom()               { currentRoom++; }
    public boolean isComplete()             { return currentRoom >= MAX_ROOMS; }
    public int getMaxRooms()                { return MAX_ROOMS; }
}
