package com.example.hung.fparking;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Service.Constants;
import Service.HttpHandler;

public class OrderParking extends AppCompatActivity {

    double selectPlaceLat = 0;
    double selectPlaceLng = 0;
    String strJSON;
    TextView textViewEmptySpace, textViewSlots, textViewPrice, textViewTime, textViewAddress;
    Button buttonDat_Cho;
    String address = "N/A";
    int currentSpace = 0;
    int totalSlot = 0;
    double price = 0;
    String time = "N/A";
    double parkingLatitde = 0;
    double parkinglongitude = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferenceEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_parking);
        getSupportActionBar().hide();

        textViewAddress = findViewById(R.id.textViewAddress);
        textViewEmptySpace = findViewById(R.id.textViewEmptySpace);
        textViewSlots = findViewById(R.id.textViewSlots);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewTime = findViewById(R.id.textViewTime);
        buttonDat_Cho = findViewById(R.id.buttonDat_Cho_Ngay);

        buttonDat_Cho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences("driver", 0);
                sharedPreferenceEditor = sharedPreferences.edit();
                sharedPreferenceEditor.putString("parkingLat", parkingLatitde + "");
                sharedPreferenceEditor.putString("parkingLng", parkinglongitude + "");
                sharedPreferenceEditor.commit();

                new pushToOwner("2","order").execute((Void)null);
            }
        });

        new GetDetailParking().execute();
    }

    public String[] getLat_lng(String location) {
        String[] latlng = location.substring(location.indexOf("(") + 1, location.indexOf(")")).split(",");
        return latlng;
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

    class pushToOwner extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog pdLoading;
        boolean success = false;
        String action,carID;
        public pushToOwner(String carID, String action) {
            this.action = action;
            this.carID = carID;
            pdLoading = new ProgressDialog(OrderParking.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tĐợi xíu...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            try {
                JSONObject formData = new JSONObject();
                formData.put("carID", carID);
                formData.put("action", action);
                String json = httpHandler.post(Constants.API_URL + "driver/booking.php", formData.toString());
                JSONObject jsonObj = new JSONObject(json);
                if (jsonObj.getInt("size") > 0) {
                    success = true;
                }

            } catch (Exception ex) {
                Log.e("Error:", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean==null) {
                pdLoading.dismiss();
                onResume();
            }else {
                pdLoading.dismiss();
            }
        }

    }

}
