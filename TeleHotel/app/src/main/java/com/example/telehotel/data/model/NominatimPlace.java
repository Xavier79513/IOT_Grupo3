package com.example.telehotel.data.model;

import com.google.gson.annotations.SerializedName;

public class NominatimPlace {
    @SerializedName("display_name")
    private String displayName;

    @SerializedName("lat")
    private String lat;

    @SerializedName("lon")
    private String lon;

    @SerializedName("place_id")
    private String placeId;

    @SerializedName("type")
    private String type;

    @SerializedName("class")
    private String placeClass;

    // Constructor vacío
    public NominatimPlace() {}

    // Constructor con parámetros básicos
    public NominatimPlace(String displayName, String lat, String lon) {
        this.displayName = displayName;
        this.lat = lat;
        this.lon = lon;
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getType() {
        return type;
    }

    public String getPlaceClass() {
        return placeClass;
    }

    // Setters (necesarios para el fallback local)
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPlaceClass(String placeClass) {
        this.placeClass = placeClass;
    }

    // Métodos utilitarios
    public double getLatitude() {
        try {
            return Double.parseDouble(lat);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getLongitude() {
        try {
            return Double.parseDouble(lon);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public boolean hasValidCoordinates() {
        return lat != null && lon != null &&
                !lat.trim().isEmpty() && !lon.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "NominatimPlace{" +
                "displayName='" + displayName + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NominatimPlace that = (NominatimPlace) obj;
        return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;
    }

    @Override
    public int hashCode() {
        return displayName != null ? displayName.hashCode() : 0;
    }
}