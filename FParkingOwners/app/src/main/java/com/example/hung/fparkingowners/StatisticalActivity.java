package com.example.hung.fparkingowners;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hung.fparkingowners.config.Constants;
import com.example.hung.fparkingowners.dto.BookingDTO;

import org.json.JSONArray;
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

public class StatisticalActivity extends AppCompatActivity {
    private static final String TAG = "Statistical";
    ListView lv;
    private TextView textViewFromDate, textViewToDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener1;
    private DatePickerDialog.OnDateSetListener mDateSetListener2;
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistical);
        getSupportActionBar().hide();
        lv = (ListView) findViewById(R.id.cars_list2);
        textViewFromDate = findViewById(R.id.txtFromDate);
        textViewToDate = findViewById(R.id.txtToDate);
        textViewToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        StatisticalActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener2, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });
        textViewFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        StatisticalActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener1, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });
        mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: yyy/mm/dd: " + year + "-" + month + "-" + day);

                String date = year + "-" + month + "-" + day;
                textViewFromDate.setText(date);
            }
        };
        mDateSetListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: yyy/mm/dd: " + year + "-" + month + "-" + day);

                String date = year + "-" + month + "-" + day;
                textViewToDate.setText(date);
                String temp = "2017-6-10";
                Calendar calendar = Calendar.getInstance();
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String checkinTime = dateFormatter.format(calendar.getTime().getTime());
//                System.out.println("date format = " + checkinTime);
//                System.out.println("temp = " + dateFormatter.format(temp));
////                System.out.println(fromDate);
////                System.out.println(toDate);
//                String fromDate = dateFormatter.format(textViewFromDate.getText().toString());
//                String toDate = dateFormatter.format(textViewToDate.getText().toString());


                new GetBookingTask("3").execute((Void) null);


            }
        };


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
            pdLoading = new ProgressDialog(StatisticalActivity.this);
            //this method will be running on UI thread
            pdLoading.setMessage("\tĐợi xíu...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                    String licensePlate = oneBooking.getString("licensePlate");
                    String type = oneBooking.getString("type");
                    String checkinTime = oneBooking.getString("checkinTime");
                    String checkoutTime = oneBooking.getString("checkoutTime");

                    String price = oneBooking.getString("price");
                    BookingDTO b = new BookingDTO(Integer.parseInt(bookingID), Integer.parseInt(txtSearch), 0, status, checkinTime, checkoutTime, licensePlate, type, Double.parseDouble(price), 0);
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

            StatisticalActivity.CarAdapter adapter = new StatisticalActivity.CarAdapter(getWindow().getDecorView().getRootView(), StatisticalActivity.this, list);
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
                convertView = inflater.inflate(R.layout.cars_list2, null);
            }
            TextView tvBienso = (TextView) convertView.findViewById(R.id.tvBienso2);
            TextView money = (TextView) convertView.findViewById(R.id.tvmoney);
            TextView checkinTime = (TextView) convertView.findViewById(R.id.tvCheckin);
            TextView checkoutTime = (TextView) convertView.findViewById(R.id.tvCheckout);


            tvBienso.setText(entry.getLicensePlate());
            money.setText(entry.getPayment()+"");
            checkinTime.setText(entry.getCheckinTime());
            checkoutTime.setText(entry.getCheckoutTime());
            return convertView;
        }
    }
}