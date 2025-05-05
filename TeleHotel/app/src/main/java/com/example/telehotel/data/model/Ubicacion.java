package com.example.telehotel.data.model;

public class Ubicacion {
    private String direccion;
    private double lat;
    private double lng;

    public Ubicacion(String direccion, double lat, double lng) {
        this.direccion = direccion;
        this.lat = lat;
        this.lng = lng;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}

