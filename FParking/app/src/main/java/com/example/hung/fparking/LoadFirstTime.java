package com.example.hung.fparking;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import Model.GPSTracker;
import Service.Constants;


public class LoadFirstTime extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_first_time);
        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("driver", 0);
//        int parkingID = 0;
        sharedPreferencesEditor = sharedPreferences.edit();
//        sharedPreferencesEditor.putString("bookingID","");
        sharedPreferencesEditor.clear().commit();
        Log.e("Cái này chạy kinh qua","aaaaaaaaaa");
//        sharedPreferencesEditor.putInt("parkingID", parkingID);
        sharedPreferencesEditor.commit();
//        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
//
//        double[] myLocation = new double[2];
//        myLocation[0] = gpsTracker.getLatitude();
//        myLocation[1] = gpsTracker.getLongitude();
//
//        Intent homeIntent = new Intent(LoadFirstTime.this, HomeActivity.class);./
//        homeIntent.putExtra("myLocation", myLocation);
//        startActivity(homeIntent);
        // pusher

        PusherOptions options = new PusherOptions();
        options.setCluster("ap1");
        Pusher pusher = new Pusher(Constants.PUSHER_KEY, options);

        Channel channel = pusher.subscribe(Constants.PUSHER_CHANNEL);

        channel.bind("ORDER_FOR_BOOKING", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {

                try {
                    System.out.println("data order : " + data);
                    JSONObject json = new JSONObject(data);
                    handleDataMessage(json, "order");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                createNotification();
            }
        });
        channel.bind("CHECKIN_FOR_BOOKING", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {

                try {
                    System.out.println("data checkin là : " + data);
                    JSONObject json = new JSONObject(data);
                    handleDataMessage(json, "checkin");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                createNotification();

            }
        });
        channel.bind("CHECKOUT_FOR_BOOKING", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {

                try {
                    System.out.println("data checkout là : " + data);
                    JSONObject json = new JSONObject(data);
                    handleDataMessage(json, "checkout");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                createNotification();

            }
        });
        pusher.connect();


        splash();
    }


    private void handleDataMessage(final JSONObject json, String action) {
        try {
            final String carID = json.getString("carID");

            if (carID.equals("2")) {
                final String message = json.getString("message");
                if (message.equals("OK")) {
                    if (action.equals("order")) { // người dùng order => insert booking với status = 1
                        final String bookingID = json.getString("bookingID");
                        final String stt = json.getString("action");

                        sharedPreferencesEditor.putString("carID", carID);
                        sharedPreferencesEditor.putString("bookingID", bookingID);
                        sharedPreferencesEditor.putString("action", stt);
                        sharedPreferencesEditor.commit();
                        Intent intent = new Intent(LoadFirstTime.this, Direction.class);
                        startActivity(intent);
                    } else if (action.equals("checkin")) {
                        final String bookingID = json.getString("bookingID");
                        if (bookingID.equals(sharedPreferences.getString("bookingID", ""))) {
                            final String stt = json.getString("action");

                            sharedPreferencesEditor.putString("action", stt);
                            sharedPreferencesEditor.commit();
                            Intent intent = new Intent(LoadFirstTime.this, CheckOut.class);
                            startActivity(intent);
                        }
                    } else if (action.equals("checkout")) {
                        final String bookingID = json.getString("bookingID");
                        if (bookingID.equals(sharedPreferences.getString("bookingID", ""))) {
                            final String stt = json.getString("action");
                            final String licensePlate = json.getString("licensePlate");
                            final String type = json.getString("type");
                            final String checkinTime = json.getString("checkinTime");
                            final String checkoutTime = json.getString("checkoutTime");
                            final String price = json.getString("price");
                            final String hours = json.getString("hours");
                            final String totalPrice = json.getString("totalPrice");


                            sharedPreferencesEditor.putString("action", stt);
                            sharedPreferencesEditor.putString("licensePlate", licensePlate);
                            sharedPreferencesEditor.putString("type", type);
                            sharedPreferencesEditor.putString("checkinTime", checkinTime);
                            sharedPreferencesEditor.putString("checkoutTime", checkoutTime);
                            sharedPreferencesEditor.putString("price", price);
                            sharedPreferencesEditor.putString("hours", hours);
                            sharedPreferencesEditor.putString("totalPrice", totalPrice);
                            sharedPreferencesEditor.commit();
                            Intent intent = new Intent(LoadFirstTime.this, CheckOut.class);
                            startActivity(intent);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            System.out.println("Json Exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Json Exception e : " + e.getMessage());
        }
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
