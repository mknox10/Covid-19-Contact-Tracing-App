package com.covidcontacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

        TextView bttnTxt = (TextView) findViewById(R.id.StartBeaconBttn);
        bttnTxt.setText("Beacon on");
    }


//dxxvxv

}
