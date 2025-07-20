package com.example.telehotel.features.cliente.adapters;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Aeropuerto;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class AirportAdapter extends RecyclerView.Adapter<AirportAdapter.AirportViewHolder> {

    private final List<Aeropuerto> airports;
    private final OnAirportClickListener listener;
    private LatLng userLocation; // Para calcular distancias

    public interface OnAirportClickListener {
        void onAirportClick(Aeropuerto aeropuerto);
    }

    public AirportAdapter(List<Aeropuerto> airports, OnAirportClickListener listener) {
        this.airports = airports;
        this.listener = listener;
    }

    public void setUserLocation(LatLng userLocation) {
        this.userLocation = userLocation;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AirportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cliente_item_airport_option, parent, false);
        return new AirportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AirportViewHolder holder, int position) {
        Aeropuerto aeropuerto = airports.get(position);
        holder.bind(aeropuerto);
    }

    @Override
    public int getItemCount() {
        return airports.size();
    }

    class AirportViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAirportName;
        private final TextView tvAirportDistance;

        public AirportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAirportName = itemView.findViewById(R.id.tv_airport_name);
            tvAirportDistance = itemView.findViewById(R.id.tv_airport_distance);
        }

        public void bind(Aeropuerto aeropuerto) {
            tvAirportName.setText(aeropuerto.getNombreAeropuerto());

            // Calcular y mostrar distancia si tenemos la ubicación del usuario
            String distanceInfo = calculateDistanceInfo(aeropuerto);
            tvAirportDistance.setText(distanceInfo);

            // Configurar click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAirportClick(aeropuerto);
                }
            });
        }

        private String calculateDistanceInfo(Aeropuerto aeropuerto) {
            if (userLocation == null) {
                return "Distancia no disponible";
            }

            LatLng airportLocation = getAirportCoordinates(aeropuerto.getAeropuertoId());
            if (airportLocation == null) {
                return "Ubicación no disponible";
            }

            // Calcular distancia
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    airportLocation.latitude, airportLocation.longitude,
                    results
            );

            float distanceKm = results[0] / 1000;
            int estimatedMinutes = Math.round(distanceKm * 2.5f); // Estimación: 2.5 min por km

            return String.format("%.1f km • %d min", distanceKm, estimatedMinutes);
        }

        private LatLng getAirportCoordinates(String airportId) {
            // Mismas coordenadas que en TaxiPickupActivity
            switch (airportId) {
                case "jorge_chavez":
                    return new LatLng(-12.0219, -77.1143);
                case "collique":
                    return new LatLng(-11.7669, -77.1089);
                case "santa_maria":
                    return new LatLng(-12.0567, -76.9608);
                default:
                    return new LatLng(-12.0219, -77.1143);
            }
        }
    }
}