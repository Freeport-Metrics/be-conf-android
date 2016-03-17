package com.freeportmetrics.beaconf;

import android.app.Application;
import android.os.RemoteException;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class BeconfApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeconfApplication";
    private RegionBootstrap regionBootstrap;

    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "App started up");
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager = BeaconManager.getInstanceForApplication(getBaseContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setBackgroundScanPeriod(1000l);
        beaconManager.setBackgroundBetweenScanPeriod(1000l);
        beaconManager.setForegroundBetweenScanPeriod(1000l);
        beaconManager.setForegroundScanPeriod(1000l);

        Region icy = new Region("com.freeportmetrics.icy", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), Identifier.fromInt(45287), Identifier.fromInt(53858));
        Region blueBerry = new Region("com.freeportmetrics.blueBerry", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), Identifier.fromInt(10344), Identifier.fromInt(31183));
        Region mint = new Region("com.freeportmetrics.mint", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), Identifier.fromInt(5919), Identifier.fromInt(60231));

        List<Region> regions = new ArrayList<Region>();
        regions.add(icy);
        regions.add(blueBerry);
        regions.add(mint);

        regionBootstrap = new RegionBootstrap(this, regions);

        try {
            IO.Options opts = new IO.Options();
            opts.reconnectionAttempts = 3;
            opts.reconnectionDelay = 3000;
            opts.forceNew = true;
            mSocket = IO.socket("http://beatconf-freeportmetrics.rhcloud.com", opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
    }

    @Override
    public void didEnterRegion(Region arg0) {
        String userId = Utils.getDefaults(Utils.USER_ID_PREF_KEY, this);
        String regionId = arg0.getId2()+"_"+arg0.getId3();
        Log.i(TAG, "Got a didEnterRegion call for regionId: " + regionId);
        emitEnterRoomEvent(regionId);
    }

    @Override
    public void didExitRegion(Region arg0) {
        String userId = Utils.getDefaults(Utils.USER_ID_PREF_KEY, this);
        String regionId = arg0.getId2()+"_"+arg0.getId3();
        Log.i(TAG, "Got a didExitRegion call for regionId: "+regionId);
        emitLeaveRoomEvent(regionId);
    }

    public void emitEnterRoomEvent(String roomId){
        String userId = Utils.getDefaults(Utils.USER_ID_PREF_KEY, this);
        Log.i(TAG, "Sending enter event to server, userId: " + userId + ", roomId: " + roomId);
        //debug("Sending enter event to server, userId: " + userId + ", roomId: " + roomId);
        mSocket.emit("enterRoom", "{\"user_id\":\"" + userId + "\",\"room_id\":\"" + roomId + "\"}");
    }

    public void emitLeaveRoomEvent(String roomId){
        String userId = Utils.getDefaults(Utils.USER_ID_PREF_KEY, this);
        Log.i(TAG, "Sending leave event to server, userId: " + userId + ", roomId: " + roomId);
        //debug("Sending leave event to server, userId: " + userId + ", roomId: " + roomId);
        mSocket.emit("leaveRoom", "{\"user_id\":\"" + userId + "\",\"room_id\":\"" + roomId + "\"}");
    }
}