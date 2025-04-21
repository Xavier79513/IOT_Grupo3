package com.example.telehotel.features.cliente.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.example.telehotel.R;
import com.example.telehotel.features.cliente.FullscreenPhotoActivity;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private List<Integer> photoList;
    private Context context;

    public PhotoAdapter(Context context, List<Integer> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
        }
    }

    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cliente_item_foto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.imageView.setImageResource(photoList.get(position));

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullscreenPhotoActivity.class);
            intent.putExtra("photos", new ArrayList<>(photoList));
            intent.putExtra("position", position);
            intent.putExtra("photoResId", photoList.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }
}