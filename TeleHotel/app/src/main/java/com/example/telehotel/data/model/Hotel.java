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

    public Hotel() {}
    // Getters y setters
}
