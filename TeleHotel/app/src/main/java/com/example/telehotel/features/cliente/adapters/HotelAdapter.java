package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.telehotel.features.cliente.HotelDetailActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;

    public HotelAdapter(List<Hotel> hotelList) {
        this.hotelList = hotelList;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cliente_item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        holder.hotelName.setText(hotel.getNombre());
        holder.hotelDescription.setText(hotel.getDescripcion());
        holder.hotelPrice.setText("Desde $" + hotel.getMontoMinimoTaxi());

        // Cargar imagen desde Firebase (solo la primera)
        if (hotel.getImagenes() != null && !hotel.getImagenes().isEmpty()) {
            Glide.with(holder.hotelImage.getContext())
                    .load(hotel.getImagenes().get(0))
                    .placeholder(R.drawable.hotel_placeholder)
                    .into(holder.hotelImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, HotelDetailActivity.class);
            intent.putExtra("hotelNombre", hotel.getNombre());
            intent.putExtra("descripcion", hotel.getDescripcion());
            intent.putExtra("precio", hotel.getMontoMinimoTaxi());
            if (hotel.getImagenes() != null && !hotel.getImagenes().isEmpty()) {
                intent.putExtra("imagenUrl", hotel.getImagenes().get(0));
            }
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView hotelName, hotelDescription, hotelDiscount, hotelPrice;
        MaterialButton btnBookNow;
        ImageView hotelImage;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelDescription = itemView.findViewById(R.id.hotelDescription);
            hotelDiscount = itemView.findViewById(R.id.hotelDiscount);
            hotelPrice = itemView.findViewById(R.id.hotelPrice);
            btnBookNow = itemView.findViewById(R.id.btnBookNow);
            hotelImage = itemView.findViewById(R.id.hotelImage);
        }
    }
}
