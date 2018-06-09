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
//import android.support.v7.app.NotificationCompat;
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
//import com.pusher.client.Pusher;
//import com.pusher.client.PusherOptions;
//import com.pusher.client.channel.Channel;
//import com.pusher.client.channel.SubscriptionEventListener;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

    }

}