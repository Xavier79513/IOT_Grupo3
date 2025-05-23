package com.example.telehotel.data.model;

public class TarjetaCredito {
    private String numeroTarjeta; // Encriptado
    private String tipoTarjeta;
    private String fechaVencimiento;
    private String titular;

    // Constructores
    public TarjetaCredito() {}

    // Getters y Setters
    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    public String getTipoTarjeta() { return tipoTarjeta; }
    public void setTipoTarjeta(String tipoTarjeta) { this.tipoTarjeta = tipoTarjeta; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }
}