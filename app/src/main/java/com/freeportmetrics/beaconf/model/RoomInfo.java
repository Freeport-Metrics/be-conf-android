package com.freeportmetrics.beaconf.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RoomInfo {

    //private HashMap<String,RoomInfoItem> roomInfoMap = new HashMap<String,RoomInfoItem>();

    public RoomInfo(JSONObject configMessage){

    }

    public HashMap deserialize(JSONObject roomStatusMessage) throws JSONException {
        JSONArray rooms = roomStatusMessage.getJSONArray("rooms");
        for (int i = 0 ; i < rooms.length(); i++) {
            JSONObject room = rooms.getJSONObject(i);
            String roomLabel = room.getString("label");
            String roomId = room.getString("room_id");
        }
        return null;
    }
}
