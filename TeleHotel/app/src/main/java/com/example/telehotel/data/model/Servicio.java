package com.example.telehotel.data.model;

import java.util.ArrayList;
import java.util.List;

public class Servicio {
    public String id;
    public String hotelId;
    public String nombre;
    public String descripcion;
    public Double precio;
    public String categoria;
    public List<String> imagenes = new ArrayList<>();
    public String estado;
    public Boolean esGratuito;

    public Servicio() {
        this.esGratuito = false;
    }
}

