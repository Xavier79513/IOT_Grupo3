package com.example.telehotel.data.model;

public class Capacidad {
    private Integer adultos;
    private Integer ninos;

    // Constructores
    public Capacidad() {}

    public Capacidad(Integer adultos, Integer ninos) {
        this.adultos = adultos;
        this.ninos = ninos;
    }

    // Getters y Setters
    public Integer getAdultos() { return adultos; }
    public void setAdultos(Integer adultos) { this.adultos = adultos; }

    public Integer getNinos() { return ninos; }
    public void setNinos(Integer ninos) { this.ninos = ninos; }
}
