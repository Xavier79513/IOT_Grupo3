package com.example.telehotel.features.cliente.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;

import java.util.List;

public class LugaresAdapter extends RecyclerView.Adapter<LugaresAdapter.ViewHolder> {
    private static final String TAG = "LugaresAdapter";

    private List<LugarItem> items;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(LugarItem lugar);
    }

    public static class LugarItem {
        public int imageResId;
        public String titulo;
        public String descripcion;
        public String pais;

        public LugarItem(int imageResId, String titulo) {
            this.imageResId = imageResId;
            this.titulo = titulo;
            Log.d(TAG, "LugarItem creado: " + titulo);
        }

        public LugarItem(int imageResId, String titulo, String descripcion) {
            this.imageResId = imageResId;
            this.titulo = titulo;
            this.descripcion = descripcion;
            Log.d(TAG, "LugarItem creado con descripción: " + titulo);
        }

        public LugarItem(int imageResId, String titulo, String descripcion, String pais) {
            this.imageResId = imageResId;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.pais = pais;
            Log.d(TAG, "LugarItem creado completo: " + titulo + " - " + pais);
        }

        public String getNombre() {
            return titulo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getPais() {
            return pais;
        }

        public int getImageResId() {
            return imageResId;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView titulo;

        public ViewHolder(@NonNull View view) {
            super(view);
            img = view.findViewById(R.id.imgItem);
            titulo = view.findViewById(R.id.txtItem);

            Log.d(TAG, "ViewHolder creado - img: " + (img != null) + ", titulo: " + (titulo != null));
        }

        public void bind(LugarItem item, OnItemClickListener clickListener) {
            Log.d(TAG, "Binding item: " + item.titulo);

            try {
                // Establecer imagen
                if (img != null) {
                    img.setImageResource(item.imageResId);
                    Log.d(TAG, "Imagen establecida para: " + item.titulo);
                } else {
                    Log.e(TAG, "ImageView es null para: " + item.titulo);
                }

                // Establecer título
                if (titulo != null) {
                    titulo.setText(item.titulo);
                    Log.d(TAG, "Título establecido: " + item.titulo);
                } else {
                    Log.e(TAG, "TextView es null para: " + item.titulo);
                }

                // Configurar click listener
                itemView.setOnClickListener(v -> {
                    Log.d(TAG, "Click en item: " + item.titulo);
                    if (clickListener != null) {
                        clickListener.onItemClick(item);
                    } else {
                        Log.w(TAG, "ClickListener es null");
                    }
                });

                Log.d(TAG, "Item bindead exitosamente: " + item.titulo);

            } catch (Exception e) {
                Log.e(TAG, "Error binding item: " + item.titulo, e);
            }
        }
    }

    public LugaresAdapter(List<LugarItem> items) {
        this.items = items;
        Log.d(TAG, "LugaresAdapter creado con " + (items != null ? items.size() : 0) + " items");

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                LugarItem item = items.get(i);
                Log.d(TAG, "Item " + i + ": " + item.titulo + " (drawable: " + item.imageResId + ")");
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
        Log.d(TAG, "OnItemClickListener establecido: " + (listener != null));
    }

    @NonNull
    @Override
    public LugaresAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder llamado");

        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_lugares, parent, false);
            Log.d(TAG, "Layout inflado correctamente");

            ViewHolder holder = new ViewHolder(view);
            Log.d(TAG, "ViewHolder creado correctamente");

            return holder;
        } catch (Exception e) {
            Log.e(TAG, "Error creando ViewHolder", e);
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder llamado para posición: " + position);

        try {
            if (items != null && position < items.size()) {
                LugarItem item = items.get(position);
                Log.d(TAG, "Binding item en posición " + position + ": " + item.titulo);
                holder.bind(item, onItemClickListener);
            } else {
                Log.e(TAG, "Item no válido en posición: " + position + ", total items: " + (items != null ? items.size() : "null"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en onBindViewHolder para posición: " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        int count = items != null ? items.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    // Métodos utilitarios con debug
    public void updateItems(List<LugarItem> nuevosItems) {
        Log.d(TAG, "updateItems llamado - items anteriores: " + (items != null ? items.size() : 0) +
                ", nuevos items: " + (nuevosItems != null ? nuevosItems.size() : 0));

        this.items = nuevosItems;
        notifyDataSetChanged();

        Log.d(TAG, "Items actualizados y adapter notificado");
    }

    public void addItem(LugarItem item) {
        if (items != null && item != null) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
            Log.d(TAG, "Item agregado: " + item.titulo + ", total items: " + items.size());
        } else {
            Log.e(TAG, "No se puede agregar item - items: " + (items != null) + ", item: " + (item != null));
        }
    }

    public LugarItem getItem(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            return items.get(position);
        }
        Log.w(TAG, "getItem posición inválida: " + position);
        return null;
    }
}