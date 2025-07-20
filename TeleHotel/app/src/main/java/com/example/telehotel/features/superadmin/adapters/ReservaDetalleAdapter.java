/*package com.example.telehotel.features.superadmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservaDetalleAdapter extends RecyclerView.Adapter<ReservaDetalleAdapter.ReservaViewHolder> {

    private List<Reserva> reservasList;
    private Context context;
    private SimpleDateFormat dateFormat;

    public ReservaDetalleAdapter(Context context, List<Reserva> reservasList) {
        this.context = context;
        this.reservasList = reservasList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reserva_detalle, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservasList.get(position);
        holder.bind(reserva);
    }

    @Override
    public int getItemCount() {
        return reservasList != null ? reservasList.size() : 0;
    }

    public void updateList(List<Reserva> newList) {
        this.reservasList = newList;
        notifyDataSetChanged();
    }

    public class ReservaViewHolder extends RecyclerView.ViewHolder {

        private TextView tvClienteNombre, tvFechasReserva, tvHabitacionReserva;
        private TextView tvMontoReserva, tvEstadoTexto;
        private View viewEstadoReserva;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        private void initViews() {
            tvClienteNombre = itemView.findViewById(R.id.tvClienteNombre);
            tvFechasReserva = itemView.findViewById(R.id.tvFechasReserva);
            tvHabitacionReserva = itemView.findViewById(R.id.tvHabitacionReserva);
            tvMontoReserva = itemView.findViewById(R.id.tvMontoReserva);
            tvEstadoTexto = itemView.findViewById(R.id.tvEstadoTexto);
            viewEstadoReserva = itemView.findViewById(R.id.viewEstadoReserva);
        }

        public void bind(Reserva reserva) {
            // Nombre del cliente
            String nombreCliente = reserva.getClienteNombre();
            if (nombreCliente == null || nombreCliente.isEmpty()) {
                nombreCliente = "Cliente no especificado";
            }
            tvClienteNombre.setText(nombreCliente);

            // Fechas de la reserva
            String fechasTexto = obtenerFechasTexto(reserva);
            tvFechasReserva.setText(fechasTexto);

            // Información de la habitación
            String habitacionInfo = obtenerHabitacionInfo(reserva);
            tvHabitacionReserva.setText(habitacionInfo);

            // Monto
            double monto = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
            tvMontoReserva.setText(String.format(Locale.getDefault(), "S/ %.2f", monto));

            // Estado
            configurarEstado(reserva.getEstado());
        }

        private String obtenerFechasTexto(Reserva reserva) {
            try {
                // Intentar usar los timestamps
                if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
                    Date fechaInicio = new Date(reserva.getFechaInicio());
                    Date fechaFin = new Date(reserva.getFechaFin());

                    String inicioStr = dateFormat.format(fechaInicio);
                    String finStr = dateFormat.format(fechaFin);

                    // Calcular días
                    long diffInMillis = reserva.getFechaFin() - reserva.getFechaInicio();
                    int dias = (int) (diffInMillis / (1000 * 60 * 60 * 24));

                    return String.format("%s - %s (%d días)", inicioStr, finStr, dias);
                }

                // Fallback usando campos de texto si existen
                return "Fechas no disponibles";

            } catch (Exception e) {
                return "Fechas no disponibles";
            }
        }

        private String obtenerHabitacionInfo(Reserva reserva) {
            StringBuilder info = new StringBuilder("🏠 ");

            if (reserva.getHabitacionNumero() != null && !reserva.getHabitacionNumero().isEmpty()) {
                info.append("Hab. ").append(reserva.getHabitacionNumero());
            } else {
                info.append("Habitación no especificada");
            }

            if (reserva.getHabitacionTipo() != null && !reserva.getHabitacionTipo().isEmpty()) {
                info.append(" - ").append(reserva.getHabitacionTipo());
            }

            return info.toString();
        }

        private void configurarEstado(String estado) {
            if (estado == null) estado = "desconocido";

            int colorEstado;
            String textoEstado;

            switch (estado.toLowerCase()) {
                case "activa":
                case "confirmada":
                    colorEstado = ContextCompat.getColor(context, R.color.green_medium);
                    textoEstado = "ACTIVA";
                    break;
                case "completada":
                case "finalizada":
                    colorEstado = ContextCompat.getColor(context, R.color.blue);
                    textoEstado = "COMPLETADA";
                    break;
                case "cancelada":
                    colorEstado = ContextCompat.getColor(context, R.color.red_medium);
                    textoEstado = "CANCELADA";
                    break;
                case "pendiente":
                    colorEstado = ContextCompat.getColor(context, R.color.orange_dark);
                    textoEstado = "PENDIENTE";
                    break;
                default:
                    colorEstado = ContextCompat.getColor(context, R.color.gray_medium);
                    textoEstado = estado.toUpperCase();
                    break;
            }

            viewEstadoReserva.setBackgroundColor(colorEstado);
            tvEstadoTexto.setText(textoEstado);
            tvEstadoTexto.setTextColor(colorEstado);
        }
    }
}*/
package com.example.telehotel.features.superadmin.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservaDetalleAdapter extends RecyclerView.Adapter<ReservaDetalleAdapter.ReservaViewHolder> {

    private List<Reserva> reservasList;
    private Context context;
    private SimpleDateFormat dateFormat;

    public ReservaDetalleAdapter(Context context, List<Reserva> reservasList) {
        this.context = context;
        this.reservasList = reservasList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reserva_detalle, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservasList.get(position);
        holder.bind(reserva);
    }

    @Override
    public int getItemCount() {
        return reservasList != null ? reservasList.size() : 0;
    }

    public void updateList(List<Reserva> newList) {
        this.reservasList = newList;
        notifyDataSetChanged();
    }

    public class ReservaViewHolder extends RecyclerView.ViewHolder {

        private TextView tvClienteNombre, tvFechasReserva, tvHabitacionReserva;
        private TextView tvMontoReserva, tvEstadoTexto;
        private View viewEstadoReserva;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        private void initViews() {
            tvClienteNombre = itemView.findViewById(R.id.tvClienteNombre);
            tvFechasReserva = itemView.findViewById(R.id.tvFechasReserva);
            tvHabitacionReserva = itemView.findViewById(R.id.tvHabitacionReserva);
            tvMontoReserva = itemView.findViewById(R.id.tvMontoReserva);
            tvEstadoTexto = itemView.findViewById(R.id.tvEstadoTexto);
            viewEstadoReserva = itemView.findViewById(R.id.viewEstadoReserva);
        }

        public void bind(Reserva reserva) {
            Log.d("ReservaDetalleAdapter", "Binding reserva: " + reserva.getId());

            // Nombre del cliente
            String nombreCliente = reserva.getClienteNombre();
            if (nombreCliente == null || nombreCliente.isEmpty()) {
                // Fallback con email si no hay nombre
                nombreCliente = reserva.getClienteEmail();
                if (nombreCliente == null || nombreCliente.isEmpty()) {
                    nombreCliente = "Cliente ID: " + (reserva.getClienteId() != null ? reserva.getClienteId() : "Desconocido");
                }
            }
            tvClienteNombre.setText(nombreCliente);
            Log.d("ReservaDetalleAdapter", "Nombre cliente: " + nombreCliente);

            // Fechas de la reserva
            String fechasTexto = obtenerFechasTexto(reserva);
            tvFechasReserva.setText(fechasTexto);
            Log.d("ReservaDetalleAdapter", "Fechas: " + fechasTexto);

            // Información de la habitación
            String habitacionInfo = obtenerHabitacionInfo(reserva);
            tvHabitacionReserva.setText(habitacionInfo);

            // Monto
            double monto = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
            tvMontoReserva.setText(String.format(Locale.getDefault(), "S/ %.2f", monto));

            // Estado
            String estado = reserva.getEstado() != null ? reserva.getEstado() : "sin_estado";
            configurarEstado(estado);
            Log.d("ReservaDetalleAdapter", "Estado: " + estado + ", Monto: " + monto);
        }

        private String obtenerFechasTexto(Reserva reserva) {
            try {
                // Intentar usar los timestamps
                if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
                    Date fechaInicio = new Date(reserva.getFechaInicio());
                    Date fechaFin = new Date(reserva.getFechaFin());

                    String inicioStr = dateFormat.format(fechaInicio);
                    String finStr = dateFormat.format(fechaFin);

                    // Calcular días
                    long diffInMillis = reserva.getFechaFin() - reserva.getFechaInicio();
                    int dias = (int) (diffInMillis / (1000 * 60 * 60 * 24));

                    return String.format("%s - %s (%d días)", inicioStr, finStr, dias);
                }

                // Fallback usando campos de texto si existen
                return "Fechas no disponibles";

            } catch (Exception e) {
                return "Fechas no disponibles";
            }
        }

        private String obtenerHabitacionInfo(Reserva reserva) {
            StringBuilder info = new StringBuilder("🏠 ");

            if (reserva.getHabitacionNumero() != null && !reserva.getHabitacionNumero().isEmpty()) {
                info.append("Hab. ").append(reserva.getHabitacionNumero());
            } else {
                info.append("Habitación no especificada");
            }

            if (reserva.getHabitacionTipo() != null && !reserva.getHabitacionTipo().isEmpty()) {
                info.append(" - ").append(reserva.getHabitacionTipo());
            }

            return info.toString();
        }

        private void configurarEstado(String estado) {
            if (estado == null) estado = "desconocido";

            int colorEstado;
            String textoEstado;

            switch (estado.toLowerCase()) {
                case "activa":
                case "confirmada":
                    colorEstado = ContextCompat.getColor(context, R.color.green_medium);
                    textoEstado = "ACTIVA";
                    break;
                case "completada":
                case "finalizada":
                    colorEstado = ContextCompat.getColor(context, R.color.blue);
                    textoEstado = "COMPLETADA";
                    break;
                case "cancelada":
                    colorEstado = ContextCompat.getColor(context, R.color.red_medium);
                    textoEstado = "CANCELADA";
                    break;
                case "pendiente":
                    colorEstado = ContextCompat.getColor(context, R.color.orange_dark);
                    textoEstado = "PENDIENTE";
                    break;
                default:
                    colorEstado = ContextCompat.getColor(context, R.color.gray_medium);
                    textoEstado = estado.toUpperCase();
                    break;
            }

            viewEstadoReserva.setBackgroundColor(colorEstado);
            tvEstadoTexto.setText(textoEstado);
            tvEstadoTexto.setTextColor(colorEstado);
        }
    }
}