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

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.BookingActivity;
import com.example.telehotel.features.cliente.Hotel;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cliente_item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);
        holder.hotelName.setText(hotel.getNombre());
        holder.hotelRating.setText(hotel.getRating());
        holder.hotelDescription.setText(hotel.getDescripcion());
        holder.hotelDiscount.setText(hotel.getDescuento());
        holder.hotelPrice.setText(hotel.getPrecio());

        holder.btnBookNow.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, BookingActivity.class);
            intent.putExtra("hotelName", hotel.getNombre());
            intent.putExtra("rating", hotel.getRating());
            intent.putExtra("descripcion", hotel.getDescripcion());
            intent.putExtra("precio", hotel.getPrecio());
            intent.putExtra("descuento", hotel.getDescuento());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView hotelName, hotelRating, hotelDescription, hotelDiscount, hotelPrice;
        MaterialButton btnBookNow;
        ImageView hotelImage;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelRating = itemView.findViewById(R.id.hotelRating);
            hotelDescription = itemView.findViewById(R.id.hotelDescription);
            hotelDiscount = itemView.findViewById(R.id.hotelDiscount);
            hotelPrice = itemView.findViewById(R.id.hotelPrice);
            btnBookNow = itemView.findViewById(R.id.btnBookNow);
            hotelImage = itemView.findViewById(R.id.hotelImage);
        }
    }
}
