package com.example.hung.fparkingowners;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public String ownerPhoneNumber,totalSpace,currentBooking;
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

            }
        });
        pusher.connect();
    }
    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("NotificationMessage")) {
                setContentView(R.layout.activity_main);
                onResume();
                // extract the extra-data in the Notification
                String msg = extras.getString("NotificationMessage");
                if (msg.contains("order")) {
                    createNotification("order","Có 1 xe mới : " + pref.getString("licensePlate", "") + " muốn đặt chỗ");
                    createDialog("order");
                } else if (msg.contains("checkin")) {
                    createNotification("checkin","Xe " + pref.getString("licensePlate", "") + " đã đến và muốn vào bãi");
                    createDialog("checkin");
                } else if (msg.contains("checkout")) {
                    createNotification("checkout","Xe " + pref.getString("licensePlate", "") + " muốn thanh toán");
                    createDialog("checkout");
                }
            }
        }


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
                currentBooking = oneParking.getString("currentSpace");
                editor.putInt("parkingID", Integer.parseInt(parkingid));
                editor.commit();
                setText(tvAddress, address);
                totalSpace = space;
                setText(tvSpace, currentBooking + "/" + space);
            } catch (Exception ex) {
                Log.e("Error:", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            new GetBookingTask(pref.getInt("parkingID", 3) + "").execute((Void) null);
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
                System.out.println("list ra listview : " + list);
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

//            if (list != null && list.size() > 0) {
//                System.out.println(list);

                CarAdapter adapter = new CarAdapter(getWindow().getDecorView().getRootView(), MainActivity.this, list);
                lv.setAdapter(adapter);
//            }

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
                btnAcept.setText("VÀO BÃI");
                btnAcept.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        Calendar calendar = Calendar.getInstance();
                        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String checkinTime = dateFormatter.format(calendar.getTime().getTime());
                        BookingDTO b = entry;
                        b.setStatus("2");
                        b.setCarID(pref.getInt("carID",0));
                        b.setCheckinTime(checkinTime.toString());
                        new UpdateBookingTask(b).execute((Void) null);
//                    btnAcept.setText("CHeckOut");
                        onResume();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        BookingDTO b = entry;
                        b.setStatus("0");
                        b.setCarID(pref.getInt("carID",0));
                        new UpdateBookingTask(b).execute((Void) null);
//                    btnAcept.setText("CHeckOut");
                        onResume();
                    }
                });
            } else if (entry.getStatus().equals("2")) {
                btnCancel.setVisibility(convertView.INVISIBLE);
                btnAcept.setText("THANH TOÁN");
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
//                                        dialog.dismiss();
                                        BookingDTO b = entry;
                                        b.setStatus("3");
                                        b.setCarID(pref.getInt("carID",0));
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
                            String totalPrice = formatter.format(diffInHours * entry.getPrice());
                            if(diffInHours<1) {
                                totalPrice = entry.getPrice() +"";
                            }
                            builder.setMessage("\tHóa đơn checkout \n"
                                    + "Biển số :          " + entry.getLicensePlate() + "\n"
                                    + "loại xe :           " + entry.getTypeCar() + "\n"
                                    + "thời gian vào : " + entry.getCheckinTime() + "\n"
                                    + "thời gian ra :   " + checkoutTime + "\n"
                                    + "giá đỗ :           " + formatter.format(entry.getPrice()) + "vnđ\n"
                                    + "thời gian đỗ :  " + formatterHour.format(diffInHours) + " giờ \n"
                                    + "tổng giá :        " + totalPrice + "vnđ")
                                    .setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).setCancelable(false).show();

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
                    createNotification("order","Có 1 Xe mới : " + pref.getString("licensePlate", "") + " muốn đặt chỗ");
                    createDialog("order");
                } else if (action.contains("checkin")) {
                    createNotification("checkin","Xe " + pref.getString("licensePlate", "") + " đã đến và muốn vào bãi");
                    createDialog("checkin");
                } else if (action.contains("checkout")) {
                    createNotification("checkout","Xe " + pref.getString("licensePlate", "") + " muốn thanh toán");
                    createDialog("checkout");


                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    class CreateBookingTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog pdLoading;
        BookingDTO b;
        boolean success = false;

        public CreateBookingTask(BookingDTO b) {
            this.b = b;
            pdLoading = new ProgressDialog(MainActivity.this);
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
                Log.e("Create-Async", b.toString());
                JSONObject formData = new JSONObject();
                formData.put("carID", b.getCarID());
                formData.put("parkingID", b.getParkingID());
//                formData.put("actionspace", "Inc");
                System.out.println(formData.toString());
                String json = httpHandler.post(Constants.API_URL + "booking/create_BookingInfor.php", formData.toString());
//                httpHandler.post(Constants.API_URL + "booking/update_BookingSpace.php", formData.toString());

                System.out.println("=============================");
                System.out.println("json tạo : " + json);
                System.out.println("=============================");

                JSONObject jsonObj = new JSONObject(json);
                if (jsonObj.getInt("size") > 0) {
                    success = true;
                }

            } catch (Exception ex) {
                Log.e("Error tạo :", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            pdLoading.dismiss();
            onResume();
        }

    }

    class UpdateBookingTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog pdLoading;
        BookingDTO b;
        boolean success = false;

        public UpdateBookingTask(BookingDTO bookingDTO) {
            this.b = bookingDTO;
            pdLoading = new ProgressDialog(MainActivity.this);
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
                Log.e("Update-Async", b.toString());
                JSONObject formData = new JSONObject();
                formData.put("bookingID", b.getBookingID());
                formData.put("status", b.getStatus());
//                Calendar calendar = Calendar.getInstance();
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String checkinTime = dateFormatter.format(calendar.getTime().getTime());
                if (b.getStatus().equals("2")) {
//                    b.setCheckinTime(checkinTime);
                    formData.put("checkinTime", b.getCheckinTime());
                    formData.put("carID", b.getCarID());
                    formData.put("actioncheck", "checkin");
                    formData.put("actionspace", "Inc");
                    currentBooking = (Integer.parseInt(currentBooking)+1) +"";
                } else if (b.getStatus().equals("3")) {
//                    String checkoutTime = dateFormatter.format(calendar.getTime().getTime());
//                    b.setCheckoutTime(checkoutTime);
                    formData.put("checkoutTime", b.getCheckoutTime());
//                    formData.put("carID", b.getCarID());
                    formData.put("actioncheck", "checkout");
                    formData.put("actionspace", "Desc");
                    currentBooking = (Integer.parseInt(currentBooking)-1) +"";
                } else if(b.getStatus().equals("0")) {
                    formData.put("actioncheck", "cancel");
                    httpHandler.post(Constants.API_URL + "booking/update_BookingInfor.php", formData.toString());
                    return false;
                }
                formData.put("carID", b.getCarID());
                formData.put("licensePlate", b.getLicensePlate());
                formData.put("type", b.getTypeCar());
//            editor.putInt("bookingID", b.getBookingID());


                Date date1 = dateFormatter.parse(b.getCheckinTime());
                if (b.getStatus().equals("3")) {
                    Date date2 = dateFormatter.parse(b.getCheckoutTime());
                    long diff = date2.getTime() - date1.getTime();
                    double diffInHours = diff / ((double) 1000 * 60 * 60);
                    NumberFormat formatter = new DecimalFormat("###,###");
                    NumberFormat formatterHour = new DecimalFormat("0.00");
                    formData.put("price", formatter.format(b.getPrice()) + "vnđ");
                    formData.put("hours", formatterHour.format(diffInHours) + " giờ \n");
                    formData.put("totalPrice", formatter.format(diffInHours * b.getPrice()) + "vnđ");
                }
                editor.putString("checkinTime", b.getCheckinTime());
                editor.putString("checkoutTime", b.getCheckoutTime());

                editor.commit();
                System.out.println(formData.toString());
                String json = httpHandler.post(Constants.API_URL + "booking/update_BookingInfor.php", formData.toString());
                formData.put("parkingID", pref.getInt("parkingID", 3));
                httpHandler.post(Constants.API_URL + "booking/update_BookingSpace.php", formData.toString());
                System.out.println("=============================");
                System.out.println(json);
                System.out.println("=============================");
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
    class UpdateBooingSpaceTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog pdLoading;
        String action,space;
        boolean success = false;

        public UpdateBooingSpaceTask(String space, String action) {
            this.action = action;
            this.space = space;
            pdLoading = new ProgressDialog(MainActivity.this);
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
                Log.e("Update-Async space ", space);
                JSONObject formData = new JSONObject();
                formData.put("actionspace", action);
                formData.put("space", space);
                formData.put("parkingID", pref.getInt("parkingID",0));
//                formData.put("actionspace", "Inc");
                System.out.println(formData.toString());
                String json = httpHandler.post(Constants.API_URL + "booking/update_BookingSpace.php", formData.toString());

                System.out.println("=============================");
                System.out.println("json update space : " + json);
                System.out.println("=============================");

                JSONObject jsonObj = new JSONObject(json);
                if (jsonObj.getInt("size") > 0) {
                    success = true;
                }

            } catch (Exception ex) {
                Log.e("Error tạo :", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            pdLoading.dismiss();
            onResume();
        }

    }
    public void createNotification(String action,String title) {
        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra("NotificationMessage", "order");
        if(action.equals("order")) {
            intent.putExtra("NotificationMessage", "order");
        }else if(action.equals("checkin")) {
            intent.putExtra("NotificationMessage", "checkin");
        }else if(action.equals("checkout")) {
            intent.putExtra("NotificationMessage", "checkout");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

//
//        // Build notification
//        // Actions are just fake
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText("Hãy xác nhận yêu cầu").setSmallIcon(R.drawable.apply)
                .setAutoCancel(true).setPriority(2)
                .setContentIntent(pIntent)
                .addAction(R.drawable.apply, "Đồng ý", pIntent)
                .addAction(R.drawable.apply, "Hủy", pIntent).setColor(Color.GREEN);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        notificationManager.notify(0, mBuilder.build());

    }

    public void createDialog(String action) {
        if (action.equals("order")) {
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
                            BookingDTO bb = new BookingDTO(pref.getInt("bookingID", 0), pref.getInt("parkingID", 0), pref.getInt("carID", 0), "0", "", "", "", "", 5000);
                            new UpdateBookingTask(bb).execute((Void) null);
                            break;
                    }
                }
            };
            try {
                builder.setMessage("Xe " + pref.getString("licensePlate", "") + "muốn đỗ xe trong bãi bạn có đồng ý không")
                        .setPositiveButton("Có", dialogClickListener)
                        .setNegativeButton("Không", dialogClickListener).setCancelable(false).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (action.equals("checkin")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int choice) {
                    switch (choice) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Calendar calendar = Calendar.getInstance();
                            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            final String checkinTime = dateFormatter.format(calendar.getTime().getTime());
                            editor.putString("checkinTime",checkinTime);
                            editor.putString("status", "2");
                            editor.commit();
                            BookingDTO b = new BookingDTO(pref.getInt("bookingID", 0), pref.getInt("parkingID", 0), pref.getInt("carID", 0), pref.getString("status", ""), pref.getString("checkinTime",""), "", "", "", 5000);
                            new UpdateBookingTask(b).execute((Void) null);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            BookingDTO bb = new BookingDTO(pref.getInt("bookingID", 0), pref.getInt("parkingID", 0), pref.getInt("carID", 0), "0", "", "", "", "", 5000);
                            new UpdateBookingTask(bb).execute((Void) null);
                            break;
                    }
                }
            };

            try {
                builder.setMessage("Xe " + pref.getString("licensePlate", "") + "muốn vào bãi bạn có đồng ý không")
                        .setPositiveButton("Có", dialogClickListener)
                        .setNegativeButton("Không", dialogClickListener).setCancelable(false).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (action.equals("checkout")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int choice) {
                    switch (choice) {
                        case DialogInterface.BUTTON_POSITIVE:
//                            dialog.dismiss();
                            Calendar calendar = Calendar.getInstance();
                            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            final String checkoutTime = dateFormatter.format(calendar.getTime().getTime());
                            DialogInterface.OnClickListener dialogClickListener2 = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int choice) {
                                    switch (choice) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            editor.putString("status", "3");
                                            editor.commit();
//                                            bookingDTO.setCheckoutTime(checkoutTime.toString());
                                            BookingDTO b = new BookingDTO(pref.getInt("bookingID", 0), pref.getInt("parkingID", 0), pref.getInt("carID", 0), pref.getString("status", ""), pref.getString("checkinTime", ""), checkoutTime, pref.getString("licensePlate", ""), pref.getString("type", ""), Double.parseDouble(pref.getString("price", "")));
                                            new UpdateBookingTask(b).execute((Void) null);

//                                            new ManagerBookingTask("update", getApplicationContext(), getWindow().getDecorView().getRootView(), parkingID, MainActivity.this, lv, bookingDTO);
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            BookingDTO bb = new BookingDTO(pref.getInt("bookingID", 0), pref.getInt("parkingID", 0), pref.getInt("carID", 0), "0", "", "", "", "", 5000);
                                            new UpdateBookingTask(bb).execute((Void) null);
                                            break;
                                    }
                                }
                            };
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                            try {
                                Date date1 = dateFormatter.parse(pref.getString("checkinTime", ""));
                                Date date2 = dateFormatter.parse(checkoutTime);
                                long diff = date2.getTime() - date1.getTime();
                                double diffInHours = diff / ((double) 1000 * 60 * 60);
                                NumberFormat formatter = new DecimalFormat("###,###");
                                NumberFormat formatterHour = new DecimalFormat("0.00");
//                                    System.out.println(formatter.format(4.0));
                                builder2.setMessage("\tHóa đơn checkout \n"
                                        + "Biển số :          " + pref.getString("licensePlate", "") + "\n"
                                        + "loại xe :           " + pref.getString("type", "") + "\n"
                                        + "thời gian vào : " + pref.getString("checkinTime", "") + "\n"
                                        + "thời gian ra :   " + checkoutTime + "\n"
                                        + "giá đỗ :           " + formatter.format(Double.parseDouble(pref.getString("price", ""))) + "vnđ\n"
                                        + "thời gian đỗ :  " + formatterHour.format(diffInHours) + " giờ \n"
                                        + "tổng giá :        " + formatter.format(diffInHours * Double.parseDouble(pref.getString("price", ""))) + "vnđ")
                                        .setPositiveButton("Yes", dialogClickListener2)
                                        .setNegativeButton("No", dialogClickListener2).setCancelable(false).show();

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            BookingDTO bb = new BookingDTO(pref.getInt("bookingID", 0), pref.getInt("parkingID", 0), pref.getInt("carID", 0), "0", "", "", "", "", 5000);
                            new UpdateBookingTask(bb).execute((Void) null);
                            break;
                    }
                }
            };


            try {
                builder.setMessage("Xe " + pref.getString("licensePlate", "") + "muốn thanh toán bạn có đồng ý không")
                        .setPositiveButton("Có", dialogClickListener)
                        .setNegativeButton("Không", dialogClickListener).setCancelable(false).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void changeSpaceButtonClick(View view) {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.change_space, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);
        userInput.setHint(currentBooking);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Đồng ý",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                System.out.println(userInput.getText().toString());
                                new UpdateBooingSpaceTask(userInput.getText().toString(),"Manul").execute((Void)null);
                                onResume();
//                                result.setText(userInput.getText());
                            }
                        })
                .setNegativeButton("Hủy",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
