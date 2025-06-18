package com.example.telehotel.features.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Habitacion;

import java.util.List;

public class HabitacionesAdapter extends RecyclerView.Adapter<HabitacionesAdapter.HabitacionViewHolder> {

    private List<Habitacion> habitaciones;

    public HabitacionesAdapter(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }

    @NonNull
    @Override
    public HabitacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habitacion, parent, false);
        return new HabitacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitacionViewHolder holder, int position) {
        Habitacion habitacion = habitaciones.get(position);
        holder.bind(habitacion);
    }

    @Override
    public int getItemCount() {
        return habitaciones.size();
    }

    public static class HabitacionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNumero, tvTipo, tvCapacidad, tvTamaño, tvPrecio, tvEstado;

        public HabitacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvCapacidad = itemView.findViewById(R.id.tvCapacidad);
            tvTamaño = itemView.findViewById(R.id.tvTamaño);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }

        public void bind(Habitacion habitacion) {
            tvNumero.setText("Habitación " + habitacion.getNumero());
            tvTipo.setText(habitacion.getTipoFormateado());
            tvCapacidad.setText(habitacion.getCapacidadTexto());
            tvTamaño.setText(habitacion.getTamaño() + " m²");
            tvPrecio.setText(habitacion.getPrecioFormateado());
            tvEstado.setText(habitacion.getEstado().toUpperCase());

            // Color del estado
            switch (habitacion.getEstado().toLowerCase()) {
                case "disponible":
                    tvEstado.setTextColor(Color.parseColor("#4CAF50")); // Verde
                    break;
                case "ocupada":
                    tvEstado.setTextColor(Color.parseColor("#F44336")); // Rojo
                    break;
                case "mantenimiento":
                    tvEstado.setTextColor(Color.parseColor("#FF9800")); // Naranja
                    break;
                default:
                    tvEstado.setTextColor(Color.parseColor("#666666")); // Gris
                    break;
            }
        }
    }
}