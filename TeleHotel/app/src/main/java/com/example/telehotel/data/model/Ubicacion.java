package com.example.telehotel.data.model;

import java.time.LocalDateTime;

public class Ubicacion {
    private String direccion;
    private String ciudad;
    private String pais;
    private String provincia;
    private String codigoPostal;
    private Double latitud;
    private Double longitud;
    private String timestamp;
    //timestamp puede que en firebase sea string
    // Falta esto:
    public Ubicacion() {}

    public Ubicacion(String direccion, double lat, double lng) {
        this.direccion = direccion;
        this.latitud = lat;
        this.longitud = lng;
    }
    public Ubicacion(String direccion, String ciudad, String pais, Double latitud, Double longitud) {
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
}

