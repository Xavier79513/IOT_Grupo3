package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.text.TextUtils;
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
        holder.tvRoomTitle.setText(hab.getDescripcion());
        holder.tvRoomDetails.setText("Tipo: " + hab.getTipo() + " - Número: " + hab.getNumero());
        holder.tvBenefits.setText(TextUtils.join(" · ", hab.getServiciosIncluidos()));
        holder.tvPolicy.setText(hab.getEstado().equalsIgnoreCase("disponible") ? "Disponible" : "No disponible");
        holder.tvDiscount.setVisibility(View.GONE);
        holder.tvPrice.setText(String.format("S/ %.2f", hab.getPrecio()));
        holder.tvTaxes.setVisibility(View.GONE);
        holder.tvWarning.setVisibility(View.GONE);

        holder.btnSelect.setEnabled(hab.getEstado().equalsIgnoreCase("disponible"));
        holder.btnSelect.setOnClickListener(v -> listener.onHabitacionClick(hab));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomTitle, tvRoomDetails, tvBenefits, tvPolicy, tvDiscount, tvPrice, tvTaxes, tvWarning;
        Button btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
            tvBenefits = itemView.findViewById(R.id.tvBenefits);
            tvPolicy = itemView.findViewById(R.id.tvPolicy);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTaxes = itemView.findViewById(R.id.tvTaxes);
            tvWarning = itemView.findViewById(R.id.tvWarning);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }
}
