package com.example.hung.fparking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Model.GPSTracker;
import Service.Constants;
import Service.HttpHandler;

public class CheckOut extends AppCompatActivity {

    TextView textViewAddress, textViewCheckIn, textViewPrice, textViewLicensePlate, textViewTotalPrice;

    double selectPlaceLat = 0;
    double selectPlaceLng = 0;
    String strJSON;
    String address = "N/A";
    int currentSpace = 0;
    int totalSlot = 0;
    double price = 0;
    String timeCheckIN = "N/A";
    String licensePlate = "N/A";

    Button buttonCheckOut;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferenceEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("driver", 0);
        sharedPreferenceEditor = sharedPreferences.edit();

        textViewAddress = findViewById(R.id.textViewAddress);
        textViewCheckIn = findViewById(R.id.textViewCheckinTime);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewLicensePlate = findViewById(R.id.textViewLicensePlate);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        buttonCheckOut = findViewById(R.id.buttonCheckout);

        final String bookingID = sharedPreferences.getString("bookingID", "");
        final String parkingID = sharedPreferences.getString("parkingID", "");

        if (sharedPreferences.getString("action", "").equals("3")) {
            buttonCheckOut.setText("THANH TOÁN XONG");
            new GetCheckOutInfor(bookingID).execute();
            textViewTotalPrice.setText(sharedPreferences.getString("totalPrice", ""));
            textViewCheckIn.setText(textViewCheckIn.getText() + " - " + sharedPreferences.getString("checkoutTime", ""));
        } else {
            buttonCheckOut.setText("THANH TOÁN");
        }

        buttonCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonCheckOut.getText().equals("THANH TOÁN")) {
                    new pushToOwner("2", "checkout", bookingID,parkingID).execute((Void) null);
                } else {
                    sharedPreferenceEditor.clear().commit();
                    GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

                    double[] myLocation = new double[2];
                    myLocation[0] = gpsTracker.getLatitude();
                    myLocation[1] = gpsTracker.getLongitude();
                    Intent intent = new Intent(CheckOut.this, HomeActivity.class);
                    intent.putExtra("myLocation", myLocation);
                    startActivity(intent);
                }
            }
        });

        new GetCheckOutInfor(bookingID).execute();
    }

    public class GetCheckOutInfor extends AsyncTask<Void, Void, Void> {
        private String bookingID;

        public GetCheckOutInfor(String bookingID) {
            this.bookingID = bookingID;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            HttpHandler httpHandler = new HttpHandler();

//            Intent intentGetParkingLocation = getIntent();
//            String[] getParkingLocation = getLat_lng(intentGetParkingLocation.getStringExtra("ParkingLocation"));
//
//            selectPlaceLat = Double.parseDouble(getParkingLocation[0]);
//            selectPlaceLng = Double.parseDouble(getParkingLocation[1]);

//            Log.e("GetNearPlace:", "O day");
            strJSON = httpHandler.getrequiement("https://fparking.net/realtimeTest/driver/get_CheckOut_Detail.php?bookingID=" + bookingID);
//            Log.e("SQL Detail:", strJSON.toString());

            if (strJSON != null) {

                try {
                    JSONObject jsonObject = new JSONObject(strJSON);

                    JSONArray jsonArrayParking = jsonObject.getJSONArray("checkout_detail");

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
            textViewPrice.setText(price + "");
            textViewLicensePlate.setText(licensePlate);
        }
    }

    public String[] getLat_lng(String location) {
        String[] latlng = location.substring(location.indexOf("(") + 1, location.indexOf(")")).split(",");
        return latlng;
    }

    class pushToOwner extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog pdLoading;
        boolean success = false;
        String action, carID, bookingID, parkingID;

        public pushToOwner(String carID, String action, String bookingID,String parkingID) {
            this.action = action;
            this.carID = carID;
            this.bookingID = bookingID;
            this.parkingID = parkingID;
            pdLoading = new ProgressDialog(CheckOut.this);
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
                formData.put("bookingID", bookingID);
                formData.put("action", action);
                formData.put("parkingID", parkingID);
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
            if (aBoolean == null) {
                pdLoading.dismiss();
                onResume();
            } else {
                pdLoading.dismiss();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(sharedPreferences.getString("action","").equals("3")){
            sharedPreferenceEditor.clear().commit();
        }
    }
}
