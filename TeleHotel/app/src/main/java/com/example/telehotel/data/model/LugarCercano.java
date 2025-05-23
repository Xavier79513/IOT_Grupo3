package com.example.telehotel.data.model;

public class LugarCercano {
    private String nombre;
    private String descripcion;
    private Double distancia; // en km
    private Double latitud;
    private Double longitud;

    public LugarCercano() {}

    public LugarCercano(String nombre, String descripcion, Double distancia, Double latitud, Double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.distancia = distancia;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public LugarCercano( String descripcion, String nombre, Double distancia) {
        this.distancia = distancia;
        this.descripcion = descripcion;
        this.nombre = nombre;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getDistancia() { return distancia; }
    public void setDistancia(Double distancia) { this.distancia = distancia; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}
