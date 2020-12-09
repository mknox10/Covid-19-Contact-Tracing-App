package com.covidcontacttracing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //save all this to local storage;
    boolean PositiveTest = false;
    boolean wasExposed = false;
    int uuID;
    //string[] contactList;


    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final String SCANNING_BUTTON = "ScanningBttn";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null && savedInstanceState.getString(SCANNING_BUTTON).equals(String.valueOf(R.string.Stop_Scanning))) {
            TextView beaconBttn = findViewById(R.id.StartBeaconBttn);
            beaconBttn.setText(getString(R.string.Stop_Scanning));
        }
        requestPermissions();

        updateState();


       // if(! null){
            //check if uuid is in local storage
            //generate uuid uuid =
       // }
    }

    


    public void updateState(){
        //call database to update info

       // Query query = FirebaseDatabase.getInstance().getRefrence("");




//        if(querycall.my(id) exist ){
//            PositiveTest = true;//call database
//        }else{
//            PositiveTest = false;//call database
//        }

       // wasExposed = false;//call database

//         PositiveTest = false;
//         wasExposed = false;
//         uuID;
//        string[] contactList;

    }




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MonitorService.class));
        stopService(new Intent(this, BeaconService.class));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView beaconBttn = findViewById(R.id.StartBeaconBttn);
        outState.putString(SCANNING_BUTTON, beaconBttn.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getString(SCANNING_BUTTON).equals(String.valueOf(R.string.Stop_Scanning))) {
            TextView beaconBttn = findViewById(R.id.StartBeaconBttn);
            beaconBttn.setText(getString(R.string.Stop_Scanning));
        }
    }

    /**
     * Asks the user to access their location. If they do not accept the app functionality will be limited.
     *
     * @author mknox
     */
    private void requestPermissions() {

        // Ask permission to use location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
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

        TextView beaconBttn = findViewById(R.id.StartBeaconBttn);
        if (beaconBttn.getText().toString().equals(getString(R.string.Start_Scanning))) {
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

        TextView lblExposure = (TextView) findViewById(R.id.exposureText);
        wasExposed = false;

        //need to insert call to the database to check if you have been exposed



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

        TextView lblPositiveTest = (TextView) findViewById(R.id.PositiveResultText);
        PositiveTest = true;

        //need to insert call to the database to send exposure update



        if (!PositiveTest) {
            lblPositiveTest.setText(" Click the Positive test button if you have received a positive test result \nYou have not reported a positive test result");
        } else{
            lblPositiveTest.setText("You have reported a positive result please follow quarantine guidelines");
        }


    }

}
