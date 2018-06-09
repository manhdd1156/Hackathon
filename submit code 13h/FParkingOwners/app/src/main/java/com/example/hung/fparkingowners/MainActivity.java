package com.example.hung.fparkingowners;

import android.Manifest;
import android.app.NotificationChannel;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.example.hung.fparkingowners.config.Constants;
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
        PusherOptions options = new PusherOptions();
        options.setCluster("ap1");
        Pusher pusher = new Pusher(Constants.PUSHER_KEY, options);

        Channel channel = pusher.subscribe(Constants.PUSHER_CHANNEL);

        channel.bind("ORDER_FOR_OWNER", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {

                try {
                    System.out.println("data order : " + data);
                    JSONObject json = new JSONObject(data);
                    handleDataMessage(json, "order");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                createNotification();
            }
        });
        channel.bind("CHECKIN_FOR_OWNER", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {

                try {
                    System.out.println("data checkin là : " + data);
                    JSONObject json = new JSONObject(data);
                    handleDataMessage(json, "checkin");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                createNotification();

            }
        });
        channel.bind("CHECKOUT_FOR_OWNER", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {

                try {
                    System.out.println("data checkout là : " + data);
                    JSONObject json = new JSONObject(data);
                    handleDataMessage(json, "checkout");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                createNotification();

            }
        });
        pusher.connect();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        pref = getApplicationContext().getSharedPreferences("searchVariable", 0);// 0 - là chế độ private
        editor = pref.edit();
        new GetParkingTask().execute((Void) null);
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
    private void handleDataMessage(final JSONObject json, String action) {
        try {
            final String carID = json.getString("carID");

            editor.putInt("carID", Integer.parseInt(carID));
            editor.commit();

            if (action.equals("order")) { // người dùng order => insert booking với status = 1
                new SearchBookingTask("carID=" + carID, "order").execute((Void) null);
//                        final BookingDTO b = new BookingDTO(0, Integer.parseInt(parkingID), Integer.parseInt(carID), "1", "", "", "", "", Double.parseDouble("0"));
            } else if (action.equals("checkin")) {
                final String bookingID = json.getString("bookingID");
                new SearchBookingTask("bookingID=" + bookingID, "checkin").execute((Void) null);

            } else if (action.equals("checkout")) {
                final String bookingID = json.getString("bookingID");
                new SearchBookingTask("bookingID=" + bookingID, "checkout").execute((Void) null);
            }
        } catch (JSONException e) {
            System.out.println("Json Exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Json Exception e : " + e.getMessage());
        }
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
                String json = httpHandler.get(Constants.API_URL + "parking/get.php?ownerPhoneNumber=" + getPhone().replace("+84", "0"));
                JSONObject jsonObj = new JSONObject(json);
                JSONArray parkings = jsonObj.getJSONArray("parkingInfor");
                oneParking = parkings.getJSONObject(0);
                String parkingid = oneParking.getString("parkingID");
                String address = oneParking.getString("address");
                String space = oneParking.getString("space");
                String currentSpace = oneParking.getString("currentSpace");
                editor.putInt("parkingID", Integer.parseInt(parkingid));
                editor.commit();
                setText(tvAddress, address);
                totalSpace = space;
                setText(tvSpace, currentSpace + "/" + space);
            } catch (Exception ex) {
                Log.e("Error:", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            new GetBookingTask(pref.getInt("parkingID", 0) + "").execute((Void) null);
//            new ManagerBookingTask("get", getApplicationContext(), getWindow().getDecorView().getRootView(), parkingID, MainActivity.this, lv, null);
        }
    }
    private void setText(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
    class GetBookingTask extends AsyncTask<Void, Void, List> {
        ProgressDialog pdLoading;
        private final String txtSearch;
        private JSONObject oneBooking;

        public GetBookingTask(String txtSearch) {
            this.txtSearch = txtSearch;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading = new ProgressDialog(MainActivity.this);
            //this method will be running on UI thread
            pdLoading.setMessage("\tĐợi xíu...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected List doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            try {
                ArrayList<BookingDTO> list = new ArrayList<>();
                String json = httpHandler.get(Constants.API_URL + "booking/get_listBookingInfor.php?parkingID=" + txtSearch);
                JSONObject jsonObj = new JSONObject(json);
                JSONArray bookings = jsonObj.getJSONArray("cars");
                for (int i = 0; i < bookings.length(); i++) {
                    oneBooking = bookings.getJSONObject(i);
                    String bookingID = oneBooking.getString("bookingID");
                    String status = oneBooking.getString("status");
                    if (!status.equals("1") && !status.equals("2")) {
                        continue;
                    }
                    String licensePlate = oneBooking.getString("licensePlate");
                    String type = oneBooking.getString("type");
                    String checkinTime = oneBooking.getString("checkinTime");
                    String checkoutTime = oneBooking.getString("checkoutTime");

                    String price = oneBooking.getString("price");
                    BookingDTO b = new BookingDTO(Integer.parseInt(bookingID), Integer.parseInt(txtSearch), 0, status, checkinTime, checkoutTime, licensePlate, type, Double.parseDouble(price));
                    list.add(b);
                }
                System.out.println(list);
                return list;

            } catch (Exception ex) {
                Log.e("Error managerBooking:", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);
            pdLoading.dismiss();
//            System.out.println("set list view data : " + list);
            if (list != null && list.size() > 0) {
                System.out.println(list);

                CarAdapter adapter = new CarAdapter(getWindow().getDecorView().getRootView(), MainActivity.this, list);
                lv.setAdapter(adapter);
            }
        }
    }
    class CarAdapter extends BaseAdapter {
        private Context mContext;
        private List<BookingDTO> listGrade;
        private View view;

        public CarAdapter(View view, Context context, List<BookingDTO> list) {
            mContext = context;
            listGrade = list;
            this.view = view;
        }

        @Override
        public int getCount() {
            return listGrade.size();
        }

        @Override
        public Object getItem(int pos) {
            return listGrade.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            final BookingDTO entry = listGrade.get(pos);
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.cars_list, null);
            }
            TextView tvBienso = (TextView) convertView.findViewById(R.id.tvBienso);
            TextView tvTypeCar = (TextView) convertView.findViewById(R.id.tvTypeCar);
            final Button btnAcept = (Button) convertView.findViewById(R.id.btnAccept);
            Button btnCancel = (Button) convertView.findViewById(R.id.btnCancel);

            if (entry.getStatus().equals("1")) {
                btnAcept.setText("CheckIn");
                btnAcept.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        Calendar calendar = Calendar.getInstance();
                        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String checkinTime = dateFormatter.format(calendar.getTime().getTime());
                        BookingDTO b = entry;
                        b.setStatus("2");
                        b.setCheckinTime(checkinTime.toString());
                        new UpdateBookingTask(b).execute((Void) null);
//                    btnAcept.setText("CHeckOut");
                        onResume();
                    }
                });
            } else if (entry.getStatus().equals("2")) {
                btnCancel.setVisibility(convertView.INVISIBLE);
                btnAcept.setText("CheckOut");
                btnAcept.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        Calendar calendar = Calendar.getInstance();
                        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        final String checkoutTime = dateFormatter.format(calendar.getTime().getTime());
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int choice) {
                                switch (choice) {
                                    case DialogInterface.BUTTON_POSITIVE:

                                        BookingDTO b = entry;
                                        b.setStatus("3");
                                        b.setCheckoutTime(checkoutTime.toString());
                                        new UpdateBookingTask(b).execute((Void) null);
                                        onResume();

                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        try {
                            Date date1 = dateFormatter.parse(entry.getCheckinTime());
                            Date date2 = dateFormatter.parse(checkoutTime);
                            long diff = date2.getTime() - date1.getTime();
                            double diffInHours = diff / ((double) 1000 * 60 * 60);
                            NumberFormat formatter = new DecimalFormat("###,###");
                            NumberFormat formatterHour = new DecimalFormat("0.00");
//                            System.out.println(formatter.format(4.0));
                            builder.setMessage("\tHóa đơn checkout \n"
                                    + "Biển số :          " + entry.getLicensePlate() + "\n"
                                    + "loại xe :           " + entry.getTypeCar() + "\n"
                                    + "thời gian vào : " + entry.getCheckinTime() + "\n"
                                    + "thời gian ra :   " + checkoutTime + "\n"
                                    + "giá đỗ :           " + formatter.format(entry.getPrice()) + "vnđ\n"
                                    + "thời gian đỗ :  " + formatterHour.format(diffInHours) + " giờ \n"
                                    + "tổng giá :        " + formatter.format(diffInHours * entry.getPrice()) + "vnđ")
                                    .setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
            tvBienso.setText(entry.getLicensePlate());
            tvTypeCar.setText(entry.getTypeCar());
            return convertView;
        }
    }
    class SearchBookingTask extends AsyncTask<Void, Void, List> {

        private JSONObject oneBooking;
        //        ProgressDialog pdLoading;
        String txtSearch, action;


        public SearchBookingTask(String txtSearch, String action) {


            this.txtSearch = txtSearch;
            this.action = action;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected List doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            ArrayList<BookingDTO> list = new ArrayList<>();
            try {
                String json = httpHandler.get(Constants.API_URL + "booking/search_BookingInfor.php?" + txtSearch);
                JSONObject jsonObj = new JSONObject(json);
                JSONArray bookings = jsonObj.getJSONArray("result");
                if (action.contains("order")) {
                    oneBooking = bookings.getJSONObject(0);

                    String licensePlate = oneBooking.getString("licensePlate");
                    String carID = oneBooking.getString("carID");


                    BookingDTO b = new BookingDTO(0, pref.getInt("parkingID", 0), Integer.parseInt(carID), "", "", "", licensePlate, "", 3000);
                    list.add(b);
                } else {

                    oneBooking = bookings.getJSONObject(0);
                    String bookingID = oneBooking.getString("bookingID");

                    String status = oneBooking.getString("status");
                    String licensePlate = oneBooking.getString("licensePlate");
                    String type = oneBooking.getString("type");
                    String checkinTime = oneBooking.getString("checkinTime");
                    String checkoutTime = oneBooking.getString("checkoutTime");
                    String price = oneBooking.getString("price");


                    editor.putInt("bookingID", Integer.parseInt(bookingID));
                    editor.putString("status", status);
                    editor.putString("licensePlate", licensePlate);
                    editor.putString("type", type);
                    editor.putString("checkinTime", checkinTime);
                    editor.putString("checkoutTime", checkoutTime);
                    editor.putString("price", price);
                    editor.commit();

                    BookingDTO b = new BookingDTO(Integer.parseInt(bookingID), pref.getInt("parkingID", 0), pref.getInt("carID", 0), status, checkinTime, checkoutTime, licensePlate, type, Double.parseDouble(price));
                    list.add(b);
                }
                return list;
            } catch (Exception ex) {
                Log.e("Error search:", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);
//            pdLoading.dismiss();
            try {
                if (list != null && list.size() > 0) {
                    List<BookingDTO> l = (List<BookingDTO>) list;
                    System.out.println("list search :" + list);
                    editor.putString("licensePlate", l.get(0).getLicensePlate());
                    editor.commit();
                }

                if (action.contains("order")) {
                    createNotification("Có Xe : " + pref.getString("licensePlate", "") + "muốn đặt chỗ");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int choice) {
                            switch (choice) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    // create booking
                                    BookingDTO b = new BookingDTO(pref.getInt("bookingID", 0), pref.getInt("parkingID", 0), pref.getInt("carID", 0), "", "", "", "", "", 5000);
                                    new CreateBookingTask(b).execute((Void) null);
//                                    new ManagerBookingTask("insert", getApplicationContext(), getWindow().getDecorView().getRootView(), "not need", MainActivity.this, lv, b);

                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    try {
                        builder.setMessage("Xe " + pref.getString("licensePlate", "") + "muốn đỗ xe trong bãi bạn có đồng ý không")
                                .setPositiveButton("Có", dialogClickListener)
                                .setNegativeButton("Không", dialogClickListener).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (action.contains("checkin")) {
                    createNotification("Xe " + pref.getString("licensePlate", "") + " đã đến và muốn vào bãi");
                    createDialog("checkin");
                } else if (action.contains("checkout")) {
                    createNotification("Xe " + pref.getString("licensePlate", "") + " muốn thanh toán");
                    createDialog("checkout");


                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

}
