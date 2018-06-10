package com.example.hung.fparking;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Model.DirectionFinder;
import Model.DirectionFinderListener;
import Entity.Route;
import Model.GPSTracker;
import Service.Constants;
import Service.HttpHandler;

public class Direction extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener {

    private GoogleMap mMap;
    private Button btnFindPath;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private LocationManager locationManager = null;
    Notification noti;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferenceEditor;

    Button buttonCheckin, buttonHuy;
    View mMapView;
    private boolean userGesture = false;
    ProgressDialog proD;
    AlertDialog.Builder builder;

    CameraPosition cameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        sharedPreferences = getSharedPreferences("driver", 0);
        sharedPreferenceEditor = sharedPreferences.edit();
        final String parkingID = sharedPreferences.getString("parkingID", "");

//        proD = new ProgressDialog(OrderParking.this);
//        builder = new AlertDialog.Builder(OrderParking.this);

        buttonCheckin = (Button) findViewById(R.id.buttonCheckin);
        buttonHuy = (Button) findViewById(R.id.buttonHuy);

        buttonCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationManager.removeUpdates(Direction.this);
                new pushToOwner("2", "checkin", sharedPreferences.getString("bookingID", ""), parkingID).execute((Void) null);

            }
        });

        buttonHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                new CountDownTimer(15000, 1000) {
////                    boolean checkOwer = true;
////
////                    public void onTick(long millisUntilFinished) {
////                        proD.setMessage("\tĐang đợi chủ xe xác nhận ... " + millisUntilFinished / 1000);
////                    }
////
////                    public void onFinish() {
////                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
////                            @Override
////                            public void onClick(DialogInterface dialog, int choice) {
////                                switch (choice) {
////                                    case DialogInterface.BUTTON_POSITIVE:
////
////                                        break;
////                                    case DialogInterface.BUTTON_NEGATIVE:
////
////                                        break;
////                                }
////                            }
////                        };
////                        try {
////                            proD.dismiss();
////                            builder.setMessage("Chủ bãi đỗ đang bận!")
////                                    .setPositiveButton("Chấp Nhận", dialogClickListener).setCancelable(false).show();
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
////                    }
////                }.start();
////
////                locationManager.removeUpdates(Direction.this);
////                new pushToOwner("2", "cancel", sharedPreferences.getString("bookingID", "")).execute((Void) null);
                AlertDialog.Builder builder = new AlertDialog.Builder(Direction.this);
                builder.setMessage("Hủy chỗ bạn sẽ bị phạt 5000 đồng vào lần gửi xe tới! Bạn chắc chắn hủy");
                builder.setCancelable(true);
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new pushToOwner("2", "cancel", sharedPreferences.getString("bookingID", ""), parkingID).execute((Void) null);
                        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

                        Intent intentBackHome = new Intent(Direction.this, HomeActivity.class);
                        double[] myLocation = new double[2];
                        myLocation[0] = gpsTracker.getLatitude();
                        myLocation[1] = gpsTracker.getLongitude();
                        sharedPreferenceEditor.putString("locationLT", gpsTracker.getLatitude() + "");
                        sharedPreferenceEditor.putString("locationLN", gpsTracker.getLongitude() + "");
                        sharedPreferenceEditor.commit();
                        locationManager.removeUpdates(Direction.this);
//                        intentBackHome.putExtra("myLocation",myLocation);
                        startActivity(intentBackHome);
                    }
                });

                builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Không hủy nhé", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.show();
            }
        });

        mMapView = mapFragment.getView();
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 1500, 0, 0);

        // Gửi yêu cầu chỉ đường
        sendRequest();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Cần check
        locationManager.removeUpdates(Direction.this);
        Intent intent = new Intent(Direction.this, OrderParking.class);
        intent.putExtra("ParkingLocation", "");
        startActivity(intent);
    }

    private void callLocationChangedListener() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendRequest() {

//        if (origin.isEmpty()) {
//            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (destination.isEmpty()) {
//            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
//            return;
//        }

        sharedPreferences = getSharedPreferences("driver", 0);
        GPSTracker gps = new GPSTracker(this);
        String directionLat = sharedPreferences.getString("parkingLat", "");
        String directionLng = sharedPreferences.getString("parkingLng", "");

        Log.e("TOA DO DIEM DEN: ", directionLat + "---" + directionLng);

        String ori = directionLat + "," + directionLng;
        try {
            new DirectionFinder(this, gps.getLatitude() + "," + gps.getLongitude(), directionLat + "," + directionLng).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        GPSTracker gps = new GPSTracker(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 18));
//        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                userGesture = false;
                return false;
            }
        });

        // Gọi listener OnCameraMoveStartedListener
        mMap.setOnCameraMoveStartedListener(this);
        // Gọi listener LocationChanged
        callLocationChangedListener();
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(15);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.e("Direction class onLocationChanged: ", "location changed");
        if (sharedPreferences.getString("action", "").equals("2")) {
            locationManager.removeUpdates(Direction.this);
        }
        if (!userGesture) {
            cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))             // Sets the center of the map to current location
                    .zoom(18)                   // Sets the zoom
                    .bearing(location.getBearing()) // Sets the orientation of the camera to east
                    .tilt(0)                   // Sets the tilt of the camera to 0 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        Location distination = new Location("distination");
        try {
            distination.setLatitude(Double.parseDouble(sharedPreferences.getString("parkingLat", "")));
            distination.setLongitude(Double.parseDouble(sharedPreferences.getString("parkingLng", "")));
            double distanceValue = distination.distanceTo(location);
            if (distanceValue <= 40) {

                createNotification("Fparking");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void createNotification(String title) {
        Intent intent = new Intent(this, Direction.class);
//        intent.putExtra("NotificationMessage", "order");
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
//                .setDefaults(Notification.DE)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_noti)
                .setTicker("Hearty365")
                .setPriority(3)
                .setContentTitle(title)
                .setContentIntent(pIntent)
                .setContentText("Xe đã đến điểm đỗ. Vui lòng vào check in!")
                .setContentInfo("Info");
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            userGesture = true;
            Toast.makeText(this, "The user gestured on the map.",
                    Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            Toast.makeText(this, "The user tapped something on the map.",
                    Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            Toast.makeText(this, "The app moved the camera.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onCameraMove() {

    }

    class pushToOwner extends AsyncTask<Void, Void, Boolean> {
        boolean success = false;
        String action, carID, bookingID, parkingID;

        public pushToOwner(String carID, String action, String bookingID, String parkingID) {
            this.action = action;
            this.carID = carID;
            this.bookingID = bookingID;
            this.parkingID = parkingID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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
                onResume();
            } else {
            }
        }

    }
}
