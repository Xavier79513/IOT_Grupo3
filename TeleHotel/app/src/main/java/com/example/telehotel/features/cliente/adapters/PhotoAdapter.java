package com.example.telehotel.features.cliente.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.telehotel.R;
import com.example.telehotel.features.cliente.FullscreenPhotoActivity;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private static final String TAG = "PhotoAdapter";
    private Context context;
    private List<String> imageUrls;
    private OnImageClickListener clickListener;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl, int position);
    }

    public PhotoAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public PhotoAdapter(Context context, List<String> imageUrls, OnImageClickListener clickListener) {
        this.context = context;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_item_foto, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        if (position >= imageUrls.size()) {
            Log.e(TAG, "Posición inválida: " + position + ", tamaño lista: " + imageUrls.size());
            return;
        }

        String imageUrl = imageUrls.get(position);

        // CRÍTICO: Solo cargar imagen si imageView no es null
        if (holder.imageView != null) {
            // Configurar Glide con opciones de rendimiento
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ic_hotel)
                    .error(R.drawable.ic_hotel)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(context)
                    .load(imageUrl)
                    .apply(options)
                    .into(holder.imageView);

            Log.d(TAG, "Imagen cargada en posición: " + position + " URL: " + imageUrl);
        } else {
            Log.e(TAG, "ImageView es null en posición: " + position + ", no se puede cargar imagen");
        }

        // Configurar click para abrir pantalla completa
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Click en imagen posición: " + position);

            // Llamar al listener personalizado si existe
            if (clickListener != null) {
                clickListener.onImageClick(imageUrl, position);
            }

            // Abrir pantalla completa
            abrirPantallaCompleta(position);
        });
    }

    /**
     * Abre la actividad de pantalla completa con todas las imágenes
     */
    private void abrirPantallaCompleta(int position) {
        try {
            Intent intent = new Intent(context, FullscreenPhotoActivity.class);

            // Pasar todas las URLs de imágenes
            ArrayList<String> imageUrlsList = new ArrayList<>(imageUrls);
            intent.putStringArrayListExtra("imageUrls", imageUrlsList);

            // Pasar la posición de la imagen seleccionada
            intent.putExtra("position", position);

            Log.d(TAG, "Abriendo pantalla completa - Posición: " + position +
                    ", Total imágenes: " + imageUrls.size());

            context.startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error abriendo pantalla completa", e);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    /**
     * Método para actualizar las imágenes
     */
    public void updateImages(List<String> newImageUrls) {
        if (newImageUrls != null) {
            this.imageUrls = new ArrayList<>(newImageUrls);
            notifyDataSetChanged();
            Log.d(TAG, "Imágenes actualizadas: " + newImageUrls.size());
        } else {
            this.imageUrls = new ArrayList<>();
            notifyDataSetChanged();
            Log.d(TAG, "Lista de imágenes limpiada");
        }
    }

    /**
     * Método para agregar imágenes
     */
    public void addImages(List<String> newImageUrls) {
        if (newImageUrls != null && !newImageUrls.isEmpty()) {
            int startPosition = imageUrls.size();
            imageUrls.addAll(newImageUrls);
            notifyItemRangeInserted(startPosition, newImageUrls.size());
            Log.d(TAG, "Imágenes agregadas: " + newImageUrls.size());
        }
    }

    /**
     * Método para limpiar las imágenes
     */
    public void clearImages() {
        int size = imageUrls.size();
        imageUrls.clear();
        notifyItemRangeRemoved(0, size);
        Log.d(TAG, "Todas las imágenes limpiadas: " + size);
    }

    /**
     * Obtiene la lista actual de URLs de imágenes
     */
    public List<String> getImageUrls() {
        return new ArrayList<>(imageUrls);
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            // CRÍTICO: Buscar ImageView y verificar si existe
            imageView = itemView.findViewById(R.id.imageView);

            if (imageView == null) {
                Log.e(TAG, "ERROR: ImageView no encontrado en el layout item_photo_gallery");
                Log.e(TAG, "Verifica que el layout tenga un ImageView con id='imageView'");

                // DEBUG: Listar todos los IDs disponibles en el layout
                debugLayoutIds(itemView);
            } else {
                // Solo configurar si imageView no es null
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Log.d(TAG, "ImageView encontrado y configurado correctamente");
            }
        }

        /**
         * Método de debugging para ver qué IDs están disponibles en el layout
         */
        private void debugLayoutIds(View view) {
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                Log.d(TAG, "=== DEBUGGING LAYOUT item_photo_gallery ===");
                Log.d(TAG, "ViewGroup type: " + group.getClass().getSimpleName());
                Log.d(TAG, "Child count: " + group.getChildCount());

                for (int i = 0; i < group.getChildCount(); i++) {
                    View child = group.getChildAt(i);
                    String resourceName = "unknown";
                    try {
                        if (child.getId() != View.NO_ID) {
                            resourceName = view.getContext().getResources().getResourceEntryName(child.getId());
                        }
                    } catch (Exception e) {
                        resourceName = "no_name";
                    }

                    Log.d(TAG, "Child " + i + ": " + child.getClass().getSimpleName() +
                            " (id: " + resourceName + ")");

                    // Si es ImageView, mostrar información adicional
                    if (child instanceof ImageView) {
                        Log.d(TAG, "  -> ¡Found ImageView! ID name: " + resourceName);
                    }

                    // Buscar recursivamente en subgrupos
                    if (child instanceof ViewGroup) {
                        debugLayoutIds(child);
                    }
                }
                Log.d(TAG, "=== END DEBUGGING ===");
            }
        }
    }
}