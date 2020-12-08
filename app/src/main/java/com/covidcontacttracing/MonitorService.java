package com.covidcontacttracing;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Iterator;

public class MonitorService extends Service implements BeaconConsumer {

    private static final String TAG = "Monitor Service";
    BeaconManager beaconManager;
    Region region;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();

        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && BeaconTransmitter.checkTransmissionSupported(getApplicationContext()) == BeaconTransmitter.SUPPORTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    region = new Region("myRangingUniqueId", null, null, null);
                    beaconManager = BeaconManager.getInstanceForApplication(this);
                    beaconManager.bind(this);

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
            } else {
                Toast.makeText(this, "Incompatible Device", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Failed to Start Beacon Service: Incompatible Device");
            }
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Functionality limited");
            builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                }

            });
            builder.show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            beaconManager.stopMonitoringBeaconsInRegion(region);
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
            beaconManager.startRangingBeaconsInRegion(region);
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