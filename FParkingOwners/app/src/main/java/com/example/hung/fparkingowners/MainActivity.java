package com.example.hung.fparkingowners;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

//import com.fparking.pushertest.adapter.CarAdapter;
//import com.fparking.pushertest.asynctask.HttpHandler;
//import com.fparking.pushertest.asynctask.IAsyncTaskHandler;
//import com.fparking.pushertest.asynctask.ManagerBookingTask;
//import com.fparking.pushertest.config.Constants;
//import com.fparking.pushertest.dto.BookingDTO;
//import com.fparking.pushertest.dto.ParkingInforDTO;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import com.example.hung.fparkingowners.dto.BookingDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public String ownerPhoneNumber,totalSpace;
    ListView lv;
    BookingDTO bookingDTO;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    TextView tvSpace,tvAddress;
    String wantPermission = android.Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            ownerPhoneNumber = getPhone();
        }
        System.out.println(ownerPhoneNumber);
        pref = getApplicationContext().getSharedPreferences("searchVariable", 0);// 0 - là chế độ private
        editor = pref.edit();
        tvSpace = (TextView) findViewById(R.id.tvSpace);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        lv = (ListView) findViewById(R.id.cars_list);
//        PusherOptions options = new PusherOptions();
//        options.setCluster("ap1");
//        Pusher pusher = new Pusher(Constants.PUSHER_KEY, options);
//
//        Channel channel = pusher.subscribe(Constants.PUSHER_CHANNEL);
//
//        channel.bind("ORDER_FOR_OWNER", new SubscriptionEventListener() {
//            @Override
//            public void onEvent(String channelName, String eventName, final String data) {
//
//                try {
//                    System.out.println("data order : " + data);
//                    JSONObject json = new JSONObject(data);
//                    handleDataMessage(json, "order");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
////                createNotification();
//            }
//        });
//        channel.bind("CHECKIN_FOR_OWNER", new SubscriptionEventListener() {
//            @Override
//            public void onEvent(String channelName, String eventName, final String data) {
//
//                try {
//                    System.out.println("data checkin là : " + data);
//                    JSONObject json = new JSONObject(data);
//                    handleDataMessage(json, "checkin");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
////                createNotification();
//
//            }
//        });
//        channel.bind("CHECKOUT_FOR_OWNER", new SubscriptionEventListener() {
//            @Override
//            public void onEvent(String channelName, String eventName, final String data) {
//
//                try {
//                    System.out.println("data checkout là : " + data);
//                    JSONObject json = new JSONObject(data);
//                    handleDataMessage(json, "checkout");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
////                createNotification();
//
//            }
//        });
//        pusher.connect();
    }

    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
            Toast.makeText(MainActivity.this, "Phone state permission allows us to get phone number. Please allow it for additional functionality.",
                    Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }
    private boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(MainActivity.this, permission);
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ownerPhoneNumber = getPhone();
                } else {

                    Toast.makeText(MainActivity.this, "Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    private String getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return "lỗi : không có quyền truy cập số điện thoại";
        }
        return phoneMgr.getLine1Number();
    }
    class GetParkingTask extends AsyncTask<Void, Void, Boolean> {

        private JSONObject oneParking;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            try {
//                String json = httpHandler.get(Constants.API_URL + "parking/get.php?ownerPhoneNumber=" + getPhone().replace("+84", "0"));
//                JSONObject jsonObj = new JSONObject(json);
//                JSONArray parkings = jsonObj.getJSONArray("parkingInfor");
//                oneParking = parkings.getJSONObject(0);
//                String parkingid = oneParking.getString("parkingID");
//                String address = oneParking.getString("address");
//                String space = oneParking.getString("space");
//                String currentSpace = oneParking.getString("currentSpace");
//                editor.putInt("parkingID", Integer.parseInt(parkingid));
//                editor.commit();
//                setText(tvAddress, address);
//                totalSpace = space;
//                setText(tvSpace, currentSpace + "/" + space);
////                tvSpace.setText(currentSpace + "/" + space);
////                json = httpHandler.get(Constants.API_URL + "booking/get_countBookingInfor.php?parkingID=" + parkingID);
////                jsonObj = new JSONObject(json);
////                JSONArray bookingInfors = jsonObj.getJSONArray("numberCarInParking");
////                JSONObject c = bookingInfors.getJSONObject(0);
////                bookingSpace = c.getString("NumberOfBooking");
            } catch (Exception ex) {
                Log.e("Error:", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
//            new GetBookingTask(pref.getInt("parkingID", 0) + "").execute((Void) null);
//            new ManagerBookingTask("get", getApplicationContext(), getWindow().getDecorView().getRootView(), parkingID, MainActivity.this, lv, null);
        }
    }
}
