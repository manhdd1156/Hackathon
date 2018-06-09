package com.example.hung.fparking;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import Model.GPSTracker;


public class LoadFirstTime extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 5000;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_first_time);
        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("dirver", 0);
        int parkingID = 0;
        sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putInt("parkingID", parkingID);
        sharedPreferencesEditor.commit();
//        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
//
//        double[] myLocation = new double[2];
//        myLocation[0] = gpsTracker.getLatitude();
//        myLocation[1] = gpsTracker.getLongitude();
//
//        Intent homeIntent = new Intent(LoadFirstTime.this, HomeActivity.class);
//        homeIntent.putExtra("myLocation", myLocation);
//        startActivity(homeIntent);
        splash();
    }

    public void splash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

                double[] myLocation = new double[2];
                myLocation[0] = gpsTracker.getLatitude();
                myLocation[1] = gpsTracker.getLongitude();

                Intent homeIntent = new Intent(LoadFirstTime.this, HomeActivity.class);
                homeIntent.putExtra("myLocation", myLocation);
                startActivity(homeIntent);
                finish();

            }
        }, SPLASH_TIME_OUT);
    }
}
