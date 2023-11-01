package com.example.dangki.Calendar;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.ViewHolder>{
    private List<CalendarItem> calendarItems;

    public WeekAdapter(List<CalendarItem> calendarItems) {
        this.calendarItems = calendarItems;
    }

    public void setCalendarItems(List<CalendarItem> calendarItems) {
        this.calendarItems = calendarItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarItem calendarItem = calendarItems.get(position);
        holder.eventNameTextView.setText(calendarItem.getName());
        holder.eventTimeTextView.setText(formatTimeRange(calendarItem.getStartTime(), calendarItem.getEndTime()));
    }

    @Override
    public int getItemCount() {
        return calendarItems.size();
    }

    private String formatTimeRange(Date startTime, Date endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(startTime) + " - " + sdf.format(endTime);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView eventNameTextView;
        public TextView eventTimeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventTimeTextView = itemView.findViewById(R.id.eventTimeTextView);
        }
    }
}
