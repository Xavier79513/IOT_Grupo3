package com.example.telehotel.data.model;

public class ServicioTaxi {
    private String id;
    private String clienteId;
    private String taxistaId;
    private String hotelId;
    private String aeropuertoDestino;
    private String fechaSolicitud;
    private String estado;
    private Ubicacion ubicacionTaxista;
    private String qrCodigo;

    public ServicioTaxi(String id, String clienteId, String taxistaId, String hotelId, String aeropuertoDestino, String fechaSolicitud, String estado, Ubicacion ubicacionTaxista, String qrCodigo) {
        this.id = id;
        this.clienteId = clienteId;
        this.taxistaId = taxistaId;
        this.hotelId = hotelId;
        this.aeropuertoDestino = aeropuertoDestino;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.ubicacionTaxista = ubicacionTaxista;
        this.qrCodigo = qrCodigo;
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

    public String getAeropuertoDestino() {
        return aeropuertoDestino;
    }

    public void setAeropuertoDestino(String aeropuertoDestino) {
        this.aeropuertoDestino = aeropuertoDestino;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
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
