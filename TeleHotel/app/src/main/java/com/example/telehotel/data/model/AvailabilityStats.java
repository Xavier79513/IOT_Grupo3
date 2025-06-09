package com.example.telehotel.data.model;

/**
 * Clase para estadísticas de disponibilidad de hoteles
 */
public class AvailabilityStats {
    public int totalHoteles = 0;
    public int hotelesActivos = 0;
    public int habitacionesDisponibles = 0;
    public int capacidadMaximaAdultos = 0;
    public int capacidadMaximaNinos = 0;

    // Constructores
    public AvailabilityStats() {}

    public AvailabilityStats(int totalHoteles, int hotelesActivos, int habitacionesDisponibles) {
        this.totalHoteles = totalHoteles;
        this.hotelesActivos = hotelesActivos;
        this.habitacionesDisponibles = habitacionesDisponibles;
    }

    // Métodos de utilidad
    public boolean tieneDisponibilidad() {
        return hotelesActivos > 0 && habitacionesDisponibles > 0;
    }

    public String getResumen() {
        return String.format("%d hoteles activos, %d habitaciones disponibles",
                hotelesActivos, habitacionesDisponibles);
    }

    public String getResumenCompleto() {
        return String.format("Total: %d hoteles | Activos: %d | Habitaciones disponibles: %d | " +
                        "Capacidad máxima: %d adultos, %d niños",
                totalHoteles, hotelesActivos, habitacionesDisponibles,
                capacidadMaximaAdultos, capacidadMaximaNinos);
    }

    public double getPorcentajeHotelesActivos() {
        if (totalHoteles == 0) return 0.0;
        return (double) hotelesActivos / totalHoteles * 100.0;
    }

    public double getPromedioHabitacionesPorHotel() {
        if (hotelesActivos == 0) return 0.0;
        return (double) habitacionesDisponibles / hotelesActivos;
    }

    // Getters y Setters
    public int getTotalHoteles() {
        return totalHoteles;
    }

    public void setTotalHoteles(int totalHoteles) {
        this.totalHoteles = totalHoteles;
    }

    public int getHotelesActivos() {
        return hotelesActivos;
    }

    public void setHotelesActivos(int hotelesActivos) {
        this.hotelesActivos = hotelesActivos;
    }

    public int getHabitacionesDisponibles() {
        return habitacionesDisponibles;
    }

    public void setHabitacionesDisponibles(int habitacionesDisponibles) {
        this.habitacionesDisponibles = habitacionesDisponibles;
    }

    public int getCapacidadMaximaAdultos() {
        return capacidadMaximaAdultos;
    }

    public void setCapacidadMaximaAdultos(int capacidadMaximaAdultos) {
        this.capacidadMaximaAdultos = capacidadMaximaAdultos;
    }

    public int getCapacidadMaximaNinos() {
        return capacidadMaximaNinos;
    }

    public void setCapacidadMaximaNinos(int capacidadMaximaNinos) {
        this.capacidadMaximaNinos = capacidadMaximaNinos;
    }

    @Override
    public String toString() {
        return "AvailabilityStats{" +
                "totalHoteles=" + totalHoteles +
                ", hotelesActivos=" + hotelesActivos +
                ", habitacionesDisponibles=" + habitacionesDisponibles +
                ", capacidadMaximaAdultos=" + capacidadMaximaAdultos +
                ", capacidadMaximaNinos=" + capacidadMaximaNinos +
                '}';
    }
}