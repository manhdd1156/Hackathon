package com.example.hung.fparking;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Entity.GetNearPlace;
import Model.GPSTracker;
import Service.HttpHandler;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback, PlaceSelectionListener, GoogleMap.OnCameraMoveStartedListener, LocationListener {

    private GoogleMap mMap;
    int check = 0;
    double searchPlaceLat = 0;
    double searchPlaceLng = 0;
    double myLocationLat = 0;
    double myLocationLng = 0;
    double selectPlaceLat = 0;
    double selectPlaceLng = 0;
    String strJSON = null;
    ArrayList<Entity.GetNearPlace> nearParkingList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    View mMapView;
    private LocationManager locationManager = null;

    boolean userAction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = getSharedPreferences("driver", 0);
        sharedPreferencesEditor = sharedPreferences.edit();
        searchPlace();

        mMapView = mapFragment.getView();
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 1500, 0, 0);

        new GetNearPlace().execute();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String parkingLocation = marker.getPosition().toString();
                String[] latlng = getLat_lng(parkingLocation);
                String lat = latlng[0];
                String lng = latlng[1];
                if (sharedPreferences.getString("bookingID", "").equals("")) {
                    Intent intentOrderFlagment = new Intent(HomeActivity.this, OrderParking.class);
                    intentOrderFlagment.putExtra("ParkingLocation", parkingLocation);
                    startActivity(intentOrderFlagment);
                } else if (sharedPreferences.getString("parkingLat", "").equals(lat) && sharedPreferences.getString("parkingLng", "").equals(lng)) {
                    if (sharedPreferences.getString("action", "").equals("2")) {
                        Intent intentOrderFlagment = new Intent(HomeActivity.this, CheckOut.class);
                        intentOrderFlagment.putExtra("ParkingLocation", parkingLocation);
                        startActivity(intentOrderFlagment);
                    } else {
                        Intent intentOrderFlagment = new Intent(HomeActivity.this, OrderParking.class);
                        intentOrderFlagment.putExtra("ParkingLocation", parkingLocation);
                        startActivity(intentOrderFlagment);
                    }

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int choice) {
                            switch (choice) {
                                case DialogInterface.BUTTON_POSITIVE:


                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:

                                    break;
                            }
                        }
                    };
                    try {
                        builder.setMessage("Bạn đang đỗ xe tại đây ")
                                .setPositiveButton("Có", dialogClickListener).setCancelable(false).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                check = 1;
                userAction = false;
                GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
                searchPlaceLat = gpsTracker.getLatitude();
                searchPlaceLng = gpsTracker.getLongitude();
                new GetNearPlace().execute();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(searchPlaceLat, searchPlaceLng), 15));
                return false;
            }
        });
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()), 15));


        // Gọi Listener của movecamera
        mMap.setOnCameraMoveStartedListener(this);
        callLocationChangedListener();
    }

    public void searchPlace() {
        PlaceAutocompleteFragment placeAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_home);
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder().setCountry("VN").build();

        placeAutocompleteFragment.setFilter(autocompleteFilter);
        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            Marker marker;

            @Override
            public void onPlaceSelected(Place place) {
                if (marker != null) {
                    marker.remove();
                }
                mMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLngMaker = place.getLatLng();

                markerOptions.position(latLngMaker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngMaker, 15));

                check = 1;
                userAction = true;
                String[] latlng = getLat_lng(latLngMaker.toString());
                searchPlaceLat = Double.parseDouble(latlng[0]);
                searchPlaceLng = Double.parseDouble(latlng[1]);


                new GetNearPlace().execute();
            }

            @Override
            public void onError(Status status) {
            }
        });
    }

    @Override
    public void onPlaceSelected(Place place) {

    }

    @Override
    public void onError(Status status) {

    }

    @Override
    public void onCameraMoveStarted(int i) {

        mMap.clear();
        check = 1;
        userAction = true;
        LatLng cameraLatLng = mMap.getCameraPosition().target;
        String[] locaton = getLat_lng(cameraLatLng.toString());
        searchPlaceLng = Double.parseDouble(locaton[0]);
        searchPlaceLng = Double.parseDouble(locaton[1]);
        new GetNearPlace().execute();

    }

    private void callLocationChangedListener() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!userAction) {
            mMap.clear();
            check = 1;
            searchPlaceLat = location.getLatitude();
            searchPlaceLng = location.getLongitude();

            new GetNearPlace().execute();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public class GetNearPlace extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            HttpHandler httpHandler = new HttpHandler();
            if (check == 0) {
                Intent intent = getIntent();
                double[] locaton = intent.getDoubleArrayExtra("myLocation");

                selectPlaceLat = locaton[0];
                selectPlaceLng = locaton[1];
            } else {
                selectPlaceLat = searchPlaceLat;
                selectPlaceLng = searchPlaceLng;
            }
            strJSON = httpHandler.getrequiement("https://fparking.net/realtimeTest/driver/get_near_my_location.php?latitude=" + selectPlaceLat + "&" + "longitude=" + selectPlaceLng);
            Log.e("SQL:", strJSON.toString());
            if (strJSON != null) {
                nearParkingList = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(strJSON);

                    JSONArray jsonArrayParking = jsonObject.getJSONArray("near_location");

                    for (int i = 0; i < jsonArrayParking.length(); i++) {
                        JSONObject c = jsonArrayParking.getJSONObject(i);

                        double lat = c.getDouble("latitude");
                        double lng = c.getDouble("longitude");

                        nearParkingList.add(new Entity.GetNearPlace(lat, lng));
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
            int height = 90;
            int width = 90;
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.parking_icon);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

            BitmapDrawable bitmapPark = (BitmapDrawable) getResources().getDrawable(R.drawable.parking_icon_green);
            Bitmap p = bitmapPark.getBitmap();
            Bitmap parkMarker = Bitmap.createScaledBitmap(p, width, height, false);
            super.onPreExecute();
            if (nearParkingList.size() > 0) {
                for (int i = 0; i < nearParkingList.size(); i++) {
                    LatLng latLng = new LatLng(nearParkingList.get(i).getLattitude(), nearParkingList.get(i).getLongitude());
                    if (sharedPreferences.getString("parkingLat", "").equals(nearParkingList.get(i).getLattitude() + "") && sharedPreferences.getString("parkingLng", "").equals(nearParkingList.get(i).getLongitude() + "")) {
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng).icon(BitmapDescriptorFactory.fromBitmap(parkMarker)));
                    } else {
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                    }
                }
            }
        }
    }

    public String[] getLat_lng(String location) {
        String[] latlng = location.substring(location.indexOf("(") + 1, location.indexOf(")")).split(",");
        return latlng;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
