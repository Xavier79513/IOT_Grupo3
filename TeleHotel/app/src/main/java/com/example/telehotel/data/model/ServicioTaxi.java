package com.example.telehotel.data.model;

public class ServicioTaxi {
    private String id;
    private String clienteId;
    private String taxistaId;
    private String fechaInicio;
    private String horaInicio;
    private String fechaFin;
    private String horaFin;
    private String hotelId;
    private String aeropuertoId;

    private String estado;
    private Ubicacion ubicacionTaxista;
    private String qrCodigo;

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public ServicioTaxi() {
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

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getAeropuertoId() {
        return aeropuertoId;
    }

    public void setAeropuertoId(String aeropuertoDestino) {
        this.aeropuertoId = aeropuertoDestino;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Ubicacion getUbicacionTaxista() {
        return ubicacionTaxista;
    }

    public void setUbicacionTaxista(Ubicacion ubicacionTaxista) {
        this.ubicacionTaxista = ubicacionTaxista;
    }

    public String getQrCodigo() {
        return qrCodigo;
    }

    public void setQrCodigo(String qrCodigo) {
        this.qrCodigo = qrCodigo;
    }

}
