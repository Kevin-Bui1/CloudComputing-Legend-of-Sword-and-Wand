package com.legends.pve.model;


public abstract class Room {

    protected int floor;

    public Room(int floor) { this.floor = floor; }

    public abstract String enter(Party party);

    public int getFloor() { return floor; }
}
