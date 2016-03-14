package com.freeportmetrics.beaconf;

import java.util.ArrayList;
import java.util.Date;

public class RoomInfo {
    private String id;
    private double roomRadius;
    private ArrayList users;
    private Date lastUpdate;

    public RoomInfo(String id, double roomRadius, ArrayList users, Date lastUpdate) {
        this.id = id;
        this.roomRadius = roomRadius;
        this.users = users;
        this.lastUpdate = lastUpdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getRoomRadius() {
        return roomRadius;
    }

    public void setRoomRadius(double roomRadius) {
        this.roomRadius = roomRadius;
    }

    public ArrayList getUsers() {
        return users;
    }

    public void setUsers(ArrayList users) {
        this.users = users;
    }

    public Date getLastUpdate() { return lastUpdate; }

    public void setLastUpdate(Date lastUpdate) { this.lastUpdate = lastUpdate; }
}
