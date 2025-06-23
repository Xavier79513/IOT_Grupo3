package com.example.telehotel.data.model;

import com.google.firebase.firestore.Exclude;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/*public class Reserva {
    public String id;
    public String clienteId;
    public String hotelId;
    public String habitacionId;
    public LocalDate fechaEntrada;
    public LocalDate fechaSalida;
    public List<ServicioAdicional> serviciosAdicionales = new ArrayList<>();
    public Double montoTotal;
    public String estado;
    public LocalDateTime fechaReserva;
    public String tarjetaUltimosDigitos;
    public String tipoTarjeta;
    public DatosCheckout checkout;
    public SolicitudTaxi solicitudTaxi;

    public Reserva() {
        this.fechaReserva = LocalDateTime.now();
        this.solicitudTaxi = new SolicitudTaxi();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getHabitacionId() {
        return habitacionId;
    }

    public void setHabitacionId(String habitacionId) {
        this.habitacionId = habitacionId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public List<ServicioAdicional> getServiciosAdicionales() {
        return serviciosAdicionales;
    }

    public void setServiciosAdicionales(List<ServicioAdicional> serviciosAdicionales) {
        this.serviciosAdicionales = serviciosAdicionales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getTarjetaUltimosDigitos() {
        return tarjetaUltimosDigitos;
    }

    public void setTarjetaUltimosDigitos(String tarjetaUltimosDigitos) {
        this.tarjetaUltimosDigitos = tarjetaUltimosDigitos;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public DatosCheckout getCheckout() {
        return checkout;
    }

    public void setCheckout(DatosCheckout checkout) {
        this.checkout = checkout;
    }

    public SolicitudTaxi getSolicitudTaxi() {
        return solicitudTaxi;
    }

    public void setSolicitudTaxi(SolicitudTaxi solicitudTaxi) {
        this.solicitudTaxi = solicitudTaxi;
    }

}*/
public class Reserva {
    // ========== CAMPOS EXISTENTES (MANTENIDOS) ==========
    public String id;
    public String clienteId;
    public String hotelId;
    public String habitacionId;



    // AGREGAR ESTE CAMPO:
    public Long fechaReservaTimestamp; // Para ordenamiento y compatibilidad
    @Exclude
    public LocalDate fechaEntrada;

    @Exclude
    public LocalDate fechaSalida;
    public List<ServicioAdicional> serviciosAdicionales = new ArrayList<>();
    public Double montoTotal;
    public String estado;
    @Exclude
    public LocalDateTime fechaReserva;
    public String tarjetaUltimosDigitos;
    public String tipoTarjeta;
    public DatosCheckout checkout;
    public SolicitudTaxi solicitudTaxi;

    // ========== CAMPOS AGREGADOS NECESARIOS ==========

    // Información adicional del cliente
    public String clienteNombre;
    public String clienteEmail;
    public String clienteTelefono;

    // Información adicional del hotel
    public String hotelNombre;
    public String hotelUbicacion;

    // Información adicional de la habitación
    public String habitacionNumero;
    public String habitacionTipo;
    public String habitacionDescripcion;
    public Double habitacionPrecio;

    // Información de la estadía
    public Integer totalDias;
    public String huespedes;

    // Información adicional del pago
    public Double impuestos;
    public String metodoPago;
    public String estadoPago; // "pendiente", "pagado", "cancelado"

    // Código de reserva único
    public String codigoReserva;

    // Campos para compatibilidad con tu código
    public Long fechaInicio; // timestamp
    public Long fechaFin; // timestamp

    // ========== CONSTRUCTORES ==========
    public Reserva() {
        this.fechaReserva = LocalDateTime.now();
        this.solicitudTaxi = new SolicitudTaxi();
        this.estado = "activa";
        this.estadoPago = "pagado";
        this.codigoReserva = generarCodigoReserva();
    }

    // ========== MÉTODOS EXISTENTES (MANTENIDOS) ==========
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public Long getFechaReservaTimestamp() {
        return fechaReservaTimestamp;
    }

