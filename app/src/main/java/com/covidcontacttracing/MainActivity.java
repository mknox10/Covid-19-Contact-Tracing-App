package com.covidcontacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //todo: we may need to re-ask the user to accept these conditions when they re-launch the app if they declined them at first.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask permission to use location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("This app needs background location access");
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @TargetApi(23)
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                            PERMISSION_REQUEST_BACKGROUND_LOCATION);
                                }

                            });
                            builder.show();
                        }
                        else {
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
                    }
                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
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
        }
    }

    /**
     * Starts the two services needed, the BeaconService for sending beacons and the Monitor service for listening for beacons.
     *
     * @param view
     * @author mknox
     */
    public void startBeacon(View view) {

        /* todo: both services will need to update functionality to allow services to be run once the app is closed. Currently
                 it works while the app is open and if it is in the background but they stop once the app is closed entirely.
            update: it seems the monitor service still works while it is closed. */

        TextView beaconBttn = findViewById(R.id.StartBeaconBttn);
        if (beaconBttn.getText().toString().equals("Start Scanning")) {
            try {
                startService(new Intent(this, MonitorService.class));
                startService(new Intent(this, BeaconService.class));
                beaconBttn.setText(getString(R.string.Stop_Scanning));
            } catch (Exception e) {
                Log.println(Log.ERROR, TAG, "Failed to start Beacons");
            }
        } else {
            stopService(new Intent(this, MonitorService.class));
            stopService(new Intent(this, BeaconService.class));
            beaconBttn.setText(getString(R.string.Start_Scanning));
        }

    }

    /**
     * Queries the database to find positive exposures from the past two days.
     *
     *      Close Contact Definition:
     *
     *          Someone who was within 6 feet of an infected person for a cumulative total of 15 minutes or
     *          more over a 24-hour period* starting from 2 days before illness onset (or, for asymptomatic
     *          patients, 2 days prior to test specimen collection) until the time the patient is isolated.
     *
     *          source: https://www.cdc.gov/coronavirus/2019-ncov/php/contact-tracing/contact-tracing-plan/appendix.html#contact
     *
     * @param view
     * @author ???
     */
    public void CheckExposure(View view) {

        TextView lblExposure = (TextView) findViewById(R.id.lblExposure);
        boolean wasExposed = false;

        if (wasExposed) {
            lblExposure.setText("You have been exposed please check CDC guiedlines on how to quarantine ");
        } else{
            lblExposure.setText("You have not been exposed");
        }

    }

    /**
     * Add a row to the positive result table with the time of first symptoms or positive test date (if asymptomatic).
     *
     *      Close Contact Definition:
     *
     *          Someone who was within 6 feet of an infected person for a cumulative total of 15 minutes or
     *          more over a 24-hour period* starting from 2 days before illness onset (or, for asymptomatic
     *          patients, 2 days prior to test specimen collection) until the time the patient is isolated.
     *
     *          source: https://www.cdc.gov/coronavirus/2019-ncov/php/contact-tracing/contact-tracing-plan/appendix.html#contact
     *
     * @param view
     * @author ???
     */
    public void PositiveResult(View view) {

    }

}
