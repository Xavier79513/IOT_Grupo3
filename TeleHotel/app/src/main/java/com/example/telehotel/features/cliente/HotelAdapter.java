package com.example.telehotel.features.cliente;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private List<Hotel> hotelList;
    private Context context;

    public HotelAdapter(Context context, List<Hotel> hotelList) {
        this.context = context;
        this.hotelList = hotelList;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        holder.nombre.setText(hotel.getNombre());
        holder.rating.setText(hotel.getRating());
        holder.descripcion.setText(hotel.getDescripcion());
        holder.descuento.setText(hotel.getDescuento());
        holder.precio.setText(hotel.getPrecio());

        /*holder.bookNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, HotelDetailActivity.class);
            context.startActivity(intent);
        });*/
        holder.bookNowButton.setOnClickListener(v -> {
            Toast.makeText(context, "Click en Book Now", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, HotelDetailActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, rating, descripcion, descuento, precio;
        MaterialButton bookNowButton;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.textViewHotelName); // Asegúrate de tener los IDs correctos
            rating = itemView.findViewById(R.id.textViewRating);
            descripcion = itemView.findViewById(R.id.textViewDescripcion);
            descuento = itemView.findViewById(R.id.textViewDescuento);
            precio = itemView.findViewById(R.id.textViewPrecio);
            bookNowButton = itemView.findViewById(R.id.bookNowButton);
        }
    }
}
