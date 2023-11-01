package com.example.dangki.Model;

public class DoUong {
    String id, name, img_url;
    double price;
    int remain;

    boolean isDelete;

    public DoUong() {
    }

    public DoUong(String id, String name, String img_url, double price, int remain, boolean isDelete) {
        this.id = id;
        this.name = name;
        this.img_url = img_url;
        this.price = price;
        this.remain = remain;
        this.isDelete =isDelete;
    }

    public DoUong(String name, String img_url, double price, int remain, boolean isDelete) {
        this.name = name;
        this.img_url = img_url;
        this.price = price;
        this.remain = remain;
        this.isDelete = isDelete;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getRemain() {
        return remain;
    }

    public void setRemain(int remain) {
        this.remain = remain;
    }


}
