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

public class ServiciosAdapter extends RecyclerView.Adapter<ServiciosAdapter.ServicioViewHolder> {

    private List<Servicio> serviciosList;
    private OnServicioClickListener listener;

    public interface OnServicioClickListener {
        void onServicioClick(Servicio servicio);
    }

    public ServiciosAdapter(List<Servicio> serviciosList) {
        this.serviciosList = serviciosList;
    }

    public void setOnServicioClickListener(OnServicioClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_row, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        Servicio servicio = serviciosList.get(position);
        holder.bind(servicio);
    }

    @Override
    public int getItemCount() {
        return serviciosList.size();
    }

    public void updateServicios(List<Servicio> nuevosServicios) {
        this.serviciosList = nuevosServicios;
        notifyDataSetChanged();
    }

    public class ServicioViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgServiceIcon;
        private TextView tvServiceName;
        private TextView tvServiceDesc;
        private TextView tvServicePrice;

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);

            imgServiceIcon = itemView.findViewById(R.id.imgServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDesc = itemView.findViewById(R.id.tvServiceDesc);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);

            // Click listener para todo el item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onServicioClick(serviciosList.get(position));
                    }
                }
            });
        }

        public void bind(Servicio servicio) {
            tvServiceName.setText(servicio.getNombre());
            tvServiceDesc.setText(servicio.getDescripcion());
            tvServicePrice.setText("Precio: " + servicio.getPrecioFormateado());

            // Establecer ícono según la categoría
            setIconByCategory(servicio.getCategoria());
        }

        private void setIconByCategory(String categoria) {
            int iconResource;

            if (categoria == null) {
                iconResource = R.drawable.ic_services;
            } else {
                switch (categoria.toLowerCase()) {
                    case "alimentación":
                    case "desayuno":
                    case "restaurant":
                        iconResource = R.drawable.ic_breakfast;
                        break;
                    case "transporte":
                    case "taxi":
                        iconResource = R.drawable.ic_taxi;
                        break;
                    case "estacionamiento":
                        iconResource = R.drawable.ic_parking;
                        break;
                    case "lavandería":
                    case "limpieza":
                        iconResource = R.drawable.ic_lavanderia;
                        break;
                    case "spa":
                    case "bienestar":
                        iconResource = R.drawable.ic_spa;
                        break;
                    case "habitación":
                    case "room service":
                        iconResource = R.drawable.ic_room_service;
                        break;
                    default:
                        iconResource = R.drawable.ic_services;
                        break;
                }
            }

            imgServiceIcon.setImageResource(iconResource);
        }
    }
}