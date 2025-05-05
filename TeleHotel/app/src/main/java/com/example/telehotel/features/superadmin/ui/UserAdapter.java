package com.example.telehotel.features.superadmin.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserActionListener listener;

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public interface OnUserActionListener {
        void onActivateClick(User user);
        void onDeactivateClick(User user);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtCorreo, txtEstado;
        Button btnActivar, btnDesactivar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtCorreo = itemView.findViewById(R.id.txtCorreo);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            btnActivar = itemView.findViewById(R.id.btnActivar);
            btnDesactivar = itemView.findViewById(R.id.btnDesactivar);
        }

        public void bind(User user, OnUserActionListener listener) {
            txtNombre.setText(user.getNombres() + " " + user.getApellidos());
            txtCorreo.setText(user.getCorreo());
            txtEstado.setText("Estado: " + user.getEstado());

            if ("Activo".equals(user.getEstado())) {
                btnActivar.setEnabled(false);
                btnActivar.setAlpha(0.5f);
                btnDesactivar.setEnabled(true);
                btnDesactivar.setAlpha(1f);
                btnDesactivar.setBackgroundColor(Color.RED);
            } else {
                btnActivar.setEnabled(true);
                btnActivar.setAlpha(1f);
                btnDesactivar.setEnabled(false);
                btnDesactivar.setAlpha(0.5f);
                btnActivar.setBackgroundColor(Color.GREEN);
            }

            btnActivar.setOnClickListener(v -> listener.onActivateClick(user));
            btnDesactivar.setOnClickListener(v -> listener.onDeactivateClick(user));
        }
    }
}