package com.example.telehotel.features.taxista.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.telehotel.R;
import com.example.telehotel.data.model.Hotel;
import com.example.telehotel.features.taxista.TaxistaHotelDetalle;

import java.util.ArrayList;
import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hoteles;
    private final Context context;

    public HotelAdapter(List<Hotel> hoteles, Context context) {
        this.hoteles = hoteles != null ? hoteles : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.taxista_item_hotel, parent, false);
        return new HotelViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hoteles.get(position);
        holder.hotelName.setText(hotel.getNombre());

        // Dirección
        if (hotel.getUbicacion() != null && hotel.getUbicacion().getDireccion() != null) {
            holder.hotelLocation.setText(hotel.getUbicacion().getDireccion());
        } else {
            holder.hotelLocation.setText("Dirección no disponible");
        }

        // Imagen del hotel (usamos la primera si hay)
        List<String> imagenes = hotel.getImagenes();
        if (imagenes != null && !imagenes.isEmpty()) {
            Glide.with(context)
                    .load(imagenes.get(0))
                    .placeholder(R.drawable.sample_hotel) // imagen por defecto mientras carga
                    .error(R.drawable.sample_hotel)      // si falla la carga
                    .into(holder.hotelImage);
        } else {
            holder.hotelImage.setImageResource(R.drawable.sample_hotel); // por defecto si no hay imagen
        }

        // Listener para ver detalles del hotel
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaxistaHotelDetalle.class);
            intent.putExtra("hotel_id", hotel.getId());
            intent.putExtra("hotel_nombre", hotel.getNombre());
            intent.putExtra("hotel_direccion", hotel.getUbicacion() != null ? hotel.getUbicacion().getDireccion() : "");
            context.startActivity(intent);
        });
    }

    public void setHoteles(List<Hotel> nuevosHoteles) {
        Log.d("TaxistaHoteles", "Cantidad hoteles en adapter antes: " + getItemCount());
        this.hoteles = nuevosHoteles != null ? nuevosHoteles : new ArrayList<>();
        notifyDataSetChanged();
        Log.d("TaxistaHoteles", "Cantidad hoteles en adapter después: " + getItemCount());
    }

    @Override
    public int getItemCount() {
        return hoteles.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {

        TextView hotelName, hotelLocation;
        ImageView hotelImage;

        public HotelViewHolder(View itemView) {
            super(itemView);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelLocation = itemView.findViewById(R.id.textViewDescripcion);
            hotelImage = itemView.findViewById(R.id.imageView); // ← Asegúrate de tener este ID en tu layout
        }
    }
}
