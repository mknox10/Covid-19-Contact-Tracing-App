package com.covidcontacttracing;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Iterator;

public class MonitorService extends Service implements BeaconConsumer {

    private static final String TAG = "Monitor Service";
    BeaconManager beaconManager;

    @Override
    public void onCreate() {
        Toast.makeText(this, "Monitor Service Started!", Toast.LENGTH_SHORT).show();
        super.onCreate();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Starts monitoring for beacon signals from other devices. It is called after implementation of the beaconManger.
     *
     * @author mknox
     */
    @Override
    public void onBeaconServiceConnect() {

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Iterator<Beacon> itr = beacons.iterator();
                    while (itr.hasNext()) {
                        Beacon beacon = itr.next();
                        /** check distance between 5.0 meters for testing purposes. **/
                        if (beacon.getDistance() < 5.0) {
                            Log.d(TAG, "Beacon within 5.0 meters");
                        }
                        if (beacon.getDistance() < 2.0) {
                            Log.d(TAG, "Beacon within 2.0 meters");
                            beaconInteraction(beacon);
                        }
                    }
                }
            }
        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {
            //todo: log some type of error message here
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves a new row to the interaction table with the id's of each device and the interaction timestamp.
     *
     * @author ???
     * @param beacon
     */
    private void beaconInteraction(Beacon beacon) {

        // test case - print interaction to screen. Remove this when finished.
        Log.i(TAG, "The beacon " + beacon.toString() + " is about " + beacon.getDistance() + " meters away.");
        Toast.makeText(this, "The beacon " + beacon.toString() + " is about " + beacon.getDistance() + " meters away.", Toast.LENGTH_SHORT).show();

        //todo: save interaction to database

    }

}