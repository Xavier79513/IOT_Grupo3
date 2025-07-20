package com.example.telehotel.data.model;

public class Viaje {

    private String id; // opcional si usas Firestore
    private String clienteId;
    private String taxistaId;

    private Ubicacion puntoInicio;
    private Ubicacion puntoDestino;

    private String estado; // solicitado, asignado, en_camino, en_viaje, completado, cancelado

    private String fechaSolicitud;
    private String fechaInicio;
    private String fechaFin;

    private double costoEstimado;
    private double distanciaKm;

    // Calificaciones opcionales
    private Double calificacionCliente;
    private Double calificacionTaxista;

    public Viaje() {
        // Constructor vacío necesario para Firebase
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

    public String getTaxistaId() {
        return taxistaId;
    }

    public void setTaxistaId(String taxistaId) {
        this.taxistaId = taxistaId;
    }

    public Ubicacion getPuntoInicio() {
        return puntoInicio;
    }

    public void setPuntoInicio(Ubicacion puntoInicio) {
        this.puntoInicio = puntoInicio;
    }

    public Ubicacion getPuntoDestino() {
        return puntoDestino;
    }

    public void setPuntoDestino(Ubicacion puntoDestino) {
        this.puntoDestino = puntoDestino;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public double getCostoEstimado() {
        return costoEstimado;
    }

    public void setCostoEstimado(double costoEstimado) {
        this.costoEstimado = costoEstimado;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Double getCalificacionTaxista() {
        return calificacionTaxista;
    }

    public void setCalificacionTaxista(Double calificacionTaxista) {
        this.calificacionTaxista = calificacionTaxista;
    }

    public Double getCalificacionCliente() {
        return calificacionCliente;
    }

    public void setCalificacionCliente(Double calificacionCliente) {
        this.calificacionCliente = calificacionCliente;
    }
}
