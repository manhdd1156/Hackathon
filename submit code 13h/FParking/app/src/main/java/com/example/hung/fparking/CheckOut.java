package com.example.hung.fparking;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Service.HttpHandler;

public class CheckOut extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        getSupportActionBar().hide();
    }

    public class GetDetailParking extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            HttpHandler httpHandler = new HttpHandler();

            Intent intentGetParkingLocation = getIntent();
            String[] getParkingLocation = getLat_lng(intentGetParkingLocation.getStringExtra("ParkingLocation"));

            selectPlaceLat = Double.parseDouble(getParkingLocation[0]);
            selectPlaceLng = Double.parseDouble(getParkingLocation[1]);

            Log.e("GetNearPlace:", "O day");
            strJSON = httpHandler.getrequiement("https://fparking.net/realtimeTest/driver/get_Detail_Parking.php?latitude=" + selectPlaceLat + "&" + "longitude=" + selectPlaceLng);
            Log.e("SQL Detail:", strJSON.toString());

            if (strJSON != null) {

                try {
                    JSONObject jsonObject = new JSONObject(strJSON);

                    JSONArray jsonArrayParking = jsonObject.getJSONArray("detail_parking");

                    for (int i = 0; i < jsonArrayParking.length(); i++) {
                        JSONObject c = jsonArrayParking.getJSONObject(i);

                        address = c.getString("address");
                        currentSpace = c.getInt("currentSpace");
                        totalSlot = c.getInt("space");
                        price = c.getDouble("price");
                        time = c.getString("timeoc");
                        parkingLatitde = c.getDouble("latitude");
                        parkinglongitude = c.getDouble("longitude");

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

            textViewEmptySpace.setText(totalSlot - currentSpace + "");
            textViewSlots.setText("/" + totalSlot + "");
            textViewPrice.setText(price + "");
            textViewTime.setText(time);
            textViewAddress.setText(address);
        }

    }
}
