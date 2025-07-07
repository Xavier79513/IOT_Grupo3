package com.example.telehotel.features.admin.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.NotificacionCheckout;

import java.util.List;
import java.util.Locale;

public class SolicitudesCheckoutAdapter extends RecyclerView.Adapter<SolicitudesCheckoutAdapter.SolicitudViewHolder> {

    private List<NotificacionCheckout> solicitudes;
    private Context context;
    private OnSolicitudClickListener listener;

    public interface OnSolicitudClickListener {
        void onSolicitudClick(NotificacionCheckout notificacion);
    }

    public SolicitudesCheckoutAdapter(List<NotificacionCheckout> solicitudes, Context context,
                                      OnSolicitudClickListener listener) {
        this.solicitudes = solicitudes;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_solicitud_checkout, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        NotificacionCheckout solicitud = solicitudes.get(position);

        // Configurar información básica
        configurarInformacionBasica(holder, solicitud);

        // Configurar estado visual
        configurarEstadoVisual(holder, solicitud);

        // Configurar fechas y tiempo
        configurarFechasYTiempo(holder, solicitud);

        // Configurar monto
        configurarMonto(holder, solicitud);

        // Configurar acciones
        configurarAcciones(holder, solicitud);

        // Configurar mensaje (si existe)
        configurarMensaje(holder, solicitud);

        // Click en toda la card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Marcar como leída si no lo estaba
                if (!solicitud.isLeida()) {
                    solicitud.marcarComoLeida();
                }
                listener.onSolicitudClick(solicitud);
            }
        });
    }

    private void configurarInformacionBasica(SolicitudViewHolder holder, NotificacionCheckout solicitud) {
        // Nombre del cliente
        holder.tvClienteNombre.setText(solicitud.getClienteNombre() != null ?
                solicitud.getClienteNombre() : "Cliente no especificado");

        // Información de la habitación
        String habitacionInfo = "Habitación " +
                (solicitud.getHabitacionNumero() != null ? solicitud.getHabitacionNumero() : "N/A");

        if (solicitud.getHabitacionTipo() != null && !solicitud.getHabitacionTipo().isEmpty()) {
            habitacionInfo += " - " + solicitud.getHabitacionTipo();
        }

        holder.tvHabitacion.setText(habitacionInfo);
    }

    private void configurarEstadoVisual(SolicitudViewHolder holder, NotificacionCheckout solicitud) {
        String estado = solicitud.getEstadoProcesamiento();
        String textoEstado = solicitud.getTextoEstado();
        String colorEstado = solicitud.getColorEstado();

        // Configurar texto del estado
        holder.tvEstado.setText(textoEstado.toUpperCase());
        holder.tvEstado.setTextColor(Color.parseColor(colorEstado));

        // Configurar indicador visual de estado
        int colorIndicador = Color.parseColor(colorEstado);
        holder.viewEstado.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(colorIndicador));

        // Aplicar opacidad si ya está procesada
        float alpha = solicitud.isProcesada() ? 0.6f : 1.0f;
        holder.itemView.setAlpha(alpha);
    }

    private void configurarFechasYTiempo(SolicitudViewHolder holder, NotificacionCheckout solicitud) {
        // Fecha de solicitud
        holder.tvFechaSolicitud.setText(solicitud.getFechaSoloFormateada());

        // Hora de solicitud
        holder.tvHoraSolicitud.setText(solicitud.getHoraFormateada());

        // Tiempo transcurrido
        holder.tvTiempo.setText(solicitud.getTiempoTranscurrido());

        // Cambiar color si es muy antigua (más de 24 horas)
        if (!solicitud.isReciente()) {
            holder.tvTiempo.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else {
            holder.tvTiempo.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }
    }

    private void configurarMonto(SolicitudViewHolder holder, NotificacionCheckout solicitud) {
        Double monto = solicitud.getMontoTotal();
        if (monto != null && monto > 0) {
            holder.tvMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", monto));
            holder.tvMonto.setVisibility(View.VISIBLE);
        } else {
            holder.tvMonto.setText("S/ 0.00");
            holder.tvMonto.setVisibility(View.VISIBLE);
        }
    }

    private void configurarAcciones(SolicitudViewHolder holder, NotificacionCheckout solicitud) {
        String estado = solicitud.getEstadoProcesamiento();

        // Mostrar botón de acción solo para solicitudes pendientes
        if ("pendiente".equals(estado)) {
            holder.layoutAcciones.setVisibility(View.VISIBLE);
            holder.btnAccionRapida.setText("Procesar");
            holder.btnAccionRapida.setEnabled(true);

            holder.btnAccionRapida.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSolicitudClick(solicitud);
                }
            });
        } else if ("procesando".equals(estado)) {
            holder.layoutAcciones.setVisibility(View.VISIBLE);
            holder.btnAccionRapida.setText("Procesando...");
            holder.btnAccionRapida.setEnabled(false);
            holder.btnAccionRapida.setOnClickListener(null);
        } else {
            // Para estados "completada" o "rechazada", ocultar acciones
            holder.layoutAcciones.setVisibility(View.GONE);
        }
    }

    private void configurarMensaje(SolicitudViewHolder holder, NotificacionCheckout solicitud) {
        String mensaje = solicitud.getMensaje();
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            holder.tvMensaje.setText(mensaje);
            holder.tvMensaje.setVisibility(View.VISIBLE);
        } else {
            holder.tvMensaje.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return solicitudes.size();
    }

    // Métodos utilitarios para actualizar la lista

    public void actualizarSolicitud(NotificacionCheckout solicitudActualizada) {
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).getId().equals(solicitudActualizada.getId())) {
                solicitudes.set(i, solicitudActualizada);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void eliminarSolicitud(String solicitudId) {
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).getId().equals(solicitudId)) {
                solicitudes.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void agregarSolicitud(NotificacionCheckout nuevaSolicitud) {
        solicitudes.add(0, nuevaSolicitud); // Agregar al principio
        notifyItemInserted(0);
    }

    public int contarSolicitudesPendientes() {
        int contador = 0;
        for (NotificacionCheckout solicitud : solicitudes) {
            if ("pendiente".equals(solicitud.getEstadoProcesamiento())) {
                contador++;
            }
        }
        return contador;
    }

    public int contarSolicitudesCompletadas() {
        int contador = 0;
        for (NotificacionCheckout solicitud : solicitudes) {
            if ("completada".equals(solicitud.getEstadoProcesamiento())) {
                contador++;
            }
        }
        return contador;
    }

    // ViewHolder
    public static class SolicitudViewHolder extends RecyclerView.ViewHolder {

        // Estado
        View viewEstado;
        TextView tvEstado, tvTiempo;

        // Información principal
        TextView tvClienteNombre, tvHabitacion, tvMonto;

        // Fechas
        TextView tvFechaSolicitud, tvHoraSolicitud;

        // Acciones
        LinearLayout layoutAcciones;
        Button btnAccionRapida;

        // Mensaje
        TextView tvMensaje;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);

            // Estado
            viewEstado = itemView.findViewById(R.id.viewEstado);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);

            // Información principal
            tvClienteNombre = itemView.findViewById(R.id.tvClienteNombre);
            tvHabitacion = itemView.findViewById(R.id.tvHabitacion);
            tvMonto = itemView.findViewById(R.id.tvMonto);

            // Fechas
            tvFechaSolicitud = itemView.findViewById(R.id.tvFechaSolicitud);
            tvHoraSolicitud = itemView.findViewById(R.id.tvHoraSolicitud);

            // Acciones
            layoutAcciones = itemView.findViewById(R.id.layoutAcciones);
            btnAccionRapida = itemView.findViewById(R.id.btnAccionRapida);

            // Mensaje
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
        }
    }
}