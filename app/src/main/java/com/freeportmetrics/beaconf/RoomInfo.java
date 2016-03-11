package com.freeportmetrics.beaconf;

import java.util.ArrayList;

/**
 * Created by marcin on 3/10/2016.
 */
public class RoomInfo {
    private String id;
    private double roomRadius;
    private ArrayList users;

    public RoomInfo(String id, double roomRadius, ArrayList users) {
        this.id = id;
        this.roomRadius = roomRadius;
        this.users = users;
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
}
