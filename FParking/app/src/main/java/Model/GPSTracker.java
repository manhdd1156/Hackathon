package Model;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    boolean canGetLocation = false;

    Location mLocation;

    double latitude;
    double longitude;

    protected LocationManager locationManager;

    public GPSTracker(Context mContext) {
        this.mContext = mContext;
        getLocation();
    }

    private Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // get GPS status

            isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // get Network status

            isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnable && !isNetworkEnable) {
                showSettingsAlert();
            } else {
                this.canGetLocation = true;

                // if Network Enabled get lat/long using GPS Services
                if (isNetworkEnable) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    if (locationManager != null) {
                        mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLocation!= null){
                            latitude = mLocation.getLatitude();
                            longitude = mLocation.getLongitude();
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnable) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    if (locationManager != null) {
                        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (mLocation!= null){
                            latitude = mLocation.getLatitude();
                            longitude = mLocation.getLongitude();
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mLocation;
    }

    public boolean isCanGetLocation() {
        return canGetLocation;
    }

    public double getLatitude() {
        if(mLocation!= null){
            latitude = mLocation.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if(mLocation!= null){
            longitude = mLocation.getLongitude();
        }
        return longitude;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);


        alertDialog.setTitle("GPS is not Enabled!");

        alertDialog.setMessage("Do you want to turn on GPS?");


        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });


        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        alertDialog.show();
    }

    public void stopListener(){
        if(locationManager!= null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

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
}
