package com.example.telehotel.data.model;

import androidx.room.Ignore;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Hotel {

    private String id;
    private String nombre;
    private String descripcion;
    private Ubicacion ubicacion;
    private String servicioId;

    private List<String> imagenes = new ArrayList<>();
    //private List<String> servicios = new ArrayList<>();  // Servicios del hotel, ej: WiFi, Piscina...
    private List<LugarCercano> lugaresCercanos = new ArrayList<>();

    private String administradorId;
    private String estado;

    private Double montoMinimoTaxi;
    private Boolean permiteReservas;
    private String telefono;
    //@Ignore
    //private List<Servicio> servicios;
    // ✅ CAMBIO CLAVE: servicios como List<String> (IDs), NO List<Servicio>
    private List<String> servicios;

    private List<LugarHistorico> lugaresHistoricos;
    private Timestamp fechaCreacion;
    private Timestamp fechaActualizacion;


    private List<Habitacion> habitaciones = new ArrayList<>(); // Habitaciones embebidas

    public Hotel() {
        // Constructor vacío requerido por Firestore
    }

    public Hotel(String id, String nombre, Ubicacion ubicacion, List<String> imagenes, List<String> servicios,
                 List<LugarCercano> lugaresCercanos, String administradorId, String estado,
                 String descripcion, Double montoMinimoTaxi, Boolean permiteReservas,
                 String horaCheckIn, String horaCheckOut, String telefono, String servicioId) {
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
        this.servicioId = servicioId;
    }

    // Getters y Setters

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }

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

    public String getServicioId() { return servicioId; }
    public void setServicioId(String id) { this.servicioId = servicioId; }
    /*@Ignore
    public List<Servicio> getServicios() { return servicios; }

    @Ignore
    public void setServicios(List<Servicio> servicios) { this.servicios = servicios; }*/
    @Override
    public String toString() {
        return nombre != null ? nombre : "Hotel sin nombre";
    }

    // Método para verificar si tiene administrador
    public boolean tieneAdministrador() {
        return administradorId != null && !administradorId.isEmpty();
    }

    public List<LugarHistorico> getLugaresHistoricos() {
        return lugaresHistoricos;
    }

    public void setLugaresHistoricos(List<LugarHistorico> lugaresHistoricos) {
        this.lugaresHistoricos = lugaresHistoricos;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Timestamp getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Timestamp fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}