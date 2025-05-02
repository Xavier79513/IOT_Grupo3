package com.example.telehotel.features.cliente.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;

import java.util.List;

public class LugaresAdapter extends RecyclerView.Adapter<LugaresAdapter.ViewHolder> {
    private List<LugarItem> items;

    public static class LugarItem {
        public int imageResId;
        public String titulo;

        public LugarItem(int imageResId, String titulo) {
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

    public LugaresAdapter(List<LugarItem> items) {
        this.items = items;
    }

    @Override
    public LugaresAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lugares, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LugarItem item = items.get(position);
        holder.img.setImageResource(item.imageResId);
        holder.titulo.setText(item.titulo);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
