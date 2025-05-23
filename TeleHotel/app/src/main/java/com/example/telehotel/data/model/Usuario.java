package com.example.telehotel.data.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    // Básicos
    private String uid;
    private String email;
    private String role;
    private String estado;

    // Datos personales
    private String nombres;
    private String apellidos;
    private String tipoDocumento;
    private String numeroDocumento;
    private String fechaNacimiento;
    private String correo;
    private String telefono;
    private String direccion;
    private String fotoUrl;
    private String passwordHash;

    // Hotel asignado (si es admin)
    private String hotelId;

    // Datos específicos para taxistas
    private String placaAuto;
    private String fotoAuto;
    private Ubicacion ubicacionActual;
    private int viajesRealizadosHoy;
    private double ingresosDiarios;
    private double reputacion;

    // Datos específicos para clientes
    private List<TarjetaCredito> tarjetasCredito = new ArrayList<>();

    // Constructor vacío requerido por Firebase
    public Usuario() {
    }

    public Usuario(String uid, String email, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.nombres = name;
        this.role = role;
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return nombres; }
    public void setName(String name) { this.nombres = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getPlacaAuto() { return placaAuto; }
    public void setPlacaAuto(String placaAuto) { this.placaAuto = placaAuto; }

    public String getFotoAuto() { return fotoAuto; }
    public void setFotoAuto(String fotoAuto) { this.fotoAuto = fotoAuto; }

    public Ubicacion getUbicacionActual() { return ubicacionActual; }
    public void setUbicacionActual(Ubicacion ubicacionActual) { this.ubicacionActual = ubicacionActual; }

    public int getViajesRealizadosHoy() { return viajesRealizadosHoy; }
    public void setViajesRealizadosHoy(int viajesRealizadosHoy) { this.viajesRealizadosHoy = viajesRealizadosHoy; }

    public double getIngresosDiarios() { return ingresosDiarios; }
    public void setIngresosDiarios(double ingresosDiarios) { this.ingresosDiarios = ingresosDiarios; }

    public double getReputacion() { return reputacion; }
    public void setReputacion(double reputacion) { this.reputacion = reputacion; }

    public List<TarjetaCredito> getTarjetasCredito() { return tarjetasCredito; }
    public void setTarjetasCredito(List<TarjetaCredito> tarjetasCredito) { this.tarjetasCredito = tarjetasCredito; }
}
