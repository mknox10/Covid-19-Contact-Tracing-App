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
import android.os.Debug;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import java.util.UUID;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final String SCANNING_BUTTON = "ScanningBttn";
    private static final String TAG = "MainActivity";
    private static final String SAVE_FILE = "userData";
    private final String FIREBASE_URL = "https://covid-contact-tracing-69663-default-rtdb.firebaseio.com/";


    // Setup for data file
    File dataFile = new File(this.getFilesDir(), SAVE_FILE);
    JSONObject json = new JSONObject();
    FileReader fr = null;
    FileWriter fw = null;
    BufferedReader br = null;
    BufferedWriter bw = null;

    //save all this to local storage;
    boolean PositiveTest = false;
    boolean wasExposed = false;
    String uuID;
    ArrayList<String> contactList = new ArrayList<String>();


    String testID = "e015bbee-f604-460e-b2df-6449d0d1fc05";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null && savedInstanceState.getString(SCANNING_BUTTON).equals(String.valueOf(R.string.Stop_Scanning))) {
            TextView beaconBttn = findViewById(R.id.StartBeaconBttn);
            beaconBttn.setText(getString(R.string.Stop_Scanning));
        }
        requestPermissions();

        if(!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                fw = new FileWriter(dataFile.getAbsoluteFile());
                bw = new BufferedWriter(fw);

                uuID = UUID.randomUUID().toString();

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

        // Allows us to make HTTP Requests
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Log.println(Log.INFO, "TEST-ID", testID);
        updateState();
    }

    


    public void updateState(){
        //call database to update info

       // CHECK database against interaction list




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
        try {
            wasExposed = checkContact(testID);
        }catch(IOException e){
            Log.println(Log.ERROR, TAG, e.getMessage());
        }catch(JSONException e){
            Log.println(Log.ERROR, TAG, "Invalid JSON.");
        }
        if (wasExposed) {
            lblExposure.setText("You have been exposed please check CDC guidelines on how to quarantine ");
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
        try {
            addPositiveCase(uuID);
        } catch (IOException e) {
            Log.println(Log.ERROR, TAG, "Couldn't add it.");
        }


        if (!PositiveTest) {
            lblPositiveTest.setText(" Click the Positive test button if you have received a positive test result \nYou have not reported a positive test result");
        } else{
            lblPositiveTest.setText("You have reported a positive result please follow quarantine guidelines");
        }


    }

    public static String makeRequestGET(String urlToRead) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public static boolean makeRequestPATCH(String urlToRead, String data) throws IOException {
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = data.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        int code = conn.getResponseCode();
        return code == 200;
    }

    public static JSONObject convertString(String data) throws JSONException {
        return new JSONObject(data);
    }

    public boolean checkContact(String id) throws IOException, JSONException {
        String url = FIREBASE_URL + "positive_cases.json?orderBy=\"$key\"&equalTo=\""+id+'"';
        String response = makeRequestGET(FIREBASE_URL + "positive_cases.json?orderBy=\"$key\"&equalTo=\""+id+'"');
        Log.println(Log.ERROR, "OUTPUT", url+"\n"+response);
        JSONObject object = convertString(response);
        return object.length() != 0;
    }

    public boolean addPositiveCase(String id) throws IOException {
        //"{\"id\": \""+id+"\"}"
        boolean response = makeRequestPATCH(FIREBASE_URL+"positive_cases.json", "{\""+id+"\": true}");
        if(response){
            Log.println(Log.ERROR, "OUTPUT", "Added positive case successfully.");
        }
        return response;
    }
}
