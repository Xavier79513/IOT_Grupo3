package com.example.telehotel.data.model;

import android.media.Image;

import java.io.Serializable;

public class Reserva implements Serializable {
    private String nombreHotel;
    private String fechaReserva;
    private double precio;
    private int adultos;
    private int ninos;
    private int cantidadHabitaciones;
    private String fechaCheckin;
    private String fechaCheckout;
    private double tax;
    private int imagen;
    private String[] alimentos; // Ejemplo: {"Bagels with turkey and bacon", "Sandwich"}
    private double[] preciosAlimentos; // Ejemplo: {10.0, 5.0}


    private ServicioTaxi servicioTaxi;

    public Reserva(String nombreHotel, String fechaReserva, double precio, int imagen) {
        this.nombreHotel = nombreHotel;
        this.fechaReserva = fechaReserva;
        this.precio = precio;
        this.imagen = imagen;

    }

    public Reserva() {

    }

    public void setNombreHotel(String nombreHotel) {
        this.nombreHotel = nombreHotel;
    }

    public void setFechaReserva(String fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public void setAlimentos(String[] alimentos) {
        this.alimentos = alimentos;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setFechaCheckout(String fechaCheckout) {
        this.fechaCheckout = fechaCheckout;
    }

    public void setFechaCheckin(String fechaCheckin) {
        this.fechaCheckin = fechaCheckin;
    }

    public void setCantidadHabitaciones(int cantidadHabitaciones) {
        this.cantidadHabitaciones = cantidadHabitaciones;
    }

    public void setNinos(int ninos) {
        this.ninos = ninos;
    }

    public void setAdultos(int adultos) {
        this.adultos = adultos;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setPreciosAlimentos(double[] preciosAlimentos) {
        this.preciosAlimentos = preciosAlimentos;
    }

    // Getters
    public String getNombreHotel() {
        return nombreHotel;
    }

    public int getImage() {
        return imagen;
    }

    public String getFechaReserva() {
        return fechaReserva;
    }

    public double getPrecio() {
        return precio;
    }

    public int getAdultos() {
        return adultos;
    }

    public int getNinos() {
        return ninos;
    }

    public int getCantidadHabitaciones() {
        return cantidadHabitaciones;
    }

    public String getFechaCheckin() {
        return fechaCheckin;
    }

    public String getFechaCheckout() {
        return fechaCheckout;
    }

    public double getTax() {
        return tax;
    }

    public String[] getAlimentos() {
        return alimentos;
    }

    public double[] getPreciosAlimentos() {
        return preciosAlimentos;
    }

    public double getTotalReserva() {
        return precio + tax;
    }

    public double getTotalComida() {
        double total = 0;
        for (double precio : preciosAlimentos) {
            total += precio;
        }
        return total;
    }

    public ServicioTaxi getServicioTaxi() {
        return servicioTaxi;
    }

    public void setServicioTaxi(ServicioTaxi servicioTaxi) {
        this.servicioTaxi = servicioTaxi;
    }

    public double getServiceTax() {
        return 2.0; // puedes modificarlo si varía
    }

    public double getTotalGeneral() {
        return getTotalComida() + getServiceTax();
    }
}
