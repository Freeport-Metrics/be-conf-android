package com.freeportmetrics.beaconf.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.freeportmetrics.beaconf.model.RoomInfoItem;
import com.freeportmetrics.beaconf.Utils;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BeaconMonitoringService extends Service implements BeaconConsumer
{
    // TODO: use synchronized HashMap
    // TODO: userId from prefs
    protected static final String TAG = "BeaconMonitoringService";
    protected static final String userId = "Marcin";
    private BeaconManager beaconManager;
    private Socket mSocket;
    private HashMap<String,RoomInfoItem> roomInfoMap = new HashMap<String,RoomInfoItem>();

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Setting up beacon manager
        beaconManager = BeaconManager.getInstanceForApplication(getBaseContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        beaconManager.setForegroundBetweenScanPeriod(1000);
        beaconManager.setBackgroundBetweenScanPeriod(1000);

        // AltBeacon library uses a running average of the RSSI to calculate the distance.
        // The sample expiration time is by default 20 seconds.
        // This would explain the gradually updating distance.
        // Decreasing sample expiration time to get faster responses
        RangedBeacon.setSampleExpirationMilliseconds(5000);

        // Setting up socket.io
        try {
            IO.Options opts = new IO.Options();
            opts.reconnectionAttempts = 3;
            opts.reconnectionDelay = 3000;
            opts.forceNew = true;
            mSocket = IO.socket("http://beatconf-freeportmetrics.rhcloud.com", opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //mSocket.connect();
        //mSocket.on("config", onConfigMessage);
        //mSocket.off("room_status", onRoomStatusMessage);
        //mSocket.on("reconnect_failed", onErrorMessage);

        // Handling of case when client left beacons area before beacon scan
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                for (Map.Entry<String, RoomInfoItem> entry : roomInfoMap.entrySet()) {
                    String roomId = entry.getKey();
                    RoomInfoItem roomInfoItem = entry.getValue();
                    Date expirationDate = Utils.addSecondsToDate(20, roomInfoItem.getLastUpdate());
                    Date dateNow = new Date();
                    if (dateNow.after(expirationDate) && roomInfoItem.getUsers().contains(userId)) {
                       // emitLeaveRoomEvent(roomId);
                    }
                }
                handler.postDelayed(this, 15000);
            }
        }, 15000);

        System.out.println("Beacon service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG,"onStartCommand called");
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        beaconManager.unbind(this);
        mSocket.disconnect();
        mSocket.off("config", onConfigMessage);
        mSocket.off("config", onRoomStatusMessage);
        mSocket.off("reconnect_failed", onErrorMessage);
        Log.i(TAG, "Service destroyed ...");
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                for (Beacon beacon : beacons) {
                    String roomId = beacon.getId2() + "_" + beacon.getId3();
                    double distance = beacon.getDistance();
                    System.out.print("roomId: " + roomId + ", distance: " + distance);
                    RoomInfoItem roomInfoItem = roomInfoMap.get(roomId);
                    if (roomInfoItem != null) {
                        // check if user entered the room
                        if (distance < roomInfoItem.getRoomRadius() && !roomInfoItem.getUsers().contains(userId)) {
                            emitEnterRoomEvent(roomId);
                        } else if (distance > roomInfoItem.getRoomRadius() && roomInfoItem.getUsers().contains(userId)) { // check if user left the room
                            emitLeaveRoomEvent(roomId);
                        }
                        roomInfoItem.setLastUpdate(new Date());
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {  e.printStackTrace(); }
    }

    //////////////////////
    // SOCKET.IO EVENTS //
    //////////////////////
    // TODO: deserialize JSON message in object class
    private Emitter.Listener onConfigMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            roomInfoMap.clear();
            try {
                JSONArray config = data.getJSONArray("config");
                for (int i = 0; i < config.length(); i++) {
                    JSONObject configItem = config.getJSONObject(i);
                    String roomId = configItem.getString("b_id");
                    double roomRadius = configItem.getDouble("room_radius");
                    RoomInfoItem roomInfoItem = new RoomInfoItem(roomId, roomRadius, new ArrayList(), new Date());
                    roomInfoMap.put(configItem.getString("b_id"), roomInfoItem);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    };

    private Emitter.Listener onRoomStatusMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            //Log.i(TAG, "Room status message: " + args[0]);
            //debug("Room status message: "+args[0]);
            JSONObject data = (JSONObject) args[0];
            //refreshRoomState(data);
        }
    };

    private Emitter.Listener onErrorMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            System.out.println("### onErrorMessage");
        }
    };

    // events sent to server
    public void emitEnterRoomEvent(String roomId){
        Log.i(TAG, "Sending enter event to server, userId: " + userId + ", roomId: " + roomId);
        //debug("Sending enter event to server, userId: " + userId + ", roomId: " + roomId);
        mSocket.emit("enterRoom", "{\"user_id\":\"" + userId + "\",\"room_id\":\"" + roomId + "\"}");
    }

    public void emitLeaveRoomEvent(String roomId){
        Log.i(TAG, "Sending leave event to server, userId: " + userId + ", roomId: " + roomId);
        //debug("Sending leave event to server, userId: " + userId + ", roomId: " + roomId);
        mSocket.emit("leaveRoom", "{\"user_id\":\"" + userId + "\",\"room_id\":\"" + roomId + "\"}");
    }
}