package com.example.telehotel.data.model;
import com.example.telehotel.data.model.Ubicacion;

import java.util.List;

public class Hotel {
    private String id;
    private String nombre;
    private Ubicacion ubicacion;
    private List<String> imagenes;
    private List<String> servicios;
    private List<String> lugaresCercanos;
    private String administradorId;
    private String estado;

    public Hotel(String id, String nombre, Ubicacion ubicacion, List<String> imagenes, List<String> servicios, List<String> lugaresCercanos, String administradorId, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.imagenes = imagenes;
        this.servicios = servicios;
        this.lugaresCercanos = lugaresCercanos;
        this.administradorId = administradorId;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public List<String> getServicios() {
        return servicios;
    }

    public void setServicios(List<String> servicios) {
        this.servicios = servicios;
    }

    public List<String> getLugaresCercanos() {
        return lugaresCercanos;
    }

    public void setLugaresCercanos(List<String> lugaresCercanos) {
        this.lugaresCercanos = lugaresCercanos;
    }

    public String getAdministradorId() {
        return administradorId;
    }

    public void setAdministradorId(String administradorId) {
        this.administradorId = administradorId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
