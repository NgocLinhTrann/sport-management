package com.example.dangki.Model;

import java.util.Date;

public class ThongTinDatLichModel {
    String rentalID,img_url, name, status;
    Date start_time, end_time;

    public ThongTinDatLichModel(String rentalID, String img_url, String name, String status, Date start_time, Date end_time) {
        this.rentalID = rentalID;
        this.img_url = img_url;
        this.name = name;
        this.status = status;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public String getRentalID() {
        return rentalID;
    }

    public void setRentalID(String rentalID) {
        this.rentalID = rentalID;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }
}
