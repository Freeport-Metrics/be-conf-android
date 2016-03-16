package com.freeportmetrics.beaconf;

import android.app.Application;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class BeconfApplication extends Application implements BootstrapNotifier {
    private static final String TAG = ".MyApplicationName";
    private RegionBootstrap regionBootstrap;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "App started up");
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager = BeaconManager.getInstanceForApplication(getBaseContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // TODO: remaining regions
        Region blueberry = new Region("com.freeportmetrics.blueberry", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), Identifier.fromInt(10344), Identifier.fromInt(31183));
        regionBootstrap = new RegionBootstrap(this, blueberry);
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
    }

    @Override
    public void didEnterRegion(Region arg0) {
        String regionId = arg0.getId2()+"_"+arg0.getId3();
        Log.i(TAG, "Got a didEnterRegion call for regionId: "+regionId);
    }

    @Override
    public void didExitRegion(Region arg0) {
        String regionId = arg0.getId2()+"_"+arg0.getId3();
        Log.i(TAG, "Got a didEnterRegion call for regionId: "+regionId);
    }
}