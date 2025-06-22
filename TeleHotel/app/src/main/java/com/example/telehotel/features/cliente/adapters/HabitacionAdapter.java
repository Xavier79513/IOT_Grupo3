package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Habitacion;

import java.util.List;

public class HabitacionAdapter extends RecyclerView.Adapter<HabitacionAdapter.ViewHolder> {

    public interface OnHabitacionClickListener {
        void onHabitacionClick(Habitacion habitacion);
    }

    private final List<Habitacion> lista;
    private final Context context;
    private final OnHabitacionClickListener listener;

    public HabitacionAdapter(Context context, List<Habitacion> lista, OnHabitacionClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_habitacion, parent, false);
        return new ViewHolder(view);
    }

    /*@Override
    public void onBindViewHolder(@NonNull HabitacionAdapter.ViewHolder holder, int position) {
        Habitacion hab = lista.get(position);
        holder.tvRoomTitle.setText(hab.getDescripcion());
        holder.tvRoomDetails.setText("Tipo: " + hab.getTipo() + " - Número: " + hab.getNumero());
        holder.tvPolicy.setText(hab.getEstado().equalsIgnoreCase("disponible") ? "Disponible" : "No disponible");
        holder.tvPrice.setText(String.format("S/ %.2f", hab.getPrecio()));

        holder.btnSelect.setEnabled(hab.getEstado().equalsIgnoreCase("disponible"));
        holder.btnSelect.setOnClickListener(v -> listener.onHabitacionClick(hab));
    }*/
    @Override
    public void onBindViewHolder(@NonNull HabitacionAdapter.ViewHolder holder, int position) {
        Habitacion hab = lista.get(position);

        // Título
        holder.tvRoomTitle.setText(hab.getDescripcion() != null ? hab.getDescripcion() : "Habitación " + hab.getNumero());

        // Detalles - AGREGAR CAPACIDAD Y TAMAÑO
        StringBuilder detalles = new StringBuilder();
        detalles.append("Tipo: ").append(hab.getTipo()).append(" - Número: ").append(hab.getNumero());

        // Agregar capacidad
        if (hab.getCapacidadAdultos() != null || hab.getCapacidadNinos() != null) {
            int adultos = hab.getCapacidadAdultos() != null ? hab.getCapacidadAdultos() : 0;
            int ninos = hab.getCapacidadNinos() != null ? hab.getCapacidadNinos() : 0;
            detalles.append("\nCapacidad: ").append(adultos).append(" adultos, ").append(ninos).append(" niños");
        }

        // Agregar tamaño
        if (hab.getTamaño() != null && hab.getTamaño() > 0) {
            detalles.append("\nTamaño: ").append(String.format("%.0f m²", hab.getTamaño()));
        }

        holder.tvRoomDetails.setText(detalles.toString());

        // Resto del código igual
        holder.tvPolicy.setText(hab.getEstado().equalsIgnoreCase("disponible") ? "Disponible" : "No disponible");
        holder.tvPrice.setText(String.format("S/ %.2f", hab.getPrecio()));

        holder.btnSelect.setEnabled(hab.getEstado().equalsIgnoreCase("disponible"));
        holder.btnSelect.setOnClickListener(v -> listener.onHabitacionClick(hab));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomTitle, tvRoomDetails, tvBenefits, tvPolicy, tvDiscount, tvPrice, tvTaxes, tvWarning;
        Button btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
            tvPolicy = itemView.findViewById(R.id.tvPolicy);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }
}