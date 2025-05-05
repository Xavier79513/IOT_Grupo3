package com.example.telehotel.features.taxista.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.ServicioTaxi;
import com.google.android.material.button.MaterialButton;

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

        // Asignar datos
        holder.userName.setText("Cliente ID: " + solicitud.getClienteId());
        holder.textViewRating.setText("⭐ 4.9 Reviews (200)"); // Opcional: usar campo real si lo tienes
        holder.textDestino.setText("Destino: " + solicitud.getAeropuertoDestino());
        holder.textRecojo.setText("Recojo: " + solicitud.getUbicacionTaxista().getDireccion());

        // Listeners de botones
        holder.btnAceptar.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Solicitud aceptada: " + solicitud.getId(), Toast.LENGTH_SHORT).show();
        });

        holder.btnRechazar.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Solicitud rechazada: " + solicitud.getId(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return solicitudes.size();
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        TextView userName, textViewRating, textDestino, textRecojo;
        MaterialButton btnAceptar, btnRechazar;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textDestino = itemView.findViewById(R.id.textDestino);
            textRecojo = itemView.findViewById(R.id.textRecojo);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}
