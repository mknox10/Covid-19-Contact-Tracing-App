package com.covidcontacttracing;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class BeaconService extends Service {

    private static final String SAVE_FILE = "userData";
    private static final String TAG = "BeaconService";

    private String uuid;
    private BeaconTransmitter beaconTransmitter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        uuid = loadUUID();

        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && BeaconTransmitter.checkTransmissionSupported(getApplicationContext()) == BeaconTransmitter.SUPPORTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    transmitBeacon();
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
    public void onDestroy() {
        super.onDestroy();
        beaconTransmitter.stopAdvertising();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Starts sending beacon signals to other devices in the area.
     *
     * @author mknox
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void transmitBeacon() {

        Toast.makeText(this, "Device has started broadcasting to other devices.", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Device ID: " + uuid, Toast.LENGTH_LONG).show();

        Beacon beacon = new Beacon.Builder()
                .setId1(uuid)
                .setId2("2")
                .setId3("3")
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();

        BeaconParser beaconParser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"); // altbeacon format

        // If you are using an emulator the app will crash at this point
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: " + errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }
        });
    }

    /**
     * load the UUID from the file stored on the device.
     *
     * @return UUID
     * @author mknox
     */
    private String loadUUID() {
        File dataFile = new File(this.getFilesDir(), SAVE_FILE);

        FileReader fr;
        BufferedReader br;
        JSONObject json;
        try {
            fr = new FileReader(dataFile.getAbsoluteFile());
            br = new BufferedReader(fr);
            StringBuffer sb = new StringBuffer();

            String curr = "";
            while ((curr = br.readLine()) != null) {
                sb.append(curr + '\n');
            }
            String fileContent = sb.toString();

            br.close();
            fr.close();

            json = new JSONObject(fileContent);

            return json.getString("UUID");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
