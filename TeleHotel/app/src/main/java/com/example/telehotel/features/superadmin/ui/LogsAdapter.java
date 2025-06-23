package com.example.telehotel.features.superadmin.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.LogSistema;
import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private List<LogSistema> logsList;

    public LogsAdapter(List<LogSistema> logsList) {
        this.logsList = logsList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogSistema log = logsList.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return logsList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private TextView textAction;
        private TextView textDescription;
        private TextView textUser;
        private TextView textDate;
        private TextView textTime;
        private View actionIndicator;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            textAction = itemView.findViewById(R.id.textAction);
            textDescription = itemView.findViewById(R.id.textDescription);
            textUser = itemView.findViewById(R.id.textUser);
            textDate = itemView.findViewById(R.id.textDate);
            textTime = itemView.findViewById(R.id.textTime);
            actionIndicator = itemView.findViewById(R.id.actionIndicator);
        }

        public void bind(LogSistema log) {
            textAction.setText(log.getActionDisplayName());
            textDescription.setText(log.getDescripcion());
            textUser.setText(log.getUserDisplayName());
            textDate.setText(log.getFormattedDate());
            textTime.setText(log.getFormattedTime());

            // Cambiar color del indicador según el tipo de acción
            int colorRes = getActionColor(log.getAccion());
            actionIndicator.setBackgroundColor(
                    itemView.getContext().getColor(colorRes)
            );
        }

        private int getActionColor(String action) {
            if (action == null) return android.R.color.darker_gray;

            switch (action) {
                case "CREATE":
                    return android.R.color.holo_green_dark;
                case "UPDATE":
                    return android.R.color.holo_blue_dark;
                case "DELETE":
                    return android.R.color.holo_red_dark;
                case "LOGIN":
                    return android.R.color.holo_purple;
                case "LOGOUT":
                    return android.R.color.holo_orange_dark;
                case "ERROR":
                    return android.R.color.holo_red_light;
                case "SYSTEM":
                default:
                    return android.R.color.darker_gray;
            }
        }
    }
}