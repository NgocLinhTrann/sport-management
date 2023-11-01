package com.example.dangki.Calendar;


import java.util.Date;

public class CalendarItem {
    private String name;
    private Date startTime;
    private Date endTime;

    public CalendarItem(String name, Date startTime, Date endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}

