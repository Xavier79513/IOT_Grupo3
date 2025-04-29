package com.example.telehotel.features.cliente;

public class Hotel {
    private String nombre;
    private String rating;
    private String descripcion;
    private String descuento;
    private String precio;
    private int imagenId;

    public Hotel(String hotelPerú, String rating, String $99) {
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    private String fecha;

    public Hotel(String nombre, String rating, String descripcion, String descuento, String precio, int imagenId) {
        this.nombre = nombre;
        this.rating = rating;
        this.descripcion = descripcion;
        this.descuento = descuento;
        this.precio = precio;
        this.imagenId =  imagenId;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getRating() { return rating; }
    public String getDescripcion() { return descripcion; }
    public String getDescuento() { return descuento; }
    public String getPrecio() { return precio; }
    public int getImagenId() { return  imagenId; }

    public String getFechaReserva() {
        return "";
    }
}
