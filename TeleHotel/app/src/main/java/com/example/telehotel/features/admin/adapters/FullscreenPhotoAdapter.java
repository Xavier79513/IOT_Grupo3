package com.example.telehotel.features.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.telehotel.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class FullscreenPhotoAdapter extends RecyclerView.Adapter<FullscreenPhotoAdapter.PhotoViewHolder> {

    private Context context;
    private List<String> imageUrls;

    public FullscreenPhotoAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fullscreen_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        holder.progressBar.setVisibility(View.VISIBLE);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.photoView);

        // Ocultar progress bar cuando la imagen se carga
        holder.photoView.setOnPhotoTapListener((view, x, y) -> {
            // Aquí puedes agregar lógica adicional si necesitas
        });

        holder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;
        ProgressBar progressBar;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}