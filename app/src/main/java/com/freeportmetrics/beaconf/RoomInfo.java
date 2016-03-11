package com.freeportmetrics.beaconf;

/**
 * Created by marcin on 3/10/2016.
 */
public class RoomInfo {
    private String id;
    private boolean occupied;
    private double roomRadius;

    public RoomInfo(String id, boolean occupied, double roomRadius) {
        this.id = id;
        this.occupied = occupied;
        this.roomRadius = roomRadius;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public double getRoomRadius() {
        return roomRadius;
    }

    public void setRoomRadius(double roomRadius) {
        this.roomRadius = roomRadius;
    }
}
