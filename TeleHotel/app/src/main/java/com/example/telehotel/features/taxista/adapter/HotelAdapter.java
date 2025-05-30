package com.example.telehotel.features.taxista.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.features.taxista.TaxistaHotelDetalle;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private final List<Hotel> hoteles;
    private final Context context;

    public HotelAdapter(List<Hotel> hoteles, Context context) {
        this.hoteles = hoteles;
        this.context = context;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout del item del hotel
        View itemView = LayoutInflater.from(context).inflate(R.layout.taxista_item_hotel, parent, false);
        return new HotelViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hoteles.get(position);
        holder.hotelName.setText(hotel.getNombre());
        holder.hotelLocation.setText(hotel.getUbicacion().getDireccion());

        // Al hacer clic en el hotel, navegamos a la actividad de detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaxistaHotelDetalle.class);
            intent.putExtra("hotel_id", hotel.getId()); // Pasamos el ID del hotel
            intent.putExtra("hotel_nombre", hotel.getNombre()); // Pasamos el nombre del hotel
            intent.putExtra("hotel_direccion", hotel.getUbicacion().getDireccion()); // Pasamos la dirección
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hoteles.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {

        TextView hotelName, hotelLocation;  // hotelLocation se cambia a textViewDescripcion

        public HotelViewHolder(View itemView) {
            super(itemView);
            hotelName = itemView.findViewById(R.id.hotelName);  // Este es el ID para el nombre del hotel
            hotelLocation = itemView.findViewById(R.id.textViewDescripcion);  // Cambiado a textViewDescripcion
        }
    }
}
