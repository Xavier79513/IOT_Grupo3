/*package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.data.model.Reserva;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistorialReservaAdapter extends RecyclerView.Adapter<HistorialReservaAdapter.ReservaViewHolder> {

    private List<Reserva> reservas;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Reserva reserva);
    }

    public HistorialReservaAdapter(List<Reserva> reservas, Context context, OnItemClickListener listener) {
        this.reservas = reservas;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    ArrayList<Hotel> listaHotelesDisponibles = new ArrayList<>();
    private String obtenerNombreHotelPorId(String hotelId) {
        for (Hotel h : listaHotelesDisponibles) { // Asegúrate de tener esta lista cargada antes
            if (h.getId().equals(hotelId)) {
                return h.getNombre();
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);

        // Mostrar nombre del hotel: necesitas obtenerlo con hotelId
        String hotelName = obtenerNombreHotelPorId(reserva.getHotelId());
        holder.nombreHotel.setText(hotelName != null ? hotelName : "Hotel desconocido");

        // Mostrar monto total
        holder.precio.setText("$" + String.format("%.2f", reserva.getMontoTotal()));

        // Acción al pulsar el botón
        holder.btnBookAgain.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(reserva);
            }
        });
    }


    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public void eliminarItem(int position) {
        reservas.remove(position);
        notifyItemRemoved(position);
    }

    public static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView nombreHotel, precio, fechaReserva;
        Button btnBookAgain;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreHotel = itemView.findViewById(R.id.txtNombreHotel); // <-- corregido
            precio = itemView.findViewById(R.id.txtPrecio); // <-- corregido
            fechaReserva = itemView.findViewById(R.id.txtFecha); // <-- corregido
        }

    }
}*/
/*package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialReservaAdapter extends RecyclerView.Adapter<HistorialReservaAdapter.ReservaViewHolder> {

    private List<Reserva> reservas;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Reserva reserva);
    }

    public HistorialReservaAdapter(List<Reserva> reservas, Context context, OnItemClickListener listener) {
        this.reservas = reservas;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);

        // Nombre del hotel (ya está guardado en la reserva)
        holder.tvHotelName.setText(reserva.getHotelNombre() != null ?
                reserva.getHotelNombre() : "Hotel no especificado");

        // Formatear fechas
        String fechasTexto = formatearFechas(reserva.getFechaInicio(), reserva.getFechaFin());
        holder.tvFechas.setText(fechasTexto);

        // Mostrar número de noches
        int noches = reserva.getTotalDias() != null ? reserva.getTotalDias() : 1;
        String nochesTexto = noches + (noches == 1 ? " noche" : " noches");
        holder.tvNoches.setText("• " + nochesTexto);

        // Mostrar huéspedes
        String huespedes = reserva.getHuespedes() != null ?
                reserva.getHuespedes() : "No especificado";
        holder.tvHuespedes.setText(huespedes);

        // Tipo de habitación
        String tipoHabitacion = reserva.getHabitacionTipo() != null ?
                reserva.getHabitacionTipo() : "Habitación estándar";
        holder.tvTipoHabitacion.setText("• " + tipoHabitacion);

        // Precio total
        double precio = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "S/ %.2f", precio));

        // Click en toda la card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(reserva);
            }
        });
    }

    private String formatearFechas(Long fechaInicio, Long fechaFin) {
        if (fechaInicio == null || fechaFin == null || fechaInicio == 0 || fechaFin == 0) {
            return "Fechas no disponibles";
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String inicio = formatter.format(new Date(fechaInicio));
            String fin = formatter.format(new Date(fechaFin));
            return inicio + " - " + fin;
        } catch (Exception e) {
            return "Fechas no válidas";
        }
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public void eliminarItem(int position) {
        reservas.remove(position);
        notifyItemRemoved(position);
    }

    public static class ReservaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName, tvFechas, tvNoches, tvHuespedes, tvTipoHabitacion, tvPrecio;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);

            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvFechas = itemView.findViewById(R.id.tvFechas);
            tvNoches = itemView.findViewById(R.id.tvNoches);
            tvHuespedes = itemView.findViewById(R.id.tvHuespedes);
            tvTipoHabitacion = itemView.findViewById(R.id.tvTipoHabitacion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
        }
    }
}*/
package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Reserva;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialReservaAdapter extends RecyclerView.Adapter<HistorialReservaAdapter.ReservaViewHolder> {

    private List<Reserva> reservas;
    private Context context;
    private OnItemClickListener listener;
    private OnCheckoutClickListener checkoutListener;

    public interface OnItemClickListener {
        void onItemClick(Reserva reserva);
    }

    public interface OnCheckoutClickListener {
        void onCheckoutClick(Reserva reserva);
    }

    public HistorialReservaAdapter(List<Reserva> reservas, Context context,
                                   OnItemClickListener listener, OnCheckoutClickListener checkoutListener) {
        this.reservas = reservas;
        this.context = context;
        this.listener = listener;
        this.checkoutListener = checkoutListener;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);

        // Nombre del hotel
        holder.tvHotelName.setText(reserva.getHotelNombre() != null ?
                reserva.getHotelNombre() : "Hotel no especificado");

        // Formatear fechas
        String fechasTexto = formatearFechas(reserva.getFechaInicio(), reserva.getFechaFin());
        holder.tvFechas.setText(fechasTexto);

        // Mostrar número de noches
        int noches = reserva.getTotalDias() != null ? reserva.getTotalDias() : 1;
        String nochesTexto = noches + (noches == 1 ? " noche" : " noches");
        holder.tvNoches.setText("• " + nochesTexto);

        // Mostrar huéspedes
        String huespedes = reserva.getHuespedes() != null ?
                reserva.getHuespedes() : "No especificado";
        holder.tvHuespedes.setText(huespedes);

        // Tipo de habitación
        String tipoHabitacion = reserva.getHabitacionTipo() != null ?
                reserva.getHabitacionTipo() : "Habitación estándar";
        holder.tvTipoHabitacion.setText("• " + tipoHabitacion);

        // Precio total
        double precio = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : 0.0;
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "S/ %.2f", precio));

        // Configurar botón de checkout
        configurarBotonCheckout(holder, reserva);

        // Click en toda la card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(reserva);
            }
        });
    }

    private void configurarBotonCheckout(ReservaViewHolder holder, Reserva reserva) {
        long fechaActual = System.currentTimeMillis();
        long fechaInicio = reserva.getFechaInicio() != null ? reserva.getFechaInicio() : 0;
        long fechaFin = reserva.getFechaFin() != null ? reserva.getFechaFin() : 0;

        String estadoReserva = reserva.getEstado() != null ? reserva.getEstado() : "";

        // Verificar si ya se hizo checkout o la reserva está cancelada
        if ("completada".equals(estadoReserva) || "cancelada".equals(estadoReserva) ||
                "checkout_solicitado".equals(estadoReserva) || "checkout_procesando".equals(estadoReserva)) {
            // Ocultar o deshabilitar botón
            holder.btnCheckout.setVisibility(View.GONE);
            return;
        }

        // Mostrar botón solo si la reserva está activa
        if ("activa".equals(estadoReserva)) {
            holder.btnCheckout.setVisibility(View.VISIBLE);

            // Verificar si está en el rango de fechas para checkout
            if (fechaActual >= fechaInicio && fechaActual <= fechaFin) {
                // Puede hacer checkout
                holder.btnCheckout.setEnabled(true);
                holder.btnCheckout.setText("Checkout");
                holder.btnCheckout.setOnClickListener(v -> {
                    if (checkoutListener != null) {
                        checkoutListener.onCheckoutClick(reserva);
                    }
                });
            } else if (fechaActual < fechaInicio) {
                // Muy temprano para checkout
                holder.btnCheckout.setEnabled(false);
                holder.btnCheckout.setText("Checkout");
                holder.btnCheckout.setOnClickListener(v -> {
                    String fechaInicioStr = formatearFechaSola(fechaInicio);
                    Toast.makeText(context,
                            "Podrás realizar el checkout desde el " + fechaInicioStr,
                            Toast.LENGTH_LONG).show();
                });
            } else {
                // Ya pasó la fecha de checkout
                holder.btnCheckout.setVisibility(View.GONE);
            }
        } else {
            holder.btnCheckout.setVisibility(View.GONE);
        }
    }

    private String formatearFechas(Long fechaInicio, Long fechaFin) {
        if (fechaInicio == null || fechaFin == null || fechaInicio == 0 || fechaFin == 0) {
            return "Fechas no disponibles";
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String inicio = formatter.format(new Date(fechaInicio));
            String fin = formatter.format(new Date(fechaFin));
            return inicio + " - " + fin;
        } catch (Exception e) {
            return "Fechas no válidas";
        }
    }

    private String formatearFechaSola(Long fecha) {
        if (fecha == null || fecha == 0) {
            return "fecha no disponible";
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());
            return formatter.format(new Date(fecha));
        } catch (Exception e) {
            return "fecha no válida";
        }
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public void eliminarItem(int position) {
        reservas.remove(position);
        notifyItemRemoved(position);
    }

    public void actualizarReserva(Reserva reservaActualizada) {
        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getId().equals(reservaActualizada.getId())) {
                reservas.set(i, reservaActualizada);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public static class ReservaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName, tvFechas, tvNoches, tvHuespedes, tvTipoHabitacion, tvPrecio;
        Button btnCheckout;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);

            ivHotelImage = itemView.findViewById(R.id.ivHotelImage);
            tvHotelName = itemView.findViewById(R.id.tvHotelName);
            tvFechas = itemView.findViewById(R.id.tvFechas);
            tvNoches = itemView.findViewById(R.id.tvNoches);
            tvHuespedes = itemView.findViewById(R.id.tvHuespedes);
            tvTipoHabitacion = itemView.findViewById(R.id.tvTipoHabitacion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            btnCheckout = itemView.findViewById(R.id.btnCheckout);
        }
    }
}