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
import com.example.telehotel.data.model.Reserva;

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

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        holder.nombreHotel.setText(reserva.getNombreHotel());
        holder.precio.setText("$" + reserva.getPrecio());
        holder.fechaReserva.setText("Booked on : " + reserva.getFechaReserva());

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