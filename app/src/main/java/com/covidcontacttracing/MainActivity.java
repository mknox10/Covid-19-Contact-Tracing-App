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
        //Intent beacon = new Intent(this, BeaconActivity.class);
        /* Start the Beacon */
        //startActivity(beacon);
        Intent beacon = new Intent(this, BeaconService.class);
        startService(beacon);

        TextView bttnTxt = findViewById(R.id.StartBeaconBttn);
        bttnTxt.setText("Beacon on");
    }

    public void CheckExposure(View view){
        //check the database fore recent exposure


        TextView lblExposure = (TextView) findViewById(R.id.lblExposure);
        boolean wasExposed = false;

        if(wasExposed ) {
            lblExposure.setText("You have been exposed please check CDC guiedlines on how to quarantine ");
        }else{
            lblExposure.setText("You have not been exposed");
        }

    }

    public void PositiveResult(View view){
        //push to the database that you have a positive test result


    }

}
