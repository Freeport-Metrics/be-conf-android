package com.freeportmetrics.beaconf;

import android.content.Context;
import android.content.Intent;
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
    }

    ///////////////////////////////////////
    // updating UI based on JSON message //
    ///////////////////////////////////////
    private void refreshUI(JSONObject roomStatusMessage){
        linearLayout.removeAllViews();
        try {
            JSONArray rooms = roomStatusMessage.getJSONArray("rooms");
            for (int i = 0 ; i < rooms.length(); i++) {
                JSONObject room = rooms.getJSONObject(i);
                String roomLabel = room.getString("label");
                StringBuilder sb = new StringBuilder();
                JSONArray users = room.getJSONArray("users");
                for (int j = 0 ; j < users.length(); j++) {
                    String user = users.getString(j);
                    sb.append(user);
                    if (j!=users.length()-1) sb.append(", ");
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
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon beacon: beacons){
                    String roomId = beacon.getId2()+"_"+beacon.getId3();
                    double distance = beacon.getDistance();
                    RoomInfo roomInfo = roomInfoMap.get(roomId);
                    if (roomInfo != null){
                        // check if user entered the room
                        if (distance < roomInfo.getRoomRadius() && !roomInfo.isOccupied()) {
                            emitEnterRoomEvent(roomId);
                            roomInfo.setOccupied(true);
                        } else{ // check if user left the room
                            emitLeaveRoomEvent(roomId);
                            roomInfo.setOccupied(false);
                        }
                    }
                }
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
                    System.out.println(args[0]);
                    JSONObject data = (JSONObject) args[0];

                    String beaconId;
                    String roomRadius;
                    Log.i(TAG, data.toString());
                    try {
                        beaconId = data.getString("b_id");
                        roomRadius = data.getString("room_radius");
                        Log.i(TAG, "### received config from server");
                        Log.i(TAG, "beaconId: "+beaconId);
                        Log.i(TAG, "roomRadius: "+roomRadius);

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
                    System.out.println(args[0]);
                    JSONObject data = (JSONObject) args[0];
                    refreshUI(data);
                }
            });
        }
    };

    // emitted events
    public void emitEnterRoomEvent(String roomId){
        Log.i(TAG, "Sending event to server, userId: " + userId + ", roomId: " + roomId);
        mSocket.emit("enterRoom", "{\"user_id\":\""+userId+"\",\"room_id\":\""+roomId+"\"}");
    }

    public void emitLeaveRoomEvent(String roomId){
        Log.i(TAG, "Sending event to server, userId: " + userId + ", roomId: " + roomId);
        mSocket.emit("leaveRoom", "{\"user_id\":\"" + userId + "\",\"room_id\":\"" + roomId + "\"}");
    }

    // debug
    public void emitEnterRoomEventAction(View view){
        emitEnterRoomEvent("5919_60231");
    }
    public void emitLeaveRoomEventAction(View view){
        emitLeaveRoomEvent("5919_60231");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        shutdownSocket();
    }
}
