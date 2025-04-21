package com.example.telehotel.features.admin;

public class Service {
    public final int iconRes;
    public final String name, desc, price;

    public Service(int iconRes, String name, String desc, String price) {
        this.iconRes = iconRes;
        this.name    = name;
        this.desc    = desc;
        this.price   = price;
    }
}
