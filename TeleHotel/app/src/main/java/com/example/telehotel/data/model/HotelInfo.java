// HotelInfo.java - Modelo de información de hotel
package com.example.telehotel.data.model;

import java.io.Serializable;
import java.util.Objects;

public class HotelInfo implements Serializable {
    private String id;
    private String name;
    private String location;
    private String imageUrl;
    private float rating;
    private double minPrice;
    private long timestamp;
    private String description;
    private String address;
    private String phone;
    private String email;
    private boolean hasWifi;
    private boolean hasBreakfast;
    private boolean hasParking;
    private boolean hasPool;
    private boolean hasGym;
    private double minimumTaxiAmount;

    public HotelInfo() {
        this.timestamp = System.currentTimeMillis();
    }

    public HotelInfo(String id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.timestamp = System.currentTimeMillis();
    }

    public HotelInfo(String id, String name, String location, String imageUrl, float rating, double minPrice) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.minPrice = minPrice;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters y Setters
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public boolean isHasBreakfast() {
        return hasBreakfast;
    }

    public void setHasBreakfast(boolean hasBreakfast) {
        this.hasBreakfast = hasBreakfast;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public boolean isHasPool() {
        return hasPool;
    }

    public void setHasPool(boolean hasPool) {
        this.hasPool = hasPool;
    }

    public boolean isHasGym() {
        return hasGym;
    }

    public void setHasGym(boolean hasGym) {
        this.hasGym = hasGym;
    }

    public double getMinimumTaxiAmount() {
        return minimumTaxiAmount;
    }

    public void setMinimumTaxiAmount(double minimumTaxiAmount) {
        this.minimumTaxiAmount = minimumTaxiAmount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HotelInfo hotelInfo = (HotelInfo) obj;
        return Objects.equals(id, hotelInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HotelInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", rating=" + rating +
                ", minPrice=" + minPrice +
                '}';
    }
}