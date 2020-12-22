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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

                    region = new Region("uniqueId", null, null, null);
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

        Toast.makeText(this, "Device has started monitoring for other devices.", Toast.LENGTH_SHORT).show();

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Iterator<Beacon> itr = beacons.iterator();
                    while (itr.hasNext()) {
                        Beacon beacon = itr.next();
                        if (beacon.getDistance() < 2.0) {
                            Log.d(TAG, "Beacon within 2.0 meters");
                            beaconInteraction(beacon.getId1().toString());
                        }
                    }
                }
            }
        };
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves a new row to the interaction table with the id's of each device and the interaction timestamp.
     *
     * @param interactionID the device id received from the in range beacon
     * @author mknx
     */
    private void beaconInteraction(String interactionID) {

        String message = String.format("Saving interaction with device id: %s", interactionID);

        Log.i(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        JSONObject json;
        String uuID = "";
        boolean PositiveTest = false;
        boolean wasExposed = false;
        ArrayList<String> contactList = new ArrayList<String>();

        File dataFile = new File(this.getFilesDir(), getString(R.string.SAVE_FILE));

        /** Read from the file. **/
        try {
            FileReader fr = new FileReader(dataFile.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);
            StringBuffer sb = new StringBuffer();

            String curr = "";
            while ((curr = br.readLine()) != null) {
                sb.append(curr + '\n');
            }
            String fileContent = sb.toString();

            br.close();
            fr.close();

            json = new JSONObject(fileContent);

            uuID = json.getString("UUID");
            PositiveTest = json.getBoolean("positive");
            wasExposed = json.getBoolean("exposed");

            JSONArray jArray = json.getJSONArray("contacts");
            for (int i = 0; i < jArray.length(); i++) {
                contactList.add(jArray.getString(i));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        /** Add this interaction to file **/
        contactList.add(interactionID);

        /** Save to file. **/
        try {
            dataFile.createNewFile();
            FileWriter fw = new FileWriter(dataFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            json = new JSONObject();

            json.put("UUID", uuID);
            json.put("positive", PositiveTest);
            json.put("exposed", wasExposed);
            json.put("contacts", new JSONArray(contactList));

            bw.write(json.toString());
            bw.close();
            fw.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}