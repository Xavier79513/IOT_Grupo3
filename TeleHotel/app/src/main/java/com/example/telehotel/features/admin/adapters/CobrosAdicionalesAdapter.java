package com.example.telehotel.features.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.CobroAdicional;

import java.util.List;
import java.util.Locale;

public class CobrosAdicionalesAdapter extends RecyclerView.Adapter<CobrosAdicionalesAdapter.CobroViewHolder> {

    private List<CobroAdicional> cobros;
    private Context context;
    private OnCobroClickListener listener;

    public interface OnCobroClickListener {
        void onEliminarCargo(int position);
    }

    public CobrosAdicionalesAdapter(List<CobroAdicional> cobros, Context context, OnCobroClickListener listener) {
        this.cobros = cobros;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CobroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cargo_adicional, parent, false);
        return new CobroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CobroViewHolder holder, int position) {
        CobroAdicional cargo = cobros.get(position);

        // Concepto del cargo
        holder.tvConcepto.setText(cargo.concepto != null ? cargo.concepto : "Sin concepto");

        // Monto del cargo
        double monto = cargo.monto != null ? cargo.monto : 0.0;
        holder.tvMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", monto));

        // Botón eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarCargo(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cobros.size();
    }

    public static class CobroViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcepto, tvMonto;
        ImageButton btnEliminar;

        public CobroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConcepto = itemView.findViewById(R.id.tvConcepto);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}