package com.example.dangki.Model;

public class PurchasedService {
    private String id;
    private String name;
    private String type;
    String img_url;
    private double price;
    int quantity;

    public PurchasedService(String id, String name, String type, String img_url,
                            double price, int quantity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.img_url = img_url;
        this.price = price;
        this.quantity = quantity;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}

