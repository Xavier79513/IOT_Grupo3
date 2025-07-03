package com.example.telehotel.data.model;

public class TarjetaGuardada {
    private String id;
    private String numeroTarjetaEncriptado; // Solo últimos 4 dígitos visibles
    private String tipoTarjeta; // VISA, MasterCard, etc.
    private String fechaVencimiento;
    private String titular;
    private String ultimosCuatroDigitos;
    private boolean esPrincipal;
    private long fechaCreacion;

    // Constructor vacío
    public TarjetaGuardada() {
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Constructor con parámetros
    public TarjetaGuardada(String numeroTarjeta, String tipoTarjeta, String fechaVencimiento, String titular) {
        this.tipoTarjeta = tipoTarjeta;
        this.fechaVencimiento = fechaVencimiento;
        this.titular = titular;
        this.ultimosCuatroDigitos = numeroTarjeta.length() >= 4 ?
                numeroTarjeta.substring(numeroTarjeta.length() - 4) : numeroTarjeta;
        this.numeroTarjetaEncriptado = encriptarNumeroTarjeta(numeroTarjeta);
        this.fechaCreacion = System.currentTimeMillis();
        this.id = "card_" + System.currentTimeMillis();
    }

    // Método simple de "encriptación" (solo para demo, en producción usar cifrado real)
    private String encriptarNumeroTarjeta(String numero) {
        // En producción, usar AES o similar
        // Por ahora solo guardamos los últimos 4 dígitos
        return "**** **** **** " + ultimosCuatroDigitos;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumeroTarjetaEncriptado() { return numeroTarjetaEncriptado; }
    public void setNumeroTarjetaEncriptado(String numeroTarjetaEncriptado) { this.numeroTarjetaEncriptado = numeroTarjetaEncriptado; }

    public String getTipoTarjeta() { return tipoTarjeta; }
    public void setTipoTarjeta(String tipoTarjeta) { this.tipoTarjeta = tipoTarjeta; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public String getUltimosCuatroDigitos() { return ultimosCuatroDigitos; }
    public void setUltimosCuatroDigitos(String ultimosCuatroDigitos) { this.ultimosCuatroDigitos = ultimosCuatroDigitos; }

    public boolean isEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(boolean esPrincipal) { this.esPrincipal = esPrincipal; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // Método para mostrar en UI
    public String getDisplayText() {
        return tipoTarjeta + " **** " + ultimosCuatroDigitos;
    }
}