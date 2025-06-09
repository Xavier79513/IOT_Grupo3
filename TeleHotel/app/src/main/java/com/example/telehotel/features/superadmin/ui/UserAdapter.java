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
import com.example.telehotel.data.model.Usuario;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<Usuario> usuarioList;
    private OnUserActionListener listener;

    public UserAdapter(List<Usuario> usuarioList, OnUserActionListener listener) {
        this.usuarioList = usuarioList;
        this.listener = listener;
    }

    public interface OnUserActionListener {
        void onToggleEstadoClick(Usuario usuario);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);
        holder.bind(usuario, listener);
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtCorreo, txtEstado;
        Button btnToggleEstado;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtCorreo = itemView.findViewById(R.id.txtCorreo);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            btnToggleEstado = itemView.findViewById(R.id.btnToggleEstado);
        }

        public void bind(Usuario usuario, OnUserActionListener listener) {
            txtNombre.setText(usuario.getName());
            txtCorreo.setText(usuario.getEmail());
            txtEstado.setText("Estado: " + usuario.getEstado());

            boolean esActivo = "Activo".equalsIgnoreCase(usuario.getEstado());

            btnToggleEstado.setText(esActivo ? "Desactivar" : "Activar");
            btnToggleEstado.setBackgroundColor(esActivo ? Color.RED : Color.GREEN);

            btnToggleEstado.setOnClickListener(v -> listener.onToggleEstadoClick(usuario));
        }
    }
}