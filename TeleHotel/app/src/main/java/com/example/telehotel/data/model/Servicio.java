package com.example.telehotel.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*public class Servicio {
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String categoria;
    private boolean activo;

    // ✅ hotelId como List para manejar múltiples hoteles
    private List<String> hotelId;
    private String imagenUrl;

    // Constructor vacío requerido para Firestore
    public Servicio() {}

    // Constructor para un solo hotel
    public Servicio(String nombre, String descripcion, double precio,
                    String categoria, boolean activo, String hotelId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.activo = activo;
        this.hotelId = Arrays.asList(hotelId);
    }

    // Constructor para múltiples hoteles
    public Servicio(String nombre, String descripcion, double precio,
                    String categoria, boolean activo, List<String> hotelIds) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.activo = activo;
        this.hotelId = hotelIds;
    }

    // Getters y Setters básicos
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

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    // ✅ Métodos para manejar múltiples hoteles
    public List<String> getHotelId() {
        return hotelId != null ? hotelId : new ArrayList<>();
    }

    public void setHotelId(List<String> hotelId) {
        this.hotelId = hotelId;
    }

    // ✅ Métodos utilitarios para hoteles
    public boolean perteneceAlHotel(String hotelIdBuscado) {
        return hotelId != null && hotelId.contains(hotelIdBuscado);
    }

    public void agregarHotel(String nuevoHotelId) {
        if (hotelId == null) {
            hotelId = new ArrayList<>();
        }
        if (!hotelId.contains(nuevoHotelId)) {
            hotelId.add(nuevoHotelId);
        }
    }

    public void removerHotel(String hotelIdARemover) {
        if (hotelId != null) {
            hotelId.remove(hotelIdARemover);
        }
    }

    public int getCantidadHoteles() {
        return hotelId != null ? hotelId.size() : 0;
    }

    public String getPrimerHotelId() {
        return (hotelId != null && !hotelId.isEmpty()) ? hotelId.get(0) : null;
    }

    // Método utilitario para el precio formateado
    public String getPrecioFormateado() {
        return String.format("S/%.2f", precio);
    }

    @Override
    public String toString() {
        return "Servicio{" +
                "nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", hoteles=" + getCantidadHoteles() +
                '}';
    }
}*/

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Servicio {
    private String id;
    private String nombre;
    private String descripcion;
    private Double precio;          // null si es gratuito
    private List<String> imagenes = new ArrayList<>();
    private boolean disponible;     // true/false
    private String hotelId;         // ID del hotel al que pertenece
    private Timestamp fechaCreacion;
    private String creadoPor;

    // Constructor vacío requerido por Firebase
    public Servicio() {}

    public Servicio(String nombre, String descripcion, Double precio,
                    String hotelId, String creadoPor) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.hotelId = hotelId;
        this.disponible = true;
        this.fechaCreacion = Timestamp.now();
        this.creadoPor = creadoPor;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }

    // Métodos de utilidad
    public boolean esGratuito() {
        return precio == null || precio <= 0;
    }

    public String getPrecioFormateado() {
        if (esGratuito()) {
            return "Gratuito";
        }
        return "S/ " + String.format("%.2f", precio);
    }

    public String getEstadoTexto() {
        return disponible ? "Disponible" : "No disponible";
    }

    public boolean tieneImagenes() {
        return imagenes != null && !imagenes.isEmpty();
    }

    public int getCantidadImagenes() {
        return imagenes != null ? imagenes.size() : 0;
    }
}