package com.covidcontacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.altbeacon.beacon.Beacon;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startBeacon(View view) {
        /* Create the Activity which will run the Beacon Process. */
        Intent beacon = new Intent(this, BeaconActivity.class);
        /* Start the Beacon */
        startActivity(beacon);
    }
}
//test a commit