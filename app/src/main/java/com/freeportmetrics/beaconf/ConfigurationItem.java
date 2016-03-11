package com.freeportmetrics.beaconf;

/**
 * Created by marcin on 2016-03-09.
 */
public class ConfigurationItem {
    private String bid;
    private float roomRadius;

    public ConfigurationItem(String bid, float roomRadius) {
        this.bid = bid;
        this.roomRadius = roomRadius;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public float getRoomRadius() {
        return roomRadius;
    }

    public void setRoomRadius(float roomRadius) {
        this.roomRadius = roomRadius;
    }
}
