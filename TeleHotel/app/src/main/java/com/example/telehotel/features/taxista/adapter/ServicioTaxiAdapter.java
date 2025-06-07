package com.example.telehotel.features.taxista.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.ServicioTaxi;
import com.example.telehotel.data.repository.AeropuertoRepository;
import com.example.telehotel.data.repository.HotelRepository;
import com.example.telehotel.data.repository.UserRepository;
import com.example.telehotel.features.taxista.TaxistaDetalleViaje;

import java.util.List;

public class ServicioTaxiAdapter extends RecyclerView.Adapter<ServicioTaxiAdapter.SolicitudViewHolder> {

    private final List<ServicioTaxi> solicitudes;

    public ServicioTaxiAdapter(List<ServicioTaxi> solicitudes) {
        this.solicitudes = solicitudes;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.taxista_item_solicitud_usuario, parent, false);
        return new SolicitudViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        ServicioTaxi solicitud = solicitudes.get(position);

        // Estado: mostrar texto y cambiar fondo/color según estado
        String estado = solicitud.getEstado();
        if (estado == null) estado = "Desconocido";
        holder.textEstado.setText(estado);

        switch (estado.toLowerCase()) {
            case "terminado":
                holder.textEstado.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_700));
                holder.textEstado.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                break;
            case "cancelado":
                holder.textEstado.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray_500));
                holder.textEstado.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                break;
            case "en curso":
                holder.textEstado.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange_700));
                holder.textEstado.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                break;
            default:
                // Color gris para desconocido o cualquier otro estado
                holder.textEstado.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray_500));
                holder.textEstado.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                break;
        }

        // Obtener datos del cliente
        if (solicitud.getClienteId() != null && !solicitud.getClienteId().isEmpty()) {
            UserRepository.getUserByUid(solicitud.getClienteId(),
                    usuario -> {
                        String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
                        holder.userName.setText("Cliente: " + nombreCompleto);

                        double reputacion = usuario.getReputacion();
                        String reputacionStr = String.format("%.1f", reputacion);
                        holder.textViewRating.setText("⭐ " + reputacionStr + " estrellas");
                    },
                    error -> {
                        holder.userName.setText("Cliente: (Nombre no disponible)");
                        holder.textViewRating.setText("⭐ Reputación no disponible");
                    }
            );
        } else {
            holder.userName.setText("Cliente: (ID no disponible)");
            holder.textViewRating.setText("⭐ Reputación no disponible");
        }

        // Destino (nombre del aeropuerto)
        if (solicitud.getAeropuertoId() != null && !solicitud.getAeropuertoId().isEmpty()) {
            AeropuertoRepository.getByAeropuertoId(
                    solicitud.getAeropuertoId(),
                    aeropuerto -> holder.textDestino.setText("Destino: " + aeropuerto.getNombreAeropuerto()),
                    error -> holder.textDestino.setText("Destino: (Error cargando aeropuerto)")
            );
        } else {
            holder.textDestino.setText("Destino: (ID de aeropuerto no disponible)");
        }

        // Recojo (nombre del hotel)
        if (solicitud.getHotelId() != null && !solicitud.getHotelId().isEmpty()) {
            HotelRepository.getHotelByIdPorCampo(
                    solicitud.getHotelId(),
                    hotel -> holder.textRecojo.setText("Recojo: " + hotel.getNombre()),
                    error -> holder.textRecojo.setText("Recojo: (Error cargando hotel)")
            );
        } else {
            holder.textRecojo.setText("Recojo: (ID de hotel no disponible)");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), TaxistaDetalleViaje.class);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return solicitudes.size();
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        TextView userName, textViewRating, textDestino, textRecojo, textEstado;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textDestino = itemView.findViewById(R.id.textDestino);
            textRecojo = itemView.findViewById(R.id.textRecojo);
            textEstado = itemView.findViewById(R.id.textEstado);
        }
    }
}
