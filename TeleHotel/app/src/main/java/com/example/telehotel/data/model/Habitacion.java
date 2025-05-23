package com.example.telehotel.data.model;

import java.util.ArrayList;
import java.util.List;

public class Habitacion {
    public String id;
    public String hotelId;
    public String numero;
    public String tipo;
    public Capacidad capacidad;
    public Double tamaño;
    public Double precio;
    public String descripcion;
    public List<String> fotos = new ArrayList<>();
    public String estado;
    public List<String> serviciosIncluidos = new ArrayList<>();


}

