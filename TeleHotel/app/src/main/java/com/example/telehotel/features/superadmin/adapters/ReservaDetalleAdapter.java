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

    private static final String TAG = "ReservaDetalleAdapter";
    private List<Reserva> reservasList;
    private Context context;
    private SimpleDateFormat dateFormat;

    public ReservaDetalleAdapter(Context context, List<Reserva> reservasList) {
        this.context = context;
        this.reservasList = reservasList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Log.d(TAG, "=== CONSTRUCTOR ADAPTER ===");
        Log.d(TAG, "Context: " + (context != null ? "✅" : "❌"));
        Log.d(TAG, "Lista de reservas: " + (reservasList != null ? reservasList.size() + " items" : "null"));
        Log.d(TAG, "=== FIN CONSTRUCTOR ===");
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "=== onCreateViewHolder ===");
        Log.d(TAG, "Inflando layout: item_reserva_detalle");

        try {
            View view = LayoutInflater.from(context).inflate(R.layout.item_reserva_detalle, parent, false);
            Log.d(TAG, "✅ Layout inflado correctamente");
            Log.d(TAG, "View dimensions: " + view.getLayoutParams());
            return new ReservaViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inflando layout", e);
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Log.d(TAG, "=== onBindViewHolder ===");
        Log.d(TAG, "Position: " + position);
        Log.d(TAG, "Total items: " + getItemCount());

        if (reservasList == null || position >= reservasList.size()) {
            Log.e(TAG, "❌ Lista es null o posición inválida");
            return;
        }

        Reserva reserva = reservasList.get(position);
        Log.d(TAG, "Reserva a bindear: " + (reserva != null ? reserva.getId() : "null"));

        holder.bind(reserva);
        Log.d(TAG, "✅ Bind completado para posición " + position);
    }

    @Override
    public int getItemCount() {
        int count = reservasList != null ? reservasList.size() : 0;
        Log.d(TAG, "getItemCount(): " + count);
        return count;
    }

    public void updateList(List<Reserva> newList) {
        Log.d(TAG, "=== updateList ===");
        Log.d(TAG, "Nueva lista: " + (newList != null ? newList.size() + " items" : "null"));
        Log.d(TAG, "Lista anterior: " + (this.reservasList != null ? this.reservasList.size() + " items" : "null"));

        this.reservasList = newList;
        notifyDataSetChanged();

        Log.d(TAG, "✅ Lista actualizada y notifyDataSetChanged() llamado");
        Log.d(TAG, "Nuevo getItemCount(): " + getItemCount());
    }

    public class ReservaViewHolder extends RecyclerView.ViewHolder {

        private TextView tvClienteNombre, tvFechasReserva, tvHabitacionReserva;
        private TextView tvMontoReserva, tvEstadoTexto;
        private View viewEstadoReserva;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "=== ReservaViewHolder Constructor ===");
            initViews();
        }

        private void initViews() {
            Log.d(TAG, "=== initViews ===");

            try {
                tvClienteNombre = itemView.findViewById(R.id.tvClienteNombre);
                tvFechasReserva = itemView.findViewById(R.id.tvFechasReserva);
                tvHabitacionReserva = itemView.findViewById(R.id.tvHabitacionReserva);
                tvMontoReserva = itemView.findViewById(R.id.tvMontoReserva);
                tvEstadoTexto = itemView.findViewById(R.id.tvEstadoTexto);
                viewEstadoReserva = itemView.findViewById(R.id.viewEstadoReserva);

                // Verificar que todas las vistas se encontraron
                Log.d(TAG, "tvClienteNombre: " + (tvClienteNombre != null ? "✅" : "❌"));
                Log.d(TAG, "tvFechasReserva: " + (tvFechasReserva != null ? "✅" : "❌"));
                Log.d(TAG, "tvHabitacionReserva: " + (tvHabitacionReserva != null ? "✅" : "❌"));
                Log.d(TAG, "tvMontoReserva: " + (tvMontoReserva != null ? "✅" : "❌"));
                Log.d(TAG, "tvEstadoTexto: " + (tvEstadoTexto != null ? "✅" : "❌"));
                Log.d(TAG, "viewEstadoReserva: " + (viewEstadoReserva != null ? "✅" : "❌"));

                // Verificar si alguna vista es null
                if (tvClienteNombre == null || tvFechasReserva == null || tvHabitacionReserva == null ||
                        tvMontoReserva == null || tvEstadoTexto == null || viewEstadoReserva == null) {
                    Log.e(TAG, "❌ ERROR: Algunas vistas son null. Verificar item_reserva_detalle.xml");
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Error en initViews", e);
            }
        }

        public void bind(Reserva reserva) {
            Log.d(TAG, "=== bind() ===");
            Log.d(TAG, "Reserva ID: " + (reserva != null ? reserva.getId() : "null"));

            if (reserva == null) {
                Log.e(TAG, "❌ Reserva es null, no se puede hacer bind");
                return;
            }

            try {
                // Nombre del cliente
                String nombreCliente = reserva.getClienteNombre();
                if (nombreCliente == null || nombreCliente.isEmpty()) {
                    nombreCliente = reserva.getClienteEmail();
                    if (nombreCliente == null || nombreCliente.isEmpty()) {
                        nombreCliente = "Cliente ID: " + (reserva.getClienteId() != null ? reserva.getClienteId() : "Desconocido");
                    }
                }

                if (tvClienteNombre != null) {
                    tvClienteNombre.setText(nombreCliente);
                    Log.d(TAG, "✅ Cliente nombre seteado: " + nombreCliente);
                } else {
                    Log.e(TAG, "❌ tvClienteNombre es null");
                }

                // Fechas de la reserva
                String fechasTexto = obtenerFechasTexto(reserva);
                if (tvFechasReserva != null) {
                    tvFechasReserva.setText(fechasTexto);
                    Log.d(TAG, "✅ Fechas seteadas: " + fechasTexto);
                } else {
                    Log.e(TAG, "❌ tvFechasReserva es null");
                }

                // Información de la habitación
                String habitacionInfo = obtenerHabitacionInfo(reserva);
                if (tvHabitacionReserva != null) {
                    tvHabitacionReserva.setText(habitacionInfo);
                    Log.d(TAG, "✅ Habitación seteada: " + habitacionInfo);
                } else {
                    Log.e(TAG, "❌ tvHabitacionReserva es null");
                }

                // Monto
                double monto = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
                String montoTexto = String.format(Locale.getDefault(), "S/ %.2f", monto);
                if (tvMontoReserva != null) {
                    tvMontoReserva.setText(montoTexto);
                    Log.d(TAG, "✅ Monto seteado: " + montoTexto);
                } else {
                    Log.e(TAG, "❌ tvMontoReserva es null");
                }

                // Estado
                String estado = reserva.getEstado() != null ? reserva.getEstado() : "sin_estado";
                configurarEstado(estado);

                Log.d(TAG, "✅ Bind completado exitosamente");

            } catch (Exception e) {
                Log.e(TAG, "❌ Error en bind()", e);
            }
        }

        private String obtenerFechasTexto(Reserva reserva) {
            try {
                if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
                    Date fechaInicio = new Date(reserva.getFechaInicio());
                    Date fechaFin = new Date(reserva.getFechaFin());

                    String inicioStr = dateFormat.format(fechaInicio);
                    String finStr = dateFormat.format(fechaFin);

                    long diffInMillis = reserva.getFechaFin() - reserva.getFechaInicio();
                    int dias = (int) (diffInMillis / (1000 * 60 * 60 * 24));

                    return String.format("%s - %s (%d días)", inicioStr, finStr, dias);
                }
                return "Fechas no disponibles";
            } catch (Exception e) {
                Log.e(TAG, "Error obteniendo fechas", e);
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
                case "checkout_listo":
                    colorEstado = ContextCompat.getColor(context, R.color.orange);
                    textoEstado = "CHECKOUT LISTO";
                    break;
                case "cancelada":
                    colorEstado = ContextCompat.getColor(context, R.color.red_medium);
                    textoEstado = "CANCELADA";
                    break;
                case "pendiente":
                    colorEstado = ContextCompat.getColor(context, R.color.orange_700);
                    textoEstado = "PENDIENTE";
                    break;
                default:
                    colorEstado = ContextCompat.getColor(context, R.color.gray_medium);
                    textoEstado = estado.toUpperCase();
                    break;
            }

            if (viewEstadoReserva != null) {
                viewEstadoReserva.setBackgroundColor(colorEstado);
                Log.d(TAG, "✅ Color de estado seteado");
            } else {
                Log.e(TAG, "❌ viewEstadoReserva es null");
            }

            if (tvEstadoTexto != null) {
                tvEstadoTexto.setText(textoEstado);
                tvEstadoTexto.setTextColor(colorEstado);
                Log.d(TAG, "✅ Texto estado seteado: " + textoEstado);
            } else {
                Log.e(TAG, "❌ tvEstadoTexto es null");
            }
        }
    }
}