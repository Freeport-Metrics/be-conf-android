package com.freeportmetrics.beaconf.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RoomStatus {
    private String label;
    private ArrayList<String> users;

    public RoomStatus(String label, ArrayList<String> users) {
        this.label = label;
        this.users = users;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public static ArrayList<RoomStatus> deserialize(JSONObject roomStatusMessage) throws JSONException {
        ArrayList<RoomStatus> roomStatuses = new ArrayList<RoomStatus>();
        JSONArray rooms = roomStatusMessage.getJSONArray("rooms");
        for (int i = 0 ; i < rooms.length(); i++) {
            JSONObject room = rooms.getJSONObject(i);
            String label = room.getString("label");
            JSONArray usersData = room.getJSONArray("users");
            ArrayList<String> users = new ArrayList<String>();
            for (int j = 0; j < usersData.length(); j++) {
                JSONObject user = usersData.getJSONObject(j);
                try {
                    String userName = user.getString("name");
                    users.add(userName);
                } catch(JSONException e){
                    continue;
                }
            }
            RoomStatus roomStatus = new RoomStatus(label, users);
                roomStatuses.add(roomStatus);
        }
        return roomStatuses;
    }
}
