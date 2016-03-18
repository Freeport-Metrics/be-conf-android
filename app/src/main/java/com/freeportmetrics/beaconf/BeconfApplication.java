package com.freeportmetrics.beaconf;

import android.app.Application;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.GsonBuilder;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeconfApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeconfApplication";
    private RegionBootstrap regionBootstrap;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "App started up");
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setBackgroundScanPeriod(5000l);
        beaconManager.setBackgroundBetweenScanPeriod(10000l);
        beaconManager.setForegroundScanPeriod(5000l);
        beaconManager.setForegroundBetweenScanPeriod(10000l);
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Region icy = new Region("com.freeportmetrics.icy", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), Identifier.fromInt(45287), Identifier.fromInt(53858));
        Region blueBerry = new Region("com.freeportmetrics.blueBerry", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), Identifier.fromInt(10344), Identifier.fromInt(31183));
        Region mint = new Region("com.freeportmetrics.mint", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), Identifier.fromInt(5919), Identifier.fromInt(60231));

        List<Region> regions = new ArrayList<Region>();
        regions.add(icy);
        regions.add(blueBerry);
        regions.add(mint);

        regionBootstrap = new RegionBootstrap(this, regions);
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
    }

    @Override
    public void didEnterRegion(Region arg0) {
        String regionId = arg0.getId2()+"_"+arg0.getId3();
        Log.i(TAG, "Got a didEnterRegion call for regionId: " + regionId);
        Utils.sendRoomStatusMessage(true, prepareRoomStatusMessage(regionId));
    }

    @Override
    public void didExitRegion(Region arg0) {
        String regionId = arg0.getId2()+"_"+arg0.getId3();
        Log.i(TAG, "Got a didExitRegion call for regionId: " + regionId);
        Utils.sendRoomStatusMessage(false, prepareRoomStatusMessage(regionId));
    }

    private String prepareRoomStatusMessage(String regionId){
        String userName = Utils.getDefaults(Utils.USER_NAME_PREF_KEY, this);
        String userId = Utils.getDefaults(Utils.USER_ID_PREF_KEY, this);
        Map<String, String> comment = new HashMap<String, String>();
        comment.put("id", userId);
        comment.put("user_id", userName);
        comment.put("room_id", regionId);
        return new GsonBuilder().create().toJson(comment, Map.class);
    }
}