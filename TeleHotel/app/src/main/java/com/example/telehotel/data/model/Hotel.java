package com.example.telehotel.data.model;

import java.util.ArrayList;
import java.util.List;

public class Hotel {

    private String id;
    private String nombre;
    private String descripcion;
    private Ubicacion ubicacion;

    private List<String> imagenes;
    private List<String> servicios;
    private List<LugarCercano> lugaresCercanos;

    private String administradorId;
    private String estado;

    private Double montoMinimoTaxi;
    private Boolean permiteReservas;
    private String horaCheckIn;
    private String horaCheckOut;

    public Hotel() {
        // Valores por defecto
        this.permiteReservas = true;
        this.horaCheckIn = "14:00";
        this.horaCheckOut = "11:00";
        this.imagenes = new ArrayList<>();
        this.servicios = new ArrayList<>();
        this.lugaresCercanos = new ArrayList<>();
    }

    public Hotel(String id, String nombre, Ubicacion ubicacion, List<String> imagenes, List<String> servicios,
                 List<LugarCercano> lugaresCercanos, String administradorId, String estado,
                 String descripcion, Double montoMinimoTaxi, Boolean permiteReservas,
                 String horaCheckIn, String horaCheckOut) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.imagenes = imagenes;
        this.servicios = servicios;
        this.lugaresCercanos = lugaresCercanos;
        this.administradorId = administradorId;
        this.estado = estado;
        this.descripcion = descripcion;
        this.montoMinimoTaxi = montoMinimoTaxi;
        this.permiteReservas = permiteReservas;
        this.horaCheckIn = horaCheckIn;
        this.horaCheckOut = horaCheckOut;
    }

    // Getters y Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }

    public List<String> getServicios() { return servicios; }
    public void setServicios(List<String> servicios) { this.servicios = servicios; }

    public List<LugarCercano> getLugaresCercanos() { return lugaresCercanos; }
    public void setLugaresCercanos(List<LugarCercano> lugaresCercanos) { this.lugaresCercanos = lugaresCercanos; }

    public String getAdministradorId() { return administradorId; }
    public void setAdministradorId(String administradorId) { this.administradorId = administradorId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Double getMontoMinimoTaxi() { return montoMinimoTaxi; }
    public void setMontoMinimoTaxi(Double montoMinimoTaxi) { this.montoMinimoTaxi = montoMinimoTaxi; }

    public Boolean getPermiteReservas() { return permiteReservas; }
    public void setPermiteReservas(Boolean permiteReservas) { this.permiteReservas = permiteReservas; }

    public String getHoraCheckIn() { return horaCheckIn; }
    public void setHoraCheckIn(String horaCheckIn) { this.horaCheckIn = horaCheckIn; }

    public String getHoraCheckOut() { return horaCheckOut; }
    public void setHoraCheckOut(String horaCheckOut) { this.horaCheckOut = horaCheckOut; }
}
