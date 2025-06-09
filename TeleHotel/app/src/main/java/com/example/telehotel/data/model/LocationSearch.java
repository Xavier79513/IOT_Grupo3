package com.example.telehotel.data.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Modelo para representar ubicaciones únicas para el buscador
 */
public class LocationSearch implements Serializable {
    private String ciudad;
    private String pais;
    private String displayName; // "Ciudad, País"
    private int hotelCount; // Cantidad de hoteles en esta ubicación

    public LocationSearch() {}

    public LocationSearch(String ciudad, String pais) {
        this.ciudad = ciudad;
        this.pais = pais;
        this.displayName = ciudad + ", " + pais;
        this.hotelCount = 0;
    }

    public LocationSearch(String ciudad, String pais, int hotelCount) {
        this.ciudad = ciudad;
        this.pais = pais;
        this.displayName = ciudad + ", " + pais;
        this.hotelCount = hotelCount;
    }

    // Getters y Setters
    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
        updateDisplayName();
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
        updateDisplayName();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getHotelCount() {
        return hotelCount;
    }

    public void setHotelCount(int hotelCount) {
        this.hotelCount = hotelCount;
    }

    private void updateDisplayName() {
        if (ciudad != null && pais != null) {
            this.displayName = ciudad + ", " + pais;
        }
    }

    // Incrementar contador de hoteles
    public void incrementHotelCount() {
        this.hotelCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationSearch that = (LocationSearch) o;
        return Objects.equals(ciudad, that.ciudad) && Objects.equals(pais, that.pais);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ciudad, pais);
    }

    @Override
    public String toString() {
        return displayName + " (" + hotelCount + " hoteles)";
    }
}