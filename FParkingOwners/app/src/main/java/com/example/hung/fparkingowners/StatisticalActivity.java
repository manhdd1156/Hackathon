package com.example.hung.fparkingowners;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

public class StatisticalActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    TextView textViewFromDate, textViewToDate;
    ImageView fromDate, toDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistical);
        getSupportActionBar().hide();
//        fromDate = (ImageView) findViewById(R.id.imgFromDate);
//        toDate = (ImageView) findViewById(R.id.imgToDate);


    }
@Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance(DateFormat.DEFAULT).format(calendar.getTime());
    }
}
