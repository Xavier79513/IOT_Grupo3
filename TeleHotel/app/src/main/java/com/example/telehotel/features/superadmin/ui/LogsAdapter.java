package com.example.telehotel.features.superadmin.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.LogItem;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private List<LogItem> logList;

    public LogsAdapter(List<LogItem> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogItem log = logList.get(position);
        holder.textDate.setText(log.fecha);
        holder.textDescription.setText(log.descripcion);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textDescription;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textDescription = itemView.findViewById(R.id.textDescription);
        }
    }
}