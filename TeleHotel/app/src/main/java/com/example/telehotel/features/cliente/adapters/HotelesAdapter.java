package com.example.telehotel.features.cliente.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;

import java.util.List;

public class HotelesAdapter extends RecyclerView.Adapter<HotelesAdapter.ViewHolder> {
    private List<HotelItem> items;

    public static class HotelItem {
        public int imageResId;
        public String titulo;

        public HotelItem(int imageResId, String titulo) {
            this.imageResId = imageResId;
            this.titulo = titulo;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView titulo;

        public ViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.imgItem);
            titulo = view.findViewById(R.id.txtItem);
        }
    }

    public HotelesAdapter(List<HotelItem> items) {
        this.items = items;
    }

    @Override
    public HotelesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lugares, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HotelItem item = items.get(position);
        holder.img.setImageResource(item.imageResId);
        holder.titulo.setText(item.titulo);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
