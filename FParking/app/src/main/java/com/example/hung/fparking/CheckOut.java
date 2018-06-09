package com.example.hung.fparking;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Service.HttpHandler;

public class CheckOut extends AppCompatActivity {

    TextView textViewAddress, textViewCheckIn, textViewPrice, textViewLicensePlate;

    double selectPlaceLat = 0;
    double selectPlaceLng = 0;
    String strJSON;
    String address = "N/A";
    int currentSpace = 0;
    int totalSlot = 0;
    double price = 0;
    String timeCheckIN = "N/A";
    String licensePlate = "N/A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        getSupportActionBar().hide();

        textViewAddress = findViewById(R.id.textViewAddress);
        textViewCheckIn = findViewById(R.id.textViewCheckinTime);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewLicensePlate = findViewById(R.id.textViewLicensePlate);

        new GetCheckOutInfor().execute();
    }

    public class GetCheckOutInfor extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            HttpHandler httpHandler = new HttpHandler();

//            Intent intentGetParkingLocation = getIntent();
//            String[] getParkingLocation = getLat_lng(intentGetParkingLocation.getStringExtra("ParkingLocation"));
//
//            selectPlaceLat = Double.parseDouble(getParkingLocation[0]);
//            selectPlaceLng = Double.parseDouble(getParkingLocation[1]);

            Log.e("GetNearPlace:", "O day");
            strJSON = httpHandler.getrequiement("https://fparking.net/realtimeTest/driver/get_CheckOut_Detail.php?bookingID=143");
            Log.e("SQL Detail:", strJSON.toString());

            if (strJSON != null) {

                try {
                    JSONObject jsonObject = new JSONObject(strJSON);

                    JSONArray jsonArrayParking = jsonObject.getJSONArray("detail_parking");

                    for (int i = 0; i < jsonArrayParking.length(); i++) {
                        JSONObject c = jsonArrayParking.getJSONObject(i);

                        address = c.getString("address");
                        price = c.getDouble("price");
                        timeCheckIN = c.getString("checkinTime");
                        licensePlate = c.getString("licensePlate");
                    }
                } catch (JSONException e) {
                    Log.e("JSONException:", e.getMessage());
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            textViewAddress.setText(address);
            textViewCheckIn.setText(timeCheckIN);
            textViewPrice.setText(price+"");
            textViewLicensePlate.setText(licensePlate);
        }

    }
    public String[] getLat_lng(String location) {
        String[] latlng = location.substring(location.indexOf("(") + 1, location.indexOf(")")).split(",");
        return latlng;
    }
}
