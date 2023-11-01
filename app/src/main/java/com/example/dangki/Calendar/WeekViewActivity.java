package com.example.dangki.Calendar;


import static android.content.ContentValues.TAG;
import static com.example.dangki.Calendar.CalendarUtils.daysInWeekArray;
import static com.example.dangki.Calendar.CalendarUtils.monthYearFromDate;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class WeekViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);
        initWidgets();
        setWeekView();
    }

    private void initWidgets()
    {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        eventListView = findViewById(R.id.eventListView);
    }

    private void setWeekView()
    {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        setEventAdpater();
    }


    public void previousWeekAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    public void nextWeekAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    @Override
    public void onItemClick(int position, LocalDate date)
    {
        CalendarUtils.selectedDate = date;
        setWeekView();
    }



    @Override
    protected void onResume()
    {
        super.onResume();
        setEventAdpater();
    }

    private void setEventAdpater()
    {
        ArrayList<Event> dailyEvents = Event.eventsForDate(CalendarUtils.selectedDate);
        EventAdapter eventAdapter = new EventAdapter(getApplicationContext(), dailyEvents);
        eventListView.setAdapter(eventAdapter);
    }

    public void newEventAction(View view)
    {
        startActivity(new Intent(this, EventEditActivity.class));

//        Event newEvent = new Event(eventName, CalendarUtils.selectedDate, time);
//        Event.eventsList.add(newEvent);
    }

    public void dailyAction(View view) {
        startActivity(new Intent(this, DailyCalendarActivity.class));

    }
    void GetData(){
        Event.eventsList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Stadium_Rental")
                .whereEqualTo("stadium_id", "1")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Lấy dữ liệu từ mỗi document trong collection
                                String id = document.getId();
                                Date start_time = document.getDate("start_time");
                                Date end_time = document.getDate("end_time");

                                LocalDate localDate = start_time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                LocalTime localTime = start_time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

                                Event newEvent = new Event(id, CalendarUtils.selectedDate, localTime);
                                Event.eventsList.add(newEvent);
                                // ...
                                // Tiếp tục xử lý dữ liệu của mỗi document
                            }
                            monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
                            ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);

                            CalendarAdapter calendarAdapter = new CalendarAdapter(days, WeekViewActivity.this);
                            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
                            calendarRecyclerView.setLayoutManager(layoutManager);
                            calendarRecyclerView.setAdapter(calendarAdapter);
                            setEventAdpater();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
//        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
//        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);
//
//        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
//        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
//        calendarRecyclerView.setLayoutManager(layoutManager);
//        calendarRecyclerView.setAdapter(calendarAdapter);
//        setEventAdpater();
    }
}

