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
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    //todo: could store these in the strings.xml file
    private final String FIREBASE_URL = "https://covid-contact-tracing-69663-default-rtdb.firebaseio.com/";
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final String SAVE_FILE = "userData";
    private static final String SCANNING_BUTTON = "ScanningBttn";
    private static final String TAG = "MainActivity";

    private File dataFile;

    // the following fields will be saved to the locally stored data file.
    private String uuID;
    private boolean positiveTest;
    private boolean wasExposed;
    private ArrayList<String> contactList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //todo: we will need to implement something to remove positive tests after two days

        dataFile = new File(this.getFilesDir(), SAVE_FILE);

        uuID = "";
        positiveTest = false;
        wasExposed = false;
        contactList = new ArrayList<String>();

        /** Allows us to make HTTP Requests. **/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        loadData();
        requestPermissions();

        /** Determines if whether the beacons are on. **/
        if (savedInstanceState != null && savedInstanceState.getString(SCANNING_BUTTON).equals(String.valueOf(R.string.Stop_Scanning))) {
            TextView beaconBttn = findViewById(R.id.StartBeaconBttn);
            beaconBttn.setText(getString(R.string.Stop_Scanning));
        }

        togglePositiveResult();
        toggleWasExposed();
    }

    /**
     * Retrieves data from saved file.
     *
     * @author Spencer F
     */
    public void loadData(){
        FileReader fr;
        FileWriter fw;
        BufferedReader br;
        BufferedWriter bw;
        JSONObject json;

        if(!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                fw = new FileWriter(dataFile.getAbsoluteFile());
                bw = new BufferedWriter(fw);
                json = new JSONObject();

                uuID = UUID.randomUUID().toString();

                json.put("UUID", uuID);
                json.put("positive", positiveTest);
                json.put("exposed", wasExposed);
                json.put("contacts", new JSONArray(contactList));

                bw.write(json.toString());
                bw.close();
                fw.close();

                saveData();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        } else {
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

                uuID = json.getString("UUID");
                positiveTest = json.getBoolean("positive");
                wasExposed = json.getBoolean("exposed");

                ArrayList<String> temp = new ArrayList<String>();
                JSONArray jArray = json.getJSONArray("contacts");
                for (int i = 0; i < jArray.length(); i++){
                    temp.add(jArray.getString(i));
                    Log.i(TAG, String.format("Recent contact with device ID: %s loaded successfully.", jArray.getString(i)));
                }
                contactList = temp;

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Will save any data changes to a file stored locally.
     *
     * @author mknox
     */
    private void saveData() {

        FileWriter fw;
        BufferedWriter bw;
        JSONObject json;

        try {
            // just overrides the file
            dataFile.createNewFile();
            fw = new FileWriter(dataFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            json = new JSONObject();

            json.put("UUID", uuID);
            json.put("positive", positiveTest);
            json.put("exposed", wasExposed);
            json.put("contacts", new JSONArray(contactList));

            bw.write(json.toString());
            bw.close();
            fw.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
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
     * @author Brett J
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void checkExposure(View view) {

        loadData();

        //need to insert call to the database to check if you have been exposed
        try {
            wasExposed = checkContact();
        } catch(JSONException e){
            Log.println(Log.ERROR, TAG, "Invalid JSON.");
        }
        toggleWasExposed();
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
     * @author Josh R
     */
    public void setPositiveResult(View view) {

        loadData();

        positiveTest = true;

        addPositiveCase(uuID);

        togglePositiveResult();

        saveData();
    }

    /**
     * Changes the text displaying whether or not the user has reported a positive test.
     *
     * @author Josh R
     */
    private void togglePositiveResult() {
        Button positiveText = findViewById(R.id.PositiveTestBttn);
        if (positiveTest) {
            positiveText.setText(R.string.Retract_Positive_Test_Result);
        } else {
            positiveText.setText(R.string.Submit_Positive_Test_Result);
        }
    }


    /**
     * Changes the text displaying whether or not the user has potentially been exposed to COVID-19.
     *
     * @author Josh R
     */
    private void toggleWasExposed() {
        TextView exposureText = findViewById(R.id.ExposureText);

        if (wasExposed) {
            exposureText.setText("You may have been exposed, please refer to CDC guidelines on how to quarantine.");
        } else {
            exposureText.setText("You have not been exposed");
        }
    }


    /**
     * @param urlToRead
     * @return
     * @throws IOException
     * @author Brett J
     */
    public static String makeRequestGET(String urlToRead) {
        try {
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
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return "";
    }

    /**
     * @param urlToRead
     * @param data
     * @return
     * @throws IOException
     * @author Brett J
     */
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


    private static JSONObject convertString(String data) throws JSONException {
        return new JSONObject(data);
    }

    /**
     * @return
     * @throws JSONException
     * @author mknox
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean checkContact() throws JSONException {

        //todo: we should check for multiple interactions with the same device as the CDC says 'over a 15 minute span'

        StringBuilder response = new StringBuilder();
        contactList.forEach(id -> {
            String url = FIREBASE_URL + "positive_cases.json?orderBy=\"$key\"&equalTo=\""+id+'"';
            Log.i(TAG, url+"\n"+response);
            response.append(makeRequestGET(FIREBASE_URL + "positive_cases.json?orderBy=\"$key\"&equalTo=\""+id+'"'));
        });
        JSONObject object = convertString(response.toString());
        return object.length() != 0;
    }

    /**
     * "{\"id\": \""+id+"\"}"
     *
     * @param id - uuid of the device recording a positive result.
     * @return
     * @throws IOException
     * @author Brett J
     */
    public boolean addPositiveCase(String id) {
        boolean response = false;
        try {
            response = makeRequestPATCH(FIREBASE_URL + "positive_cases.json", "{\"" + id + "\": true}");
            if (response) {
                Log.i(TAG, "Added positive case successfully.");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return response;
    }
}
