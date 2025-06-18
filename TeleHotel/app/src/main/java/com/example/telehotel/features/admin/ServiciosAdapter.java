package com.example.telehotel.features.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Servicio;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Servicio;

import java.util.List;

public class ServiciosAdapter extends RecyclerView.Adapter<ServiciosAdapter.ServicioViewHolder> {

    private List<Servicio> servicios;

    public ServiciosAdapter(List<Servicio> servicios) {
        this.servicios = servicios;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_servicio, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        Servicio servicio = servicios.get(position);
        holder.bind(servicio);
    }

    @Override
    public int getItemCount() {
        return servicios.size();
    }

    public static class ServicioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre, tvDescripcion, tvPrecio, tvEstado, tvImagenes;

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvImagenes = itemView.findViewById(R.id.tvImagenes);
        }

        public void bind(Servicio servicio) {
            tvNombre.setText(servicio.getNombre());
            tvDescripcion.setText(servicio.getDescripcion());
            tvPrecio.setText(servicio.getPrecioFormateado());
            tvEstado.setText(servicio.getEstadoTexto());

            // Color del precio según si es gratuito o no
            if (servicio.esGratuito()) {
                tvPrecio.setTextColor(Color.parseColor("#4CAF50")); // Verde para gratuito
            } else {
                tvPrecio.setTextColor(Color.parseColor("#FF9800")); // Naranja para con costo
            }

            // Color del estado
            if (servicio.isDisponible()) {
                tvEstado.setTextColor(Color.parseColor("#4CAF50")); // Verde
            } else {
                tvEstado.setTextColor(Color.parseColor("#F44336")); // Rojo
            }

            // Información de imágenes
            if (servicio.tieneImagenes()) {
                tvImagenes.setText(servicio.getCantidadImagenes() + " imagen(es)");
                tvImagenes.setTextColor(Color.parseColor("#2196F3")); // Azul
            } else {
                tvImagenes.setText("Sin imágenes");
                tvImagenes.setTextColor(Color.parseColor("#666666")); // Gris
            }
        }
    }
}