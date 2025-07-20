package com.example.telehotel.data.model;

public class ReporteHotel {
    private String hotelId;
    private String nombreHotel;
    private String ubicacionHotel;
    private int totalReservas;
    private double totalIngresos;
    private int reservasActivas;
    private int reservasCanceladas;
    private double promedioIngresoPorReserva;
    private String periodo;

    // Constructores
    public ReporteHotel() {}

    public ReporteHotel(String hotelId, String nombreHotel, String ubicacionHotel) {
        this.hotelId = hotelId;
        this.nombreHotel = nombreHotel;
        this.ubicacionHotel = ubicacionHotel;
        this.totalReservas = 0;
        this.totalIngresos = 0.0;
        this.reservasActivas = 0;
        this.reservasCanceladas = 0;
    }

    // =============== GETTERS Y SETTERS ===============

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getNombreHotel() {
        return nombreHotel != null ? nombreHotel : "Hotel Sin Nombre";
    }

    public void setNombreHotel(String nombreHotel) {
        this.nombreHotel = nombreHotel;
    }

    public String getUbicacionHotel() {
        return ubicacionHotel != null ? ubicacionHotel : "Ubicación no disponible";
    }

    public void setUbicacionHotel(String ubicacionHotel) {
        this.ubicacionHotel = ubicacionHotel;
    }

    public int getTotalReservas() {
        return totalReservas;
    }

    public void setTotalReservas(int totalReservas) {
        this.totalReservas = totalReservas;
        calcularPromedio();
    }

    public double getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(double totalIngresos) {
        this.totalIngresos = totalIngresos;
        calcularPromedio();
    }

    public int getReservasActivas() {
        return reservasActivas;
    }

    public void setReservasActivas(int reservasActivas) {
        this.reservasActivas = reservasActivas;
    }

    public int getReservasCanceladas() {
        return reservasCanceladas;
    }

    public void setReservasCanceladas(int reservasCanceladas) {
        this.reservasCanceladas = reservasCanceladas;
    }

    public double getPromedioIngresoPorReserva() {
        return promedioIngresoPorReserva;
    }

    public void setPromedioIngresoPorReserva(double promedioIngresoPorReserva) {
        this.promedioIngresoPorReserva = promedioIngresoPorReserva;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    // =============== MÉTODOS UTILITARIOS ===============

    private void calcularPromedio() {
        if (totalReservas > 0) {
            this.promedioIngresoPorReserva = totalIngresos / totalReservas;
        } else {
            this.promedioIngresoPorReserva = 0.0;
        }
    }

    /**
     * Obtiene el porcentaje de reservas activas
     */
    public double getPorcentajeReservasActivas() {
        if (totalReservas == 0) return 0.0;
        return (double) reservasActivas / totalReservas * 100.0;
    }

    /**
     * Obtiene el porcentaje de reservas canceladas
     */
    public double getPorcentajeReservasCanceladas() {
        if (totalReservas == 0) return 0.0;
        return (double) reservasCanceladas / totalReservas * 100.0;
    }

    /**
     * Obtiene los ingresos formateados como string
     */
    public String getTotalIngresosFormateado() {
        return String.format("S/ %.2f", totalIngresos);
    }

    /**
     * Obtiene el promedio formateado como string
     */
    public String getPromedioFormateado() {
        return String.format("S/ %.2f", promedioIngresoPorReserva);
    }

    /**
     * Obtiene un resumen del hotel
     */
    public String getResumen() {
        return String.format("%s - %d reservas, %s",
                getNombreHotel(),
                totalReservas,
                getTotalIngresosFormateado());
    }

    /**
     * Determina si el hotel tiene buen rendimiento (más del 80% de reservas activas)
     */
    public boolean tieneBuenRendimiento() {
        return getPorcentajeReservasActivas() >= 80.0;
    }

    /**
     * Obtiene el estado del hotel basado en el rendimiento
     */
    public String getEstadoRendimiento() {
        double porcentajeActivas = getPorcentajeReservasActivas();

        if (porcentajeActivas >= 90.0) {
            return "Excelente";
        } else if (porcentajeActivas >= 80.0) {
            return "Muy Bueno";
        } else if (porcentajeActivas >= 70.0) {
            return "Bueno";
        } else if (porcentajeActivas >= 50.0) {
            return "Regular";
        } else {
            return "Necesita Atención";
        }
    }

    /**
     * Incrementa el contador de reservas y actualiza los ingresos
     */
    public void agregarReserva(double montoReserva, boolean esActiva) {
        this.totalReservas++;
        this.totalIngresos += montoReserva;

        if (esActiva) {
            this.reservasActivas++;
        } else {
            this.reservasCanceladas++;
        }

        calcularPromedio();
    }

    @Override
    public String toString() {
        return "ReporteHotel{" +
                "hotelId='" + hotelId + '\'' +
                ", nombreHotel='" + nombreHotel + '\'' +
                ", totalReservas=" + totalReservas +
                ", totalIngresos=" + totalIngresos +
                ", promedioIngresoPorReserva=" + promedioIngresoPorReserva +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ReporteHotel that = (ReporteHotel) obj;
        return hotelId != null ? hotelId.equals(that.hotelId) : that.hotelId == null;
    }

    @Override
    public int hashCode() {
        return hotelId != null ? hotelId.hashCode() : 0;
    }
}
