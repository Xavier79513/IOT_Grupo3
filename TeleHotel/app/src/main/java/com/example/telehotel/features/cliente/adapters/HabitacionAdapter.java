/*package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Habitacion;

import java.util.List;

public class HabitacionAdapter extends RecyclerView.Adapter<HabitacionAdapter.ViewHolder> {
    private static final String TAG = "HabitacionAdapter";

    public interface OnHabitacionClickListener {
        void onHabitacionClick(Habitacion habitacion);
    }

    private final List<Habitacion> lista;
    private final Context context;
    private final OnHabitacionClickListener listener;

    public HabitacionAdapter(Context context, List<Habitacion> lista, OnHabitacionClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;

        Log.d(TAG, "=== HABITACIONES RECIBIDAS EN ADAPTER ===");
        Log.d(TAG, "Total habitaciones: " + lista.size());
        for (int i = 0; i < lista.size(); i++) {
            Habitacion hab = lista.get(i);
            int adultos = hab.getCapacidadAdultos() != null ? hab.getCapacidadAdultos() : 0;
            int ninos = hab.getCapacidadNinos() != null ? hab.getCapacidadNinos() : 0;
            Log.d(TAG, String.format("Habitación %d: Número=%s, Capacidad=[%d adultos, %d niños], Estado=%s",
                    i, hab.getNumero(), adultos, ninos, hab.getEstado()));
        }
        Log.d(TAG, "==========================================");
    }

    @NonNull
    @Override
    public HabitacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_habitacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitacionAdapter.ViewHolder holder, int position) {
        Habitacion hab = lista.get(position);

        int adultos = hab.getCapacidadAdultos() != null ? hab.getCapacidadAdultos() : 0;
        int ninos = hab.getCapacidadNinos() != null ? hab.getCapacidadNinos() : 0;
        Log.d(TAG, String.format("Mostrando posición %d: Habitación %s [%d adultos, %d niños]",
                position, hab.getNumero(), adultos, ninos));

        holder.tvRoomTitle.setText(hab.getDescripcion() != null ? hab.getDescripcion() : "Habitación " + hab.getNumero());

        StringBuilder detalles = new StringBuilder();
        detalles.append("Tipo: ").append(hab.getTipo()).append(" - Número: ").append(hab.getNumero());

        if (hab.getCapacidadAdultos() != null || hab.getCapacidadNinos() != null) {
            detalles.append("\nCapacidad: ").append(adultos).append(" adultos, ").append(ninos).append(" niños");
        }

        if (hab.getTamaño() != null && hab.getTamaño() > 0) {
            detalles.append("\nTamaño: ").append(String.format("%.0f m²", hab.getTamaño()));
        }

        holder.tvRoomDetails.setText(detalles.toString());
        holder.tvPolicy.setText(hab.getEstado().equalsIgnoreCase("disponible") ? "Disponible" : "No disponible");
        holder.tvPrice.setText(String.format("S/ %.2f", hab.getPrecio()));
        holder.btnSelect.setEnabled(hab.getEstado().equalsIgnoreCase("disponible"));
        holder.btnSelect.setOnClickListener(v -> listener.onHabitacionClick(hab));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomTitle, tvRoomDetails, tvPolicy, tvPrice;
        Button btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPolicy=itemView.findViewById(R.id.tvPolicy);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }
}*/
package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Habitacion;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HabitacionAdapter extends RecyclerView.Adapter<HabitacionAdapter.ViewHolder> {
    private static final String TAG = "HabitacionAdapter";

    public interface OnHabitacionClickListener {
        void onHabitacionClick(Habitacion habitacion);
    }

    private final List<Habitacion> lista;
    private final Context context;
    private final OnHabitacionClickListener listener;

    public HabitacionAdapter(Context context, List<Habitacion> lista, OnHabitacionClickListener listener) {
        this.context = context;
        this.listener = listener;

        Log.d(TAG, "=== CONSTRUCTOR HABITACION ADAPTER ===");
        Log.d(TAG, "📦 Lista recibida es null: " + (lista == null));

        if (lista != null) {
            Log.d(TAG, "📊 Total habitaciones recibidas: " + lista.size());

            // Mostrar habitaciones recibidas
            for (int i = 0; i < lista.size(); i++) {
                Habitacion hab = lista.get(i);
                if (hab != null) {
                    Log.d(TAG, "  " + i + ". Habitación " + hab.getNumero() + " -> Estado: '" + hab.getEstado() + "'");
                } else {
                    Log.d(TAG, "  " + i + ". Habitación NULL");
                }
            }
        }

        // ✅ NO FILTRAR - usar directamente la lista recibida (ya viene filtrada del Repository)
        this.lista = lista != null ? new ArrayList<>(lista) : new ArrayList<>();

        Log.d(TAG, "🎯 RESULTADO FINAL:");
        Log.d(TAG, "   Habitaciones en adapter: " + this.lista.size());
        Log.d(TAG, "======================================");
    }

    @NonNull
    @Override
    public HabitacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "🏗️ onCreateViewHolder() llamado");

        try {
            View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_habitacion, parent, false);

            if (view == null) {
                Log.e(TAG, "❌ Error: view es null después de inflate");
                throw new RuntimeException("No se pudo inflar el layout cliente_item_habitacion");
            }

            Log.d(TAG, "✅ ViewHolder creado correctamente");
            return new ViewHolder(view);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error en onCreateViewHolder: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HabitacionAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "🎨 onBindViewHolder() - posición: " + position);

        try {
            Habitacion hab = lista.get(position);

            if (hab == null) {
                Log.e(TAG, "❌ Habitación en posición " + position + " es null");
                return;
            }

            int adultos = hab.getCapacidadAdultos() != null ? hab.getCapacidadAdultos() : 0;
            int ninos = hab.getCapacidadNinos() != null ? hab.getCapacidadNinos() : 0;

            Log.d(TAG, String.format("✅ Vinculando habitación %s [%d adultos, %d niños]",
                    hab.getNumero(), adultos, ninos));

            // ✅ TÍTULO DE LA HABITACIÓN
            if (holder.tvRoomTitle != null) {
                String titulo = hab.getDescripcion() != null && !hab.getDescripcion().trim().isEmpty()
                        ? hab.getDescripcion()
                        : "Habitación " + hab.getNumero();
                holder.tvRoomTitle.setText(titulo);
            } else {
                Log.w(TAG, "❌ tvRoomTitle es null");
            }

            // ✅ DETALLES DE LA HABITACIÓN
            if (holder.tvRoomDetails != null) {
                StringBuilder detalles = new StringBuilder();

                // Tipo y número
                if (hab.getTipo() != null) {
                    detalles.append("Tipo: ").append(hab.getTipo());
                }
                if (hab.getNumero() != null) {
                    if (detalles.length() > 0) detalles.append(" - ");
                    detalles.append("Número: ").append(hab.getNumero());
                }

                // Capacidad
                if (hab.getCapacidadAdultos() != null || hab.getCapacidadNinos() != null) {
                    detalles.append("\nCapacidad: ").append(adultos).append(" adultos, ").append(ninos).append(" niños");
                }

                // Tamaño
                if (hab.getTamaño() != null && hab.getTamaño() > 0) {
                    detalles.append("\nTamaño: ").append(String.format("%.0f m²", hab.getTamaño()));
                }

                holder.tvRoomDetails.setText(detalles.toString());
            } else {
                Log.w(TAG, "❌ tvRoomDetails es null");
            }

            // ✅ ESTADO - SIEMPRE DISPONIBLE (porque ya vienen filtradas)
            if (holder.tvPolicy != null) {
                holder.tvPolicy.setText("Disponible");
            } else {
                Log.w(TAG, "⚠️ tvPolicy es null - verifica que existe en el XML");
            }

            // ✅ PRECIO
            if (holder.tvPrice != null && hab.getPrecio() != null) {
                holder.tvPrice.setText(String.format("S/ %.2f", hab.getPrecio()));
            } else {
                Log.w(TAG, "❌ tvPrice es null o precio es null");
                if (holder.tvPrice != null) {
                    holder.tvPrice.setText("Precio no disponible");
                }
            }

            // ✅ BOTÓN SELECCIONAR
            if (holder.btnSelect != null) {
                holder.btnSelect.setEnabled(true);
                holder.btnSelect.setOnClickListener(v -> {
                    if (listener != null) {
                        Log.d(TAG, "✅ Habitación seleccionada: " + hab.getNumero());
                        listener.onHabitacionClick(hab);
                    }
                });
            } else {
                Log.w(TAG, "❌ btnSelect es null");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error en onBindViewHolder posición " + position + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        int count = lista != null ? lista.size() : 0;
        Log.d(TAG, "🔢 getItemCount() llamado - devolviendo: " + count);

        if (count == 0) {
            Log.w(TAG, "⚠️ getItemCount() = 0 - RecyclerView estará vacío");
            Log.w(TAG, "   Lista es null: " + (lista == null));
            if (lista != null) {
                Log.w(TAG, "   Lista size: " + lista.size());
            }
        }

        return count;
    }

    // ✅ MÉTODO PARA ACTUALIZAR HABITACIONES (sin filtro adicional)
    /*public void actualizarHabitaciones(List<Habitacion> nuevasHabitaciones) {
        Log.d(TAG, "=== ACTUALIZANDO HABITACIONES ===");

        if (nuevasHabitaciones == null) {
            Log.w(TAG, "⚠️ Lista de nuevas habitaciones es null");
            return;
        }

        // No filtrar - las habitaciones ya vienen filtradas del Repository
        this.lista.clear();
        this.lista.addAll(nuevasHabitaciones);

        Log.d(TAG, "Habitaciones actualizadas: " + lista.size());

        // Notificar cambios
        notifyDataSetChanged();
    }*/
    public void actualizarHabitaciones(List<Habitacion> nuevasHabitaciones) {
        Log.d(TAG, "=== ACTUALIZANDO HABITACIONES EN ADAPTER ===");
        Log.d(TAG, "Habitaciones actuales en adapter: " + (lista != null ? lista.size() : "lista null"));
        Log.d(TAG, "Nuevas habitaciones recibidas: " + (nuevasHabitaciones != null ? nuevasHabitaciones.size() : "null"));

        if (nuevasHabitaciones == null) {
            Log.w(TAG, "⚠️ Lista de nuevas habitaciones es null");
            return;
        }

        // Limpiar y agregar las nuevas habitaciones
        this.lista.clear();
        this.lista.addAll(nuevasHabitaciones);

        Log.d(TAG, "Lista en adapter actualizada: " + lista.size());
        Log.d(TAG, "getItemCount() después de actualizar: " + getItemCount());

        // Notificar cambios
        notifyDataSetChanged();

        Log.d(TAG, "notifyDataSetChanged() llamado");
        Log.d(TAG, "========================================");
    }

    // ✅ VIEWHOLDER CON VALIDACIONES
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomTitle, tvRoomDetails, tvPolicy, tvPrice;
        Button btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                // ✅ ENCONTRAR TODAS LAS VISTAS
                tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
                tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
                tvPolicy = itemView.findViewById(R.id.tvPolicy);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                btnSelect = itemView.findViewById(R.id.btnSelect);

                // ✅ VALIDAR QUE SE ENCONTRARON LAS VISTAS CRÍTICAS
                if (tvRoomTitle == null) Log.e("HabitacionAdapter", "❌ tvRoomTitle no encontrado en XML");
                if (tvRoomDetails == null) Log.e("HabitacionAdapter", "❌ tvRoomDetails no encontrado en XML");
                if (tvPolicy == null) Log.e("HabitacionAdapter", "❌ tvPolicy no encontrado en XML");
                if (tvPrice == null) Log.e("HabitacionAdapter", "❌ tvPrice no encontrado en XML");
                if (btnSelect == null) Log.e("HabitacionAdapter", "❌ btnSelect no encontrado en XML");

            } catch (Exception e) {
                Log.e("HabitacionAdapter", "❌ Error inicializando ViewHolder: " + e.getMessage(), e);
            }
        }
    }
}