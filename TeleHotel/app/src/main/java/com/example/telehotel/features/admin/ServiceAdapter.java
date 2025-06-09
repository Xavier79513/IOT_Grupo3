package com.example.telehotel.features.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import java.util.List;

public class ServiceAdapter
        extends RecyclerView.Adapter<ServiceAdapter.VH> {

    private final List<Service> services;
    public ServiceAdapter(List<Service> services) {
        this.services = services;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Service s = services.get(position);
        holder.icon.setImageResource(s.iconRes);
        holder.name.setText(s.name);
        holder.desc.setText(s.desc);
        holder.price.setText(s.price);

    }

    @Override public int getItemCount() {
        return services.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView name, desc, price;

        VH(View itemView) {
            super(itemView);
            icon       = itemView.findViewById(R.id.imgServiceIcon);
            name       = itemView.findViewById(R.id.tvServiceName);
            desc       = itemView.findViewById(R.id.tvServiceDesc);
            price      = itemView.findViewById(R.id.tvServicePrice);

        }
    }
}
