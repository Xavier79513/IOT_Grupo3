package com.example.telehotel.data.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogSistema {
    private String id;
    private String accion;
    private String usuarioId;
    private String descripcion;
    private String timestamp;
    private Date fechaCreacion;

    // Constructor vacío requerido por Firestore
    public LogSistema() {}

    // Constructor completo
    public LogSistema(String id, String accion, String usuarioId, String descripcion, String timestamp) {
        this.id = id;
        this.accion = accion;
        this.usuarioId = usuarioId;
        this.descripcion = descripcion;
        this.timestamp = timestamp;
        this.fechaCreacion = new Date();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // Métodos de utilidad
    public String getFormattedDate() {
        if (timestamp == null) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return timestamp.split(" ")[0];
        }
    }

    public String getFormattedTime() {
        if (timestamp == null) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (ParseException e) {
            String[] parts = timestamp.split(" ");
            return parts.length > 1 ? parts[1] : "";
        }
    }

    public String getActionDisplayName() {
        if (accion == null) return "Acción";
        switch (accion) {
            case "CREATE": return "Crear";
            case "UPDATE": return "Actualizar";
            case "DELETE": return "Eliminar";
            case "LOGIN": return "Inicio Sesión";
            case "LOGOUT": return "Cerrar Sesión";
            case "SYSTEM": return "Sistema";
            case "ERROR": return "Error";
            default: return accion;
        }
    }

    public String getUserDisplayName() {
        if (usuarioId == null || usuarioId.equals("SISTEMA")) {
            return "Sistema Automático";
        }
        return "Usuario: " + usuarioId;
    }
}