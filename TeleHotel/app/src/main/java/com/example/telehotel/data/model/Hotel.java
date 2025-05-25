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
    private String telefono;

    public Hotel() {
        // Constructor vacío requerido por Firestore
    }

    public Hotel(String id, String nombre, Ubicacion ubicacion, List<String> imagenes, List<String> servicios,
                 List<LugarCercano> lugaresCercanos, String administradorId, String estado,
                 String descripcion, Double montoMinimoTaxi, Boolean permiteReservas,
                 String horaCheckIn, String horaCheckOut, String telefono) {
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
        this.telefono = telefono;
    }

    // Getters y Setters

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

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

}
