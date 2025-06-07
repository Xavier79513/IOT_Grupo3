package com.example.telehotel.data.model;

public class Aeropuerto {
    private String aeropuertoId;
    private String nombreAeropuerto;

    // Constructor vacío (requerido por Firestore)
    public Aeropuerto() {}

    // Constructor con parámetros
    public Aeropuerto(String aeropuertoId, String nombreAeropuerto) {
        this.aeropuertoId = aeropuertoId;
        this.nombreAeropuerto = nombreAeropuerto;
    }

    public String getAeropuertoId() {
        return aeropuertoId;
    }

    public void setAeropuertoId(String aeropuertoId) {
        this.aeropuertoId = aeropuertoId;
    }

    public String getNombreAeropuerto() {
        return nombreAeropuerto;
    }

    public void setNombreAeropuerto(String nombreAeropuerto) {
        this.nombreAeropuerto = nombreAeropuerto;
    }
}
