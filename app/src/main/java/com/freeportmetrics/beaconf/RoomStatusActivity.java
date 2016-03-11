package com.freeportmetrics.beaconf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomStatusActivity extends AppCompatActivity implements BeaconConsumer {

    protected static final String TAG = "RoomStatusActivity";
    private BeaconManager beaconManager;
    private HashMap<String,RoomInfo> roomInfoMap = new HashMap<String,RoomInfo>();
    private String userId;
    private LinearLayout linearLayout;
    private LinearLayout debugLayout;
    private TextView debugTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSocket();

        setContentView(R.layout.activity_room_status);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        userId = SharedPreferencesHelper.getDefaults(SharedPreferencesHelper.USER_ID_PREF_KEY, this);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        linearLayout = (LinearLayout) findViewById(R.id.locations_table);

        // DEBUG
        debugLayout = (LinearLayout) findViewById(R.id.debug_view);
        debugTextView = new TextView(this);
        debugTextView.setTextAppearance(this, android.R.style.TextAppearance_Small);
        debugLayout.addView(debugTextView);
    }

    ///////////////////////////////////////
    // updating data based on JSON message //
    ///////////////////////////////////////
    private void refreshRoomState(JSONObject roomStatusMessage){
        linearLayout.removeAllViews();

        try {
            JSONArray rooms = roomStatusMessage.getJSONArray("rooms");
            for (int i = 0 ; i < rooms.length(); i++) {
                JSONObject room = rooms.getJSONObject(i);
                String roomLabel = room.getString("label");
                String roomId = room.getString("room_id");

                StringBuilder sb = new StringBuilder();
                JSONArray users = room.getJSONArray("users");
                RoomInfo roomInfo = roomInfoMap.get(roomId);
                roomInfo.getUsers().clear();
                for (int j = 0 ; j < users.length(); j++) {
                    JSONObject user = users.getJSONObject(j);
                    String userName = user.getString("name");
                    sb.append(userName);
                    if (j!=users.length()-1) sb.append(", ");

                    // updating room information
                    if (roomInfo != null) {
                        roomInfo.getUsers().add(userName);
                    }
                }
                if (users.length()==0) sb.append("-");
                linearLayout.addView(createTextView(roomLabel, sb.toString()));
                linearLayout.addView(createSeparatorView());
            }
            TextView updatedTextView = new TextView(this);
            updatedTextView.setText("updated: " + new Date());
            updatedTextView.setTextAppearance(this, android.R.style.TextAppearance_Small);
            linearLayout.addView(updatedTextView);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    private TextView createTextView(String location, String people){
        TextView textView = new TextView(this);
        textView.setText(location + ": " + people);
        textView.setPadding(5, 5, 5, 5);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        return textView;
    }

    private View createSeparatorView(){
        View separatorView = new View(this);
        separatorView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        separatorView.setBackgroundColor(Color.rgb(51, 51, 51));
        return separatorView;
    }

    /////////////////////
    // beacon handling //
    /////////////////////
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String debug = "";

                        for (Beacon beacon : beacons) {
                            String roomId = beacon.getId2() + "_" + beacon.getId3();
                            double distance = beacon.getDistance();
                            //if (debugTextView.getText().length() > 500) {
                            //    debugTextView.setText("");
                            //}
                            //debugTextView.setText(debugTextView.getText() + "DEBUG: roomId: " + roomId + ", distance: " + distance + '\n');

                            RoomInfo roomInfo = roomInfoMap.get(roomId);
                            if (roomInfo != null) {
                                // check if user entered the room
                                if (distance < roomInfo.getRoomRadius() && !roomInfo.getUsers().contains(userId)) {
                                    emitEnterRoomEvent(roomId);
                                } else if (distance > roomInfo.getRoomRadius() && roomInfo.getUsers().contains(userId)) { // check if user left the room
                                    emitLeaveRoomEvent(roomId);
                                }
                            }
                        }
                    }
                });
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {  e.printStackTrace(); }
    }

    /////////////////////////////
    // Socket.io communication //
    /////////////////////////////
    private void setupSocket(){
        mSocket.connect();
        mSocket.on("config", onConfigMessage);
        mSocket.on("room_status", onRoomStatusMessage);
    }

    private void shutdownSocket(){
        mSocket.disconnect();
        mSocket.off("config", onConfigMessage);
        mSocket.off("room_status", onRoomStatusMessage);
    }

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://beatconf-freeportmetrics.rhcloud.com");
        } catch (URISyntaxException e) { e.printStackTrace(); }
    }

    // received events
    private Emitter.Listener onConfigMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    System.out.println("Config message: "+args[0]);
                    roomInfoMap.clear();
                    try {
                        JSONArray config = data.getJSONArray("config");
                        for (int i = 0; i < config.length(); i++) {
                            JSONObject configItem = config.getJSONObject(i);
                            String roomId = configItem.getString("b_id");
                            double roomRadius = configItem.getDouble("room_radius");
                            RoomInfo roomInfo = new RoomInfo(roomId, roomRadius, new ArrayList());
                            roomInfoMap.put(configItem.getString("b_id"), roomInfo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onRoomStatusMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Room status message: "+args[0]);
                    debug("Room status message: "+args[0]);
                    JSONObject data = (JSONObject) args[0];
                    refreshRoomState(data);
                }
            });
        }
    };

    // emitted events
    public void emitEnterRoomEvent(String roomId){
        Log.i(TAG, "Sending enter event to server, userId: " + userId + ", roomId: " + roomId);
        debug("Sending enter event to server, userId: " + userId + ", roomId: " + roomId);
        mSocket.emit("enterRoom", "{\"user_id\":\"" + userId + "\",\"room_id\":\"" + roomId + "\"}");
    }

    public void emitLeaveRoomEvent(String roomId){
        Log.i(TAG, "Sending leave event to server, userId: " + userId + ", roomId: " + roomId);
        debug("Sending leave event to server, userId: " + userId + ", roomId: " + roomId);
        mSocket.emit("leaveRoom", "{\"user_id\":\"" + userId + "\",\"room_id\":\"" + roomId + "\"}");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        shutdownSocket();
    }

    ///////////
    // DEBUG //
    ///////////
    public void debug(String text){
        if (debugTextView.getText().length() > 500) {
            debugTextView.setText("");
        }
        debugTextView.setText(debugTextView.getText() + text + "\n");
    }
}
