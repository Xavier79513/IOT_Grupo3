package com.example.telehotel.data.model;

public class Reserva {
    private String id;
    private String clienteId;
    private String hotelId;
    private String habitacionId;
    private String fechaCheckin;
    private String fechaCheckout;
    private String estado;
    private Pago pago;
    private Valoracion valoracion;
    private String qrCheckout;

    public Reserva() {}
    // Getters y setters
}
