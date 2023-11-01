package com.example.dangki.Admin.DatLich;

import java.util.Date;

public class DatLichModel {
    String rentalID, userName, tenSan, status, img_url_san;
    Date start_time, end_time;

    public DatLichModel(String rentalID, String userName, String tenSan, String status, String img_url_san, Date start_time, Date end_time) {
        this.rentalID = rentalID;
        this.userName = userName;
        this.tenSan = tenSan;
        this.status = status;
        this.img_url_san = img_url_san;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public String getRentalID() {
        return rentalID;
    }

    public void setRentalID(String rentalID) {
        this.rentalID = rentalID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTenSan() {
        return tenSan;
    }

    public void setTenSan(String tenSan) {
        this.tenSan = tenSan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImg_url_san() {
        return img_url_san;
    }

    public void setImg_url_san(String img_url_san) {
        this.img_url_san = img_url_san;
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
