package com.example.telehotel.data.model;

public class LugarHistorico {
    private String nombre;
    private String descripcion;
    private LatLng ubicacion;
    private float distancia; // en km
    private String tipoLugar;
    private boolean seleccionado;

    public LugarHistorico() {
        // Constructor vacío requerido para Firestore
    }

    public LugarHistorico(String nombre, String descripcion, LatLng ubicacion,
                          float distancia, String tipoLugar) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.distancia = distancia;
        this.tipoLugar = tipoLugar;
        this.seleccionado = false;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LatLng getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(LatLng ubicacion) {
        this.ubicacion = ubicacion;
    }

    public float getDistancia() {
        return distancia;
    }

    public void setDistancia(float distancia) {
        this.distancia = distancia;
    }

    public String getTipoLugar() {
        return tipoLugar;
    }

    public void setTipoLugar(String tipoLugar) {
        this.tipoLugar = tipoLugar;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }
}