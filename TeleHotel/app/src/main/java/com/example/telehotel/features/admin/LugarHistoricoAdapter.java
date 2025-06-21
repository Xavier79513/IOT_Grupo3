package com.example.telehotel.features.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.LugarHistorico;

import java.util.List;

/*public class LugarHistoricoAdapter extends RecyclerView.Adapter<LugarHistoricoAdapter.ViewHolder> {

    private List<LugarHistorico> lugares;
    private OnLugarClickListener listener;

    public interface OnLugarClickListener {
        void onLugarClick(LugarHistorico lugar);
    }

    public LugarHistoricoAdapter(List<LugarHistorico> lugares, OnLugarClickListener listener) {
        this.lugares = lugares;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lugar_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LugarHistorico lugar = lugares.get(position);
        holder.bind(lugar);
    }

    @Override
    public int getItemCount() {
        return lugares.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbSeleccionar;
        private TextView tvNombre, tvDescripcion, tvDistancia, tvTipo;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSeleccionar = itemView.findViewById(R.id.cbSeleccionar);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvDistancia = itemView.findViewById(R.id.tvDistancia);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(LugarHistorico lugar) {
            tvNombre.setText(lugar.getNombre());
            tvDescripcion.setText(lugar.getDescripcion());
            tvDistancia.setText(String.format("%.1f km", lugar.getDistancia()));
            tvTipo.setText(lugar.getTipoLugar());

            cbSeleccionar.setChecked(lugar.isSeleccionado());

            // Cambiar apariencia según selección
            if (lugar.isSeleccionado()) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
                cardView.setAlpha(0.1f);
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                cardView.setAlpha(1.0f);
            }

            // Listener para el checkbox
            cbSeleccionar.setOnCheckedChangeListener(null); // Limpiar listener anterior
            cbSeleccionar.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onLugarClick(lugar);
                }
            });

            // Listener para el click en el item completo
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLugarClick(lugar);
                }
            });
        }
    }
}*/
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LugarHistoricoAdapter extends RecyclerView.Adapter<LugarHistoricoAdapter.ViewHolder> {

    private List<LugarHistorico> lugares;
    private OnLugarClickListener listener;

    public interface OnLugarClickListener {
        void onLugarClick(LugarHistorico lugar);
    }

    public LugarHistoricoAdapter(List<LugarHistorico> lugares, OnLugarClickListener listener) {
        this.lugares = lugares;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lugar_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LugarHistorico lugar = lugares.get(position);
        holder.bind(lugar);
    }

    @Override
    public int getItemCount() {
        return lugares.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbSeleccionar;
        private TextView tvNombre, tvDescripcion, tvDistancia, tvTipo;
        private CardView cardView;
        private boolean isBinding = false; // Flag para evitar loops

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSeleccionar = itemView.findViewById(R.id.cbSeleccionar);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvDistancia = itemView.findViewById(R.id.tvDistancia);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(LugarHistorico lugar) {
            isBinding = true; // Marcar que estamos en proceso de binding

            tvNombre.setText(lugar.getNombre());
            tvDescripcion.setText(lugar.getDescripcion());
            tvDistancia.setText(String.format("%.1f km", lugar.getDistancia()));
            tvTipo.setText(lugar.getTipoLugar());

            // Cambiar apariencia según selección
            if (lugar.isSeleccionado()) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
                cardView.setAlpha(0.9f);
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                cardView.setAlpha(1.0f);
            }

            // Limpiar listener anterior para evitar llamadas no deseadas
            cbSeleccionar.setOnCheckedChangeListener(null);

            // Establecer el estado del checkbox
            cbSeleccionar.setChecked(lugar.isSeleccionado());

            // Configurar el listener después de establecer el estado
            cbSeleccionar.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isBinding && listener != null) {
                    // Usar Handler para evitar conflictos con el layout del RecyclerView
                    itemView.post(() -> {
                        if (listener != null) {
                            listener.onLugarClick(lugar);
                        }
                    });
                }
            });

            // Listener para el click en el item completo
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    // Usar Handler para evitar conflictos con el layout del RecyclerView
                    itemView.post(() -> {
                        if (listener != null) {
                            listener.onLugarClick(lugar);
                        }
                    });
                }
            });

            isBinding = false; // Finalizar el proceso de binding
        }
    }
}