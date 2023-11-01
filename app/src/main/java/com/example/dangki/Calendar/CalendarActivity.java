package com.example.dangki.Calendar;


import static android.content.ContentValues.TAG;
import static com.example.dangki.Calendar.CalendarUtils.daysInMonthArray;
import static com.example.dangki.Calendar.CalendarUtils.monthYearFromDate;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity  implements CalendarAdapter.OnItemListener{
    static int REQUEST_DATSAN_CODE =1;

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    LocalTime selected_StartTime_final, selected_EndTime_final;
    int gioChoi=0;
    double totalDb=0.0, stadium_price=0.0;
    String sanID="", rentalID="", userID;
    double san_price=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.main_calendar);
        initWidgets();
        CalendarUtils.selectedDate = LocalDate.now();
        setMonthView();

    }

    private void initWidgets()
    {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);

        sanID = getIntent().getStringExtra("idSan_intent");
        san_price = getIntent().getDoubleExtra("stadium_price", 0.0);
        userID = getIntent().getStringExtra("userID");
    }

    private void setMonthView()
    {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray();

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    public void previousMonthAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, LocalDate date)
    {
        Intent intent= new Intent(CalendarActivity.this, DailyCalendarActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("idSan_intent", sanID);
        intent.putExtra("stadium_price", san_price);
        CalendarUtils.selectedDate = date;
        setMonthView();
        startActivityForResult(intent, REQUEST_DATSAN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_DATSAN_CODE && resultCode ==2){
//            totalDb =data.getDoubleExtra("totalDb", 0.0);
//            selected_StartTime_final = LocalTime.parse(data.getStringExtra("start_time"));
//            selected_StartTime_final = LocalTime.parse(data.getStringExtra("end_time"));
//            gioChoi = data.getIntExtra("rental_time", 0);
//
//            Intent intent = new Intent();
//            intent.putExtra("totalDb", totalDb);
//            intent.putExtra("start_time", selected_StartTime_final.toString());
//            intent.putExtra("end_time", selected_EndTime_final.toString());
//            intent.putExtra("rental_time", gioChoi);
//            setResult(RESULT_DATSAN_SUCCESS, intent);
//            finish();
            finish();
        }
    }


    public void weeklyAction(View view)
    {
        startActivity(new Intent(this, WeekViewActivity.class));
    }

}

