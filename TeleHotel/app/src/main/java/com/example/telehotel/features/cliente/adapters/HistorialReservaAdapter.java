package com.example.telehotel.features.cliente.adapters;

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

        // Formatear fecha de reserva
        if (reserva.getFechaReserva() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            holder.fechaReserva.setText("Booked on: " + reserva.getFechaReserva().format(formatter));
        } else {
            holder.fechaReserva.setText("Fecha desconocida");
        }

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
            btnBookAgain = itemView.findViewById(R.id.btnRepetir); // <-- corregido
        }

    }
}