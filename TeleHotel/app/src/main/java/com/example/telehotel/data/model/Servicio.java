package com.example.telehotel.data.model;

import java.util.ArrayList;
import java.util.List;

/*public class Servicio {
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
}*/
public class Servicio {
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String categoria;
    private boolean activo;
    private String hotelId;
    private String imagenUrl;

    // Constructor vacío requerido para Firestore
    public Servicio() {}

    // Constructor completo
    public Servicio(String nombre, String descripcion, double precio,
                    String categoria, boolean activo, String hotelId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.activo = activo;
        this.hotelId = hotelId;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    // Método utilitario para el precio formateado
    public String getPrecioFormateado() {
        return String.format("S/%.2f", precio);
    }
}

