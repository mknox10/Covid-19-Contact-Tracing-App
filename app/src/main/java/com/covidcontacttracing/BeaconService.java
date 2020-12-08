package com.covidcontacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import java.util.Arrays;

public class BeaconService extends Service {

    private static final String TAG = "BeaconService";

    BeaconTransmitter beaconTransmitter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        // this message is really only needed for testing purposes
        Toast.makeText(this, "Broadcast Service Started!", Toast.LENGTH_SHORT).show();
        super.onCreate();

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
        Toast.makeText(this, "Broadcast Service Destroyed.", Toast.LENGTH_LONG).show();
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void createNotificationChannel() {
//        CharSequence name = "chName";
//        String description = "chDescription";
//        int importance = NotificationManager.IMPORTANCE_DEFAULT;
//        NotificationChannel channel = new NotificationChannel("chMe", name, importance);
//        channel.setDescription(description);
//        // Register the channel with the system; you can't change the importance
//        // or other notification behaviors after this
//        NotificationManager notificationManager = getSystemService(NotificationManager.class);
//        notificationManager.createNotificationChannel(channel);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        /*Bundle extras = intent.getExtras();
//        Intent data = (Intent) extras.get("data");
//        displaySize = (Point) extras.get("size");
//        dpi = extras.getInt("dpi");
//        imageReader = ImageReader.newInstance(displaySize.x, displaySize.y, PixelFormat.RGBA_8888, 2);
//        mediaProjection = mediaManager.getMediaProjection(-1, data);
//        virtualDisplay = mediaProjection.createVirtualDisplay("service", displaySize.x,
//                displaySize.y, dpi,
//                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(),
//                null, null);
//        serverThread.start();*/
////        beaconThread.start();
//        return START_STICKY;
//    }

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

        //todo: rework this so it can transmit in the background. https://altbeacon.github.io/android-beacon-library/beacon-transmitter.html

        Beacon beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6") // Not a clue what this means - https://altbeacon.github.io/android-beacon-library/beacon-transmitter.html
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"); // altbeacon format

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
}
