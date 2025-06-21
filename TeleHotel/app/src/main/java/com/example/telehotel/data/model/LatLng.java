package com.example.telehotel.data.model;

public class LatLng {
    public final double latitude;
    public final double longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "LatLng{" +
                "lat=" + latitude +
                ", lng=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LatLng latLng = (LatLng) o;
        return Double.compare(latLng.latitude, latitude) == 0 &&
                Double.compare(latLng.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(latitude);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}