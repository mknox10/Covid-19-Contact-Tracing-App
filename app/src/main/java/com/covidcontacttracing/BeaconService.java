package com.covidcontacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {

        // check if device is compatible
        if (BeaconTransmitter.checkTransmissionSupported(getApplicationContext()) != BeaconTransmitter.SUPPORTED) {
            Toast.makeText(this, "Incompatible Device", Toast.LENGTH_SHORT).show();
            return; // maybe throw some type of exception here
        }


        //todo: might need to fix this stuff up, not sure.
        Toast.makeText(this, "Broadcast Service Started!", Toast.LENGTH_SHORT).show();
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // stop the service
                    // will this work? It will just prevent the thread from being created below? - Mitch
                    return;
                }
            }
        } else {
            return;
        }
        transmitBeacon();
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
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
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
