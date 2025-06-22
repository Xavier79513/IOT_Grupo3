package com.example.telehotel.data.model;

import java.util.ArrayList;
import java.util.List;

/*public class Habitacion {
    private String id;
    private String numero;
    private String tipo; // ej: "standard", "económica", "lux"
    private Capacidad capacidad;
    private Double tamaño;  // área en m2
    private Double precio;
    private String descripcion;
    private List<String> fotos = new ArrayList<>();
    private String estado; // ej: "disponible", "ocupada"
    private List<String> serviciosIncluidos = new ArrayList<>(); // servicios exclusivos o extras para esta habitación (opcional)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Capacidad getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Capacidad capacidad) {
        this.capacidad = capacidad;
    }

    public Double getTamaño() {
        return tamaño;
    }

    public void setTamaño(Double tamaño) {
        this.tamaño = tamaño;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<String> getServiciosIncluidos() {
        return serviciosIncluidos;
    }

    public void setServiciosIncluidos(List<String> serviciosIncluidos) {
        this.serviciosIncluidos = serviciosIncluidos;
    }
}*/

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

/*public class Habitacion {
    private String id;
    private String numero;
    private String tipo; // ej: "standard", "economica", "lux"
    private Capacidad capacidad;
    private Double tamaño;  // área en m2
    private Double precio;
    private String descripcion;

    private List<String> fotos = new ArrayList<>();
    private String estado; // ej: "disponible", "ocupada", "mantenimiento"
    private List<String> serviciosIncluidos = new ArrayList<>(); // servicios exclusivos o extras para esta habitación

    // ✅ AGREGADOS: Campos necesarios para el admin
    private String hotelId;     // ID del hotel al que pertenece
    private Timestamp fechaCreacion;
    private String creadoPor;

    // Constructor vacío requerido por Firebase
    public Habitacion() {}

    // ✅ AGREGADO: Constructor para compatibilidad con el fragment
    public Habitacion(String numero, String tipo, int capacidadAdultos, int capacidadNinos,
                      double tamaño, double precio, String hotelId, String creadoPor) {
        this.numero = numero;
        this.tipo = tipo;
        this.capacidad = new Capacidad(capacidadAdultos, capacidadNinos);
        this.tamaño = tamaño;
        this.precio = precio;
        this.hotelId = hotelId;
        this.estado = "disponible";
        this.fechaCreacion = Timestamp.now();
        this.creadoPor = creadoPor;
    }

    // Getters y Setters existentes
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Capacidad getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Capacidad capacidad) {
        this.capacidad = capacidad;
    }

    public Double getTamaño() {
        return tamaño;
    }

    public void setTamaño(Double tamaño) {
        this.tamaño = tamaño;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<String> getServiciosIncluidos() {
        return serviciosIncluidos;
    }

    public void setServiciosIncluidos(List<String> serviciosIncluidos) {
        this.serviciosIncluidos = serviciosIncluidos;
    }

    // ✅ AGREGADOS: Getters y setters para los nuevos campos
    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    // ✅ AGREGADOS: Métodos de utilidad para el fragment
    public String getCapacidadTexto() {
        if (capacidad != null) {
            return capacidad.getAdultos() + " adultos, " + capacidad.getNinos() + " niños";
        }
        return "No especificada";
    }

    public String getTipoFormateado() {
        if (tipo == null) return "Sin tipo";

        switch (tipo.toLowerCase()) {
            case "standard": return "Estándar";
            case "economica": return "Económica";
            case "lux": return "Lux";
            default: return tipo;
        }
    }

    public String getPrecioFormateado() {
        if (precio != null) {
            return "S/ " + String.format("%.2f", precio);
        }
        return "S/ 0.00";
    }

    // ✅ AGREGADOS: Métodos de compatibilidad para el fragment
    public int getCapacidadAdultos() {
        return capacidad != null ? capacidad.getAdultos() : 0;
    }

    public int getCapacidadNinos() {
        return capacidad != null ? capacidad.getNinos() : 0;
    }

    public void setCapacidadAdultos(int adultos) {
        if (capacidad == null) {
            capacidad = new Capacidad();
        }
        capacidad.setAdultos(adultos);
    }

    public void setCapacidadNinos(int ninos) {
        if (capacidad == null) {
            capacidad = new Capacidad();
        }
        capacidad.setNinos(ninos);
    }

}*/

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Habitacion {
    private String id;
    private String numero;
    private String tipo; // ej: "standard", "economica", "lux"
    private Capacidad capacidad;
    private Double tamaño;  // área en m2
    private Double precio;
    private String descripcion;
    private List<String> fotos = new ArrayList<>();
    private String estado; // ej: "disponible", "ocupada", "mantenimiento"
    private List<String> serviciosIncluidos = new ArrayList<>(); // servicios exclusivos o extras para esta habitación

    // ✅ AGREGADOS: Campos necesarios para el admin
    private String hotelId;     // ID del hotel al que pertenece
    private Timestamp fechaCreacion;
    private String creadoPor;

    // ✅ NUEVOS: Campos adicionales que aparecen en Firebase
    private Integer capacidadAdultos;
    private Integer capacidadNinos;
    private String capacidadTexto;
    private Integer total;

    // Constructor vacío requerido por Firebase
    public Habitacion() {}

    // ✅ AGREGADO: Constructor para compatibilidad con el fragment
    public Habitacion(String numero, String tipo, int capacidadAdultos, int capacidadNinos,
                      double tamaño, double precio, String hotelId, String creadoPor) {
        this.numero = numero;
        this.tipo = tipo;
        this.capacidad = new Capacidad(capacidadAdultos, capacidadNinos);
        this.capacidadAdultos = capacidadAdultos;
        this.capacidadNinos = capacidadNinos;
        this.total = capacidadAdultos + capacidadNinos;
        this.capacidadTexto = capacidadAdultos + " adultos, " + capacidadNinos + " niños";
        this.tamaño = tamaño;
        this.precio = precio;
        this.hotelId = hotelId;
        this.estado = "disponible";
        this.fechaCreacion = Timestamp.now();
        this.creadoPor = creadoPor;
    }

    // ============= GETTERS Y SETTERS BÁSICOS =============

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Capacidad getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Capacidad capacidad) {
        this.capacidad = capacidad;
    }

    public Double getTamaño() {
        return tamaño;
    }

    public void setTamaño(Double tamaño) {
        this.tamaño = tamaño;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<String> getServiciosIncluidos() {
        return serviciosIncluidos;
    }

    public void setServiciosIncluidos(List<String> serviciosIncluidos) {
        this.serviciosIncluidos = serviciosIncluidos;
    }

    // ============= GETTERS Y SETTERS PARA CAMPOS ADMINISTRATIVOS =============

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    // ============= GETTERS Y SETTERS PARA CAMPOS DE FIREBASE =============

    /**
     * Obtiene capacidad de adultos (prioriza campos directos de Firebase)
     */
    public Integer getCapacidadAdultos() {
        // Priorizar el campo directo de Firebase
        if (capacidadAdultos != null) {
            return capacidadAdultos;
        }
        // Fallback al objeto capacidad
        if (capacidad != null) {
            return capacidad.getAdultos();
        }
        return null;
    }

    public void setCapacidadAdultos(Integer capacidadAdultos) {
        this.capacidadAdultos = capacidadAdultos;
        // Sincronizar con el objeto capacidad
        if (capacidad == null) {
            capacidad = new Capacidad();
        }
        capacidad.setAdultos(capacidadAdultos != null ? capacidadAdultos : 0);
        // Actualizar texto y total
        actualizarCapacidadDerivedFields();
    }

    /**
     * Obtiene capacidad de niños (prioriza campos directos de Firebase)
     */
    public Integer getCapacidadNinos() {
        // Priorizar el campo directo de Firebase
        if (capacidadNinos != null) {
            return capacidadNinos;
        }
        // Fallback al objeto capacidad
        if (capacidad != null) {
            return capacidad.getNinos();
        }
        return null;
    }

    public void setCapacidadNinos(Integer capacidadNinos) {
        this.capacidadNinos = capacidadNinos;
        // Sincronizar con el objeto capacidad
        if (capacidad == null) {
            capacidad = new Capacidad();
        }
        capacidad.setNinos(capacidadNinos != null ? capacidadNinos : 0);
        // Actualizar texto y total
        actualizarCapacidadDerivedFields();
    }

    public String getCapacidadTextoField() {
        return capacidadTexto;
    }

    public void setCapacidadTextoField(String capacidadTexto) {
        this.capacidadTexto = capacidadTexto;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    // ============= MÉTODOS DE UTILIDAD =============

    /**
     * Actualiza campos derivados cuando cambia la capacidad
     */
    private void actualizarCapacidadDerivedFields() {
        int adultos = getCapacidadAdultos() != null ? getCapacidadAdultos() : 0;
        int ninos = getCapacidadNinos() != null ? getCapacidadNinos() : 0;

        this.total = adultos + ninos;
        this.capacidadTexto = adultos + " adultos, " + ninos + " niños";
    }

    /**
     * Obtiene texto de capacidad formateado
     */
    public String getCapacidadTexto() {
        if (capacidadTexto != null && !capacidadTexto.isEmpty()) {
            return capacidadTexto;
        }

        // Generar texto si no existe
        Integer adultos = getCapacidadAdultos();
        Integer ninos = getCapacidadNinos();

        if (adultos != null || ninos != null) {
            int a = adultos != null ? adultos : 0;
            int n = ninos != null ? ninos : 0;
            return a + " adultos, " + n + " niños";
        }

        return "No especificada";
    }

    public String getTipoFormateado() {
        if (tipo == null) return "Sin tipo";

        switch (tipo.toLowerCase()) {
            case "standard": return "Estándar";
            case "economica": return "Económica";
            case "lux": return "Lux";
            default: return tipo;
        }
    }

    public String getPrecioFormateado() {
        if (precio != null) {
            return "S/ " + String.format("%.2f", precio);
        }
        return "S/ 0.00";
    }

    /**
     * Verifica si la habitación está disponible
     */
    public boolean estaDisponible() {
        return "disponible".equalsIgnoreCase(estado);
    }

    /**
     * Verifica si la habitación puede acomodar exactamente la capacidad especificada
     */
    public boolean tieneCapacidadExacta(int adultosRequeridos, int ninosRequeridos) {
        Integer adultos = getCapacidadAdultos();
        Integer ninos = getCapacidadNinos();

        if (adultos == null || ninos == null) {
            return false;
        }

        return adultos.equals(adultosRequeridos) && ninos.equals(ninosRequeridos);
    }

    /**
     * Verifica si la habitación puede acomodar al menos la capacidad especificada
     */
    public boolean tieneCapacidadMinima(int adultosRequeridos, int ninosRequeridos) {
        Integer adultos = getCapacidadAdultos();
        Integer ninos = getCapacidadNinos();

        if (adultos == null || ninos == null) {
            return false;
        }

        return adultos >= adultosRequeridos && ninos >= ninosRequeridos;
    }

    /**
     * Obtiene la capacidad total de la habitación
     */
    public int getCapacidadTotal() {
        if (total != null) {
            return total;
        }

        Integer adultos = getCapacidadAdultos();
        Integer ninos = getCapacidadNinos();

        int a = adultos != null ? adultos : 0;
        int n = ninos != null ? ninos : 0;

        return a + n;
    }

    // ============= MÉTODOS DE COMPATIBILIDAD =============

    /**
     * Métodos para compatibilidad con código existente
     */
    public int getCapacidadAdultosInt() {
        Integer adultos = getCapacidadAdultos();
        return adultos != null ? adultos : 0;
    }

    public int getCapacidadNinosInt() {
        Integer ninos = getCapacidadNinos();
        return ninos != null ? ninos : 0;
    }

    // ============= OVERRIDE METHODS =============

    @Override
    public String toString() {
        return "Habitacion{" +
                "id='" + id + '\'' +
                ", numero='" + numero + '\'' +
                ", tipo='" + tipo + '\'' +
                ", capacidadAdultos=" + getCapacidadAdultos() +
                ", capacidadNinos=" + getCapacidadNinos() +
                ", precio=" + precio +
                ", estado='" + estado + '\'' +
                ", hotelId='" + hotelId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Habitacion that = (Habitacion) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

