/*package com.example.telehotel.data.model;

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
}*/
package com.example.telehotel.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogSistema {
    private String id;
    private String accion;
    private String usuarioId;
    private String usuarioEmail;
    private String usuarioNombre;
    private String usuarioRole;
    private String descripcion;
    private String timestamp;
    private long fechaCreacionMillis;  // ✅ CAMBIO CRÍTICO: Usar long en lugar de Date

    // Constructor vacío requerido por Firestore
    public LogSistema() {
        // ✅ CAMBIO: Crear fecha independiente en cada instancia
        this.fechaCreacionMillis = System.currentTimeMillis();
        this.timestamp = formatTimestamp(this.fechaCreacionMillis);
    }

    // Constructor completo ANTIGUO - mantener para compatibilidad
    public LogSistema(String id, String accion, String usuarioId, String descripcion, String timestamp) {
        this.id = id;
        this.accion = accion;
        this.usuarioId = usuarioId;
        this.descripcion = descripcion;
        this.timestamp = timestamp;
        // ✅ CAMBIO: Crear fecha independiente
        this.fechaCreacionMillis = System.currentTimeMillis();
    }

    // ✅ NUEVO: Constructor completo con información de usuario
    public LogSistema(String accion, String usuarioId, String usuarioEmail,
                      String usuarioNombre, String usuarioRole, String descripcion) {
        // ✅ CAMBIO CRÍTICO: Crear timestamp ÚNICO para cada log
        long currentTime = System.currentTimeMillis();

        this.accion = accion;
        this.usuarioId = usuarioId;
        this.usuarioEmail = usuarioEmail;
        this.usuarioNombre = usuarioNombre;
        this.usuarioRole = usuarioRole;
        this.descripcion = descripcion;
        this.fechaCreacionMillis = currentTime;
        this.timestamp = formatTimestamp(currentTime);
    }

    // ✅ NUEVO: Método helper para formatear timestamp de forma consistente
    private String formatTimestamp(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    // Getters y Setters EXISTENTES
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

    // ✅ CAMBIO: Getter y Setter para fechaCreacionMillis
    public long getFechaCreacionMillis() { return fechaCreacionMillis; }
    public void setFechaCreacionMillis(long fechaCreacionMillis) {
        this.fechaCreacionMillis = fechaCreacionMillis;
        this.timestamp = formatTimestamp(fechaCreacionMillis);
    }

    // ✅ MÉTODO DE COMPATIBILIDAD: Para mantener compatibilidad con código existente
    public Date getFechaCreacion() {
        return new Date(fechaCreacionMillis);
    }

    public void setFechaCreacion(Date fechaCreacion) {
        if (fechaCreacion != null) {
            this.fechaCreacionMillis = fechaCreacion.getTime();
            this.timestamp = formatTimestamp(this.fechaCreacionMillis);
        }
    }

    // NUEVOS Getters y Setters
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getUsuarioRole() { return usuarioRole; }
    public void setUsuarioRole(String usuarioRole) { this.usuarioRole = usuarioRole; }

    // ✅ MÉTODOS DE FORMATO CORREGIDOS
    public String getFormattedDate() {
        try {
            Date dateToFormat = new Date(fechaCreacionMillis);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
            return outputFormat.format(dateToFormat);
        } catch (Exception e) {
            return "Fecha no disponible";
        }
    }

    public String getFormattedTime() {
        try {
            Date dateToFormat = new Date(fechaCreacionMillis);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return outputFormat.format(dateToFormat);
        } catch (Exception e) {
            return "Hora no disponible";
        }
    }

    public String getActionDisplayName() {
        if (accion == null) return "Acción Desconocida";

        switch (accion.toUpperCase()) {
            case "CREATE": return "Crear";
            case "UPDATE": return "Actualizar";
            case "DELETE": return "Eliminar";
            case "LOGIN": return "Iniciar Sesión";
            case "LOGOUT": return "Cerrar Sesión";
            case "ERROR": return "Error";
            case "SYSTEM": return "Sistema";
            case "USER_MANAGEMENT": return "Gestión Usuario";
            case "HOTEL_MANAGEMENT": return "Gestión Hotel";
            default: return accion;
        }
    }

    public String getUserDisplayName() {
        // Prioridad: Nombre completo > Email > ID > Sistema
        if (usuarioNombre != null && !usuarioNombre.isEmpty() &&
                !usuarioNombre.equals("null") && !usuarioNombre.equals("Usuario")) {

            String displayName = usuarioNombre;

            // Agregar rol si está disponible
            if (usuarioRole != null && !usuarioRole.isEmpty() && !usuarioRole.equals("null")) {
                String roleDisplay = getRoleDisplayName();
                displayName += " (" + roleDisplay + ")";
            }

            return displayName;
        } else if (usuarioEmail != null && !usuarioEmail.isEmpty() && !usuarioEmail.equals("null")) {
            return usuarioEmail;
        } else if (usuarioId != null && !usuarioId.isEmpty() && !usuarioId.equals("SISTEMA")) {
            return "Usuario: " + usuarioId.substring(0, Math.min(8, usuarioId.length())) + "...";
        } else {
            return "Sistema Automático";
        }
    }

    private String getRoleDisplayName() {
        if (usuarioRole == null) return "";

        switch (usuarioRole.toLowerCase()) {
            case "admin": return "Administrador";
            case "superadmin": return "Super Admin";
            case "taxista": return "Taxista";
            case "cliente": return "Cliente";
            default: return usuarioRole;
        }
    }

    @Override
    public String toString() {
        return "LogSistema{" +
                "accion='" + accion + '\'' +
                ", usuarioNombre='" + usuarioNombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fechaCreacionMillis=" + fechaCreacionMillis +
                ", formattedTime=" + getFormattedTime() +
                '}';
    }
}