package com.freeportmetrics.beaconf.model;

import java.util.ArrayList;
import java.util.Date;

public class RoomInfoItem {
    private String id;
    private ArrayList<String> users;
    private Date lastUpdate;
    private double radius;

    public RoomInfoItem(String id, ArrayList<String> users, Date lastUpdate, double radius) {
        this.id = id;
        this.users = users;
        this.lastUpdate = lastUpdate;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}