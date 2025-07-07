package com.example.telehotel.data.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modelo para representar las notificaciones de checkout que recibe el administrador
 */
public class NotificacionCheckout implements Serializable {

    private String id;
    private String tipo; // "checkout_solicitado"
    private String reservaId;
    private String clienteId;
    private String clienteNombre;
    private String hotelId;
    private String hotelNombre;
    private String habitacionNumero;
    private String habitacionTipo;
    private Double montoTotal;
    private String mensaje;
    private long timestamp;
    private boolean leida;
    private boolean procesada; // Si ya fue procesada por el admin
    private String estadoProcesamiento; // "pendiente", "procesando", "completada", "rechazada"

    // Datos adicionales que pueden ser útiles
    private String clienteTelefono;
    private String clienteEmail;
    private Long fechaSolicitud;
    private String observaciones;

    // Constructor vacío requerido por Firestore
    public NotificacionCheckout() {
        this.timestamp = System.currentTimeMillis();
        this.leida = false;
        this.procesada = false;
        this.estadoProcesamiento = "pendiente";
    }

    // Constructor con parámetros básicos
    public NotificacionCheckout(String reservaId, String clienteId, String clienteNombre,
                                String hotelId, String hotelNombre, String habitacionNumero) {
        this();
        this.tipo = "checkout_solicitado";
        this.reservaId = reservaId;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.hotelId = hotelId;
        this.hotelNombre = hotelNombre;
        this.habitacionNumero = habitacionNumero;
        this.mensaje = "El cliente " + clienteNombre + " ha solicitado checkout para la habitación " + habitacionNumero;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getReservaId() {
        return reservaId;
    }

    public void setReservaId(String reservaId) {
        this.reservaId = reservaId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelNombre() {
        return hotelNombre;
    }

    public void setHotelNombre(String hotelNombre) {
        this.hotelNombre = hotelNombre;
    }

    public String getHabitacionNumero() {
        return habitacionNumero;
    }

    public void setHabitacionNumero(String habitacionNumero) {
        this.habitacionNumero = habitacionNumero;
    }

    public String getHabitacionTipo() {
        return habitacionTipo;
    }

    public void setHabitacionTipo(String habitacionTipo) {
        this.habitacionTipo = habitacionTipo;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public boolean isProcesada() {
        return procesada;
    }

    public void setProcesada(boolean procesada) {
        this.procesada = procesada;
    }

    public String getEstadoProcesamiento() {
        return estadoProcesamiento;
    }

    public void setEstadoProcesamiento(String estadoProcesamiento) {
        this.estadoProcesamiento = estadoProcesamiento;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public Long getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Long fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // Métodos utilitarios

    /**
     * Obtiene la fecha de la solicitud formateada
     */
    public String getFechaFormateada() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return formatter.format(new Date(timestamp));
        } catch (Exception e) {
            return "Fecha no disponible";
        }
    }

    /**
     * Obtiene la fecha de la solicitud formateada (solo fecha)
     */
    public String getFechaSoloFormateada() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return formatter.format(new Date(timestamp));
        } catch (Exception e) {
            return "Fecha no disponible";
        }
    }

    /**
     * Obtiene la hora de la solicitud formateada
     */
    public String getHoraFormateada() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return formatter.format(new Date(timestamp));
        } catch (Exception e) {
            return "Hora no disponible";
        }
    }

    /**
     * Verifica si la notificación es reciente (últimas 24 horas)
     */
    public boolean isReciente() {
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        return (System.currentTimeMillis() - timestamp) < oneDayInMillis;
    }

    /**
     * Obtiene el tiempo transcurrido desde la solicitud
     */
    public String getTiempoTranscurrido() {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (days > 0) {
            return days + (days == 1 ? " día" : " días");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hora" : " horas");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minuto" : " minutos");
        } else {
            return "Hace un momento";
        }
    }

    /**
     * Obtiene el color del estado para mostrar en la UI
     */
    public String getColorEstado() {
        switch (estadoProcesamiento) {
            case "pendiente":
                return "#FF9800"; // Naranja
            case "procesando":
                return "#2196F3"; // Azul
            case "completada":
                return "#4CAF50"; // Verde
            case "rechazada":
                return "#F44336"; // Rojo
            default:
                return "#757575"; // Gris
        }
    }

    /**
     * Obtiene el texto del estado para mostrar en la UI
     */
    public String getTextoEstado() {
        switch (estadoProcesamiento) {
            case "pendiente":
                return "Pendiente";
            case "procesando":
                return "Procesando";
            case "completada":
                return "Completada";
            case "rechazada":
                return "Rechazada";
            default:
                return "Desconocido";
        }
    }

    /**
     * Marca la notificación como leída
     */
    public void marcarComoLeida() {
        this.leida = true;
    }

    /**
     * Marca la notificación como procesada
     */
    public void marcarComoProcesada() {
        this.procesada = true;
        this.estadoProcesamiento = "completada";
    }

    @Override
    public String toString() {
        return "NotificacionCheckout{" +
                "id='" + id + '\'' +
                ", clienteNombre='" + clienteNombre + '\'' +
                ", habitacionNumero='" + habitacionNumero + '\'' +
                ", estadoProcesamiento='" + estadoProcesamiento + '\'' +
                ", procesada=" + procesada +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificacionCheckout that = (NotificacionCheckout) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}