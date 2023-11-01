package com.example.dangki.Model;

public class San {
    String id, img_url, name;
    double price;
    boolean isDelete;

    public San(String id, String img_url, String name, double price, boolean isDelete) {
        this.id = id;
        this.img_url = img_url;
        this.name = name;
        this.price = price;
        this.isDelete = isDelete;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }
}