    public void setFechaReservaTimestamp(Long fechaReservaTimestamp) {
        this.fechaReservaTimestamp = fechaReservaTimestamp;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getHabitacionId() {
        return habitacionId;
    }

    public void setHabitacionId(String habitacionId) {
        this.habitacionId = habitacionId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }
    @Exclude
    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }
    @Exclude
    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }
    @Exclude
    public LocalDate getFechaSalida() {
        return fechaSalida;
    }
    @Exclude
    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public List<ServicioAdicional> getServiciosAdicionales() {
        return serviciosAdicionales;
    }

    public void setServiciosAdicionales(List<ServicioAdicional> serviciosAdicionales) {
        this.serviciosAdicionales = serviciosAdicionales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    @Exclude
    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }
    @Exclude
    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getTarjetaUltimosDigitos() {
        return tarjetaUltimosDigitos;
    }

    public void setTarjetaUltimosDigitos(String tarjetaUltimosDigitos) {
        this.tarjetaUltimosDigitos = tarjetaUltimosDigitos;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public DatosCheckout getCheckout() {
        return checkout;
    }

    public void setCheckout(DatosCheckout checkout) {
        this.checkout = checkout;
    }

    public SolicitudTaxi getSolicitudTaxi() {
        return solicitudTaxi;
    }

    public void setSolicitudTaxi(SolicitudTaxi solicitudTaxi) {
        this.solicitudTaxi = solicitudTaxi;
    }

    // ========== GETTERS Y SETTERS AGREGADOS ==========

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getHotelNombre() {
        return hotelNombre;
    }

    public void setHotelNombre(String hotelNombre) {
        this.hotelNombre = hotelNombre;
    }

    public String getHotelUbicacion() {
        return hotelUbicacion;
    }

    public void setHotelUbicacion(String hotelUbicacion) {
        this.hotelUbicacion = hotelUbicacion;
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

    public String getHabitacionDescripcion() {
        return habitacionDescripcion;
    }

    public void setHabitacionDescripcion(String habitacionDescripcion) {
        this.habitacionDescripcion = habitacionDescripcion;
    }

    public Double getHabitacionPrecio() {
        return habitacionPrecio;
    }

    public void setHabitacionPrecio(Double habitacionPrecio) {
        this.habitacionPrecio = habitacionPrecio;
    }

    public Integer getTotalDias() {
        return totalDias;
    }

    public void setTotalDias(Integer totalDias) {
        this.totalDias = totalDias;
    }

    public String getHuespedes() {
        return huespedes;
    }

    public void setHuespedes(String huespedes) {
        this.huespedes = huespedes;
    }

    public Double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(Double impuestos) {
        this.impuestos = impuestos;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public void setCodigoReserva(String codigoReserva) {
        this.codigoReserva = codigoReserva;
    }

    // ========== MÉTODOS PARA FECHAS (COMPATIBILIDAD CON TU CÓDIGO) ==========

    public Long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Long fechaInicio) {
        this.fechaInicio = fechaInicio;
        // También actualizar LocalDate si se proporciona timestamp
        if (fechaInicio != null && fechaInicio > 0) {
            this.fechaEntrada = Instant.ofEpochMilli(fechaInicio)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
    }

    public Long getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Long fechaFin) {
        this.fechaFin = fechaFin;
        // También actualizar LocalDate si se proporciona timestamp
        if (fechaFin != null && fechaFin > 0) {
            this.fechaSalida = Instant.ofEpochMilli(fechaFin)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
    }

    // ========== MÉTODOS UTILITARIOS ==========

    private String generarCodigoReserva() {
        // Generar código único: TH + timestamp + random
        return "TH" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    // Método utilitario para convertir LocalDate a long (timestamp) si es necesario
    public long getFechaEntradaTimestamp() {
        if (fechaEntrada != null) {
            return fechaEntrada.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return fechaInicio != null ? fechaInicio : 0;
    }

    public long getFechaSalidaTimestamp() {
        if (fechaSalida != null) {
            return fechaSalida.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return fechaFin != null ? fechaFin : 0;
    }

    // Método para establecer fechas desde timestamp (para compatibilidad)
    public void setFechaEntradaFromTimestamp(long timestamp) {
        setFechaInicio(timestamp);
    }

    public void setFechaSalidaFromTimestamp(long timestamp) {
        setFechaFin(timestamp);
    }
}
