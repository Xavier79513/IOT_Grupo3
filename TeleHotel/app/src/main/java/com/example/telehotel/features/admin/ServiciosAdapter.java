package com.example.telehotel.features.admin;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.telehotel.R;
import com.example.telehotel.data.model.Servicio;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telehotel.R;
import com.example.telehotel.data.model.Servicio;

import java.util.List;

public class ServiciosAdapter extends RecyclerView.Adapter<ServiciosAdapter.ServicioViewHolder> {

    private List<Servicio> servicios;

    public ServiciosAdapter(List<Servicio> servicios) {
        this.servicios = servicios;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_servicio, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        Servicio servicio = servicios.get(position);
        holder.bind(servicio);
    }

    @Override
    public int getItemCount() {
        return servicios.size();
    }

    public static class ServicioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre, tvDescripcion, tvPrecio, tvEstado, tvImagenes;
        private ImageView ivImagenServicio, btnOpciones; // agregar btnOpciones

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvImagenes = itemView.findViewById(R.id.tvImagenes);

            ivImagenServicio = itemView.findViewById(R.id.ivImagenServicio);
            btnOpciones = itemView.findViewById(R.id.btnOpciones);

        }

        public void bind(Servicio servicio) {
            tvNombre.setText(servicio.getNombre());
            tvDescripcion.setText(servicio.getDescripcion());
            tvPrecio.setText(servicio.getPrecioFormateado());
            tvEstado.setText(servicio.getEstadoTexto());

            // Color del precio según si es gratuito o no
            if (servicio.esGratuito()) {
                tvPrecio.setTextColor(Color.parseColor("#4CAF50")); // Verde
            } else {
                tvPrecio.setTextColor(Color.parseColor("#FF9800")); // Naranja
            }

            // Color del estado
            if (servicio.isDisponible()) {
                tvEstado.setTextColor(Color.parseColor("#4CAF50")); // Verde
            } else {
                tvEstado.setTextColor(Color.parseColor("#F44336")); // Rojo
            }

            // Mostrar imagen si existe
            if (servicio.tieneImagenes() && servicio.getImagenes().size() > 0) {
                String url = servicio.getImagenes().get(0); // Usamos la primera
                ivImagenServicio.setVisibility(View.VISIBLE);

                Glide.with(itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_hhotel)
                        .error(R.drawable.ic_hhotel)
                        .centerCrop()
                        .into(ivImagenServicio);

                tvImagenes.setText(servicio.getCantidadImagenes() + " imagen(es)");
                tvImagenes.setTextColor(Color.parseColor("#2196F3"));
            } else {
                ivImagenServicio.setVisibility(View.GONE);
                tvImagenes.setText("Sin imágenes");
                tvImagenes.setTextColor(Color.parseColor("#666666"));
            }

            // Menú de opciones
            btnOpciones.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(itemView.getContext(), btnOpciones);
                popup.inflate(R.menu.menu_servicio_item); // Asegúrate de tener este menú
                popup.setOnMenuItemClickListener(menuItem -> {
                    int id = menuItem.getItemId();
                    if (id == R.id.action_ver_imagen) {
                        if (servicio.tieneImagenes()) {
                            String imageUrl = servicio.getImagenes().get(0);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                            itemView.getContext().startActivity(intent);
                        } else {
                            Toast.makeText(itemView.getContext(), "No hay imagen para mostrar", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else if (id == R.id.action_eliminar) {
                        Toast.makeText(itemView.getContext(), "Eliminar: " + servicio.getNombre(), Toast.LENGTH_SHORT).show();
                        // Aquí puedes emitir un callback al fragmento
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

    }
}