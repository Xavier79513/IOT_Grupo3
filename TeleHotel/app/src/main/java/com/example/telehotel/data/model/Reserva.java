package com.example.telehotel.data.model;

import android.media.Image;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    public String id;
    public String clienteId;
    public String hotelId;
    public String habitacionId;
    public LocalDate fechaEntrada;
    public LocalDate fechaSalida;
    public List<ServicioAdicional> serviciosAdicionales = new ArrayList<>();
    public Double montoTotal;
    public String estado;
    public LocalDateTime fechaReserva;
    public String tarjetaUltimosDigitos;
    public String tipoTarjeta;
    public DatosCheckout checkout;
    public SolicitudTaxi solicitudTaxi;

    public Reserva() {
        this.fechaReserva = LocalDateTime.now();
        this.solicitudTaxi = new SolicitudTaxi();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getHabitacionId() {
        return habitacionId;
    }

    public void setHabitacionId(String habitacionId) {
        this.habitacionId = habitacionId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public List<ServicioAdicional> getServiciosAdicionales() {
        return serviciosAdicionales;
    }

    public void setServiciosAdicionales(List<ServicioAdicional> serviciosAdicionales) {
        this.serviciosAdicionales = serviciosAdicionales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getTarjetaUltimosDigitos() {
        return tarjetaUltimosDigitos;
    }

    public void setTarjetaUltimosDigitos(String tarjetaUltimosDigitos) {
        this.tarjetaUltimosDigitos = tarjetaUltimosDigitos;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public DatosCheckout getCheckout() {
        return checkout;
    }

    public void setCheckout(DatosCheckout checkout) {
        this.checkout = checkout;
    }

    public SolicitudTaxi getSolicitudTaxi() {
        return solicitudTaxi;
    }

    public void setSolicitudTaxi(SolicitudTaxi solicitudTaxi) {
        this.solicitudTaxi = solicitudTaxi;
    }

}
