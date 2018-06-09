package com.example.hung.fparkingowners;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.hung.fparkingowners.dto.BookingDTO;

import org.json.JSONObject;

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

            } catch (Exception ex) {
                Log.e("Error:", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
           }
    }
}
