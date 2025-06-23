package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.telehotel.R;

import java.util.List;

public class FullscreenPhotoAdapter extends RecyclerView.Adapter<FullscreenPhotoAdapter.FullscreenPhotoViewHolder> {

    private static final String TAG = "FullscreenPhotoAdapter";

    private Context context;
    private List<String> imageUrls;

    public FullscreenPhotoAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
        Log.d(TAG, "Adapter creado con " + (imageUrls != null ? imageUrls.size() : 0) + " imágenes");
    }

    @NonNull
    @Override
    public FullscreenPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_fullscreen_photo, parent, false);
        return new FullscreenPhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FullscreenPhotoViewHolder holder, int position) {
        if (position >= imageUrls.size()) {
            Log.e(TAG, "Posición inválida: " + position);
            return;
        }

        String imageUrl = imageUrls.get(position);
        Log.d(TAG, "Cargando imagen en posición " + position + ": " + imageUrl);

        // Mostrar ProgressBar mientras carga
        if (holder.progressBar != null) {
            holder.progressBar.setVisibility(View.VISIBLE);
        }

        // Configurar opciones de Glide para pantalla completa
        RequestOptions options = new RequestOptions()
                .fitCenter()  // Para pantalla completa usamos fitCenter en lugar de centerCrop
                .placeholder(R.drawable.ic_hotel)
                .error(R.drawable.ic_hotel)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        // Cargar imagen con Glide
        Glide.with(context)
                .load(imageUrl)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade(300))  // Transición suave
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e,
                                                Object model, Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        Log.e(TAG, "Error cargando imagen en posición " + position, e);
                        if (holder.progressBar != null) {
                            holder.progressBar.setVisibility(View.GONE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                   Object model, Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d(TAG, "Imagen cargada exitosamente en posición " + position);
                        if (holder.progressBar != null) {
                            holder.progressBar.setVisibility(View.GONE);
                        }
                        return false;
                    }
                })
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    /**
     * ViewHolder para la imagen en pantalla completa
     */
    static class FullscreenPhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;

        FullscreenPhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.fullscreenImageView);
            progressBar = itemView.findViewById(R.id.progressBar);

            if (imageView == null) {
                Log.e(TAG, "ImageView no encontrado en item_fullscreen_photo");
            }

            if (progressBar == null) {
                Log.w(TAG, "ProgressBar no encontrado en item_fullscreen_photo");
            }

            Log.d(TAG, "ViewHolder creado - ImageView: " + (imageView != null ? "OK" : "NULL") +
                    ", ProgressBar: " + (progressBar != null ? "OK" : "NULL"));
        }
    }
}