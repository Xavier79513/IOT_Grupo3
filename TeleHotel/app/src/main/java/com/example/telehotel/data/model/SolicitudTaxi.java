package com.example.telehotel.data.model;

public class SolicitudTaxi {
    public Boolean solicitado = false;
    public String aeropuertoDestino;
    public String taxistaAsignadoId;
    public String codigoQR;
    public String estado;

    public SolicitudTaxi() {}

    public Boolean getSolicitado() {
        return solicitado;
    }

    public void setSolicitado(Boolean solicitado) {
        this.solicitado = solicitado;
    }

    public String getAeropuertoDestino() {
        return aeropuertoDestino;
    }

    public void setAeropuertoDestino(String aeropuertoDestino) {
        this.aeropuertoDestino = aeropuertoDestino;
    }

    public String getTaxistaAsignadoId() {
        return taxistaAsignadoId;
    }

    public void setTaxistaAsignadoId(String taxistaAsignadoId) {
        this.taxistaAsignadoId = taxistaAsignadoId;
    }

    public String getCodigoQR() {
        return codigoQR;
    }

    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
