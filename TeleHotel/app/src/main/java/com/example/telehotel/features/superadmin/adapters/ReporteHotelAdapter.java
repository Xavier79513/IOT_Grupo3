package com.example.telehotel.features.superadmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telehotel.R;
import com.example.telehotel.data.model.ReporteHotel;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class ReporteHotelAdapter extends RecyclerView.Adapter<ReporteHotelAdapter.ReporteViewHolder> {

    private List<ReporteHotel> reportesList;
    private Context context;
    private OnReporteClickListener listener;

    // Interface para manejar clicks
    public interface OnReporteClickListener {
        void onVerDetalles(ReporteHotel reporte);
    }

    public ReporteHotelAdapter(Context context, List<ReporteHotel> reportesList) {
        this.context = context;
        this.reportesList = reportesList;
    }

    public void setOnReporteClickListener(OnReporteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reporte_hotel, parent, false);
        return new ReporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReporteViewHolder holder, int position) {
        ReporteHotel reporte = reportesList.get(position);
        holder.bind(reporte, position + 1); // +1 para ranking basado en 1
    }

    @Override
    public int getItemCount() {
        return reportesList != null ? reportesList.size() : 0;
    }

    public void updateList(List<ReporteHotel> newList) {
        this.reportesList = newList;
        notifyDataSetChanged();
    }

    public class ReporteViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRanking, tvNombreHotel, tvUbicacionHotel, tvEstadoRendimiento;
        private TextView tvTotalReservas, tvTotalIngresos, tvPromedioReserva;
        private TextView tvReservasActivas, tvReservasCanceladas;
        private MaterialButton btnVerDetalles;
        private CardView rankingCard, estadoCard;

        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListeners();
        }

        private void initViews() {
            tvRanking = itemView.findViewById(R.id.tvRanking);
            tvNombreHotel = itemView.findViewById(R.id.tvNombreHotel);
            tvUbicacionHotel = itemView.findViewById(R.id.tvUbicacionHotel);
            tvEstadoRendimiento = itemView.findViewById(R.id.tvEstadoRendimiento);
            tvTotalReservas = itemView.findViewById(R.id.tvTotalReservas);
            tvTotalIngresos = itemView.findViewById(R.id.tvTotalIngresos);
            tvPromedioReserva = itemView.findViewById(R.id.tvPromedioReserva);
            tvReservasActivas = itemView.findViewById(R.id.tvReservasActivas);
            tvReservasCanceladas = itemView.findViewById(R.id.tvReservasCanceladas);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            rankingCard = itemView.findViewById(R.id.rankingCard);
            estadoCard = itemView.findViewById(R.id.estadoCard);
        }

        private void setupClickListeners() {
            btnVerDetalles.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerDetalles(reportesList.get(getAdapterPosition()));
                }
            });

            // Click en todo el item también muestra detalles
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerDetalles(reportesList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(ReporteHotel reporte, int ranking) {
            // Ranking
            tvRanking.setText(String.valueOf(ranking));
            configurarColorRanking(ranking);

            // Información básica del hotel
            tvNombreHotel.setText(reporte.getNombreHotel());
            tvUbicacionHotel.setText("📍 " + reporte.getUbicacionHotel());

            // Estado de rendimiento
            String estadoRendimiento = reporte.getEstadoRendimiento();
            tvEstadoRendimiento.setText(estadoRendimiento.toUpperCase());
            configurarColorEstado(estadoRendimiento);

            // Estadísticas principales
            tvTotalReservas.setText(String.valueOf(reporte.getTotalReservas()));
            tvTotalIngresos.setText(reporte.getTotalIngresosFormateado());
            tvPromedioReserva.setText(reporte.getPromedioFormateado());

            // Desglose de reservas
            int activas = reporte.getReservasActivas();
            int canceladas = reporte.getReservasCanceladas();
            double porcentajeActivas = reporte.getPorcentajeReservasActivas();
            double porcentajeCanceladas = reporte.getPorcentajeReservasCanceladas();

            tvReservasActivas.setText(String.format(Locale.getDefault(),
                    "%d (%.0f%%)", activas, porcentajeActivas));
            tvReservasCanceladas.setText(String.format(Locale.getDefault(),
                    "%d (%.0f%%)", canceladas, porcentajeCanceladas));
        }

        private void configurarColorRanking(int ranking) {
            int backgroundColor;
            int textColor;

            switch (ranking) {
                case 1:
                    backgroundColor = ContextCompat.getColor(context, R.color.green_medium);
                    textColor = ContextCompat.getColor(context, R.color.white);
                    break;
                case 2:
                    backgroundColor = ContextCompat.getColor(context, R.color.orange_dark);
                    textColor = ContextCompat.getColor(context, R.color.white);
                    break;
                case 3:
                    backgroundColor = ContextCompat.getColor(context, R.color.red_medium);
                    textColor = ContextCompat.getColor(context, R.color.white);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.gray_medium);
                    textColor = ContextCompat.getColor(context, R.color.white);
                    break;
            }

            rankingCard.setCardBackgroundColor(backgroundColor);
            tvRanking.setTextColor(textColor);
        }

        private void configurarColorEstado(String estado) {
            int backgroundColor;
            int textColor;

            switch (estado.toLowerCase()) {
                case "excelente":
                    backgroundColor = ContextCompat.getColor(context, R.color.green_light);
                    textColor = ContextCompat.getColor(context, R.color.green_dark);
                    break;
                case "muy bueno":
                    backgroundColor = ContextCompat.getColor(context, R.color.green_light);
                    textColor = ContextCompat.getColor(context, R.color.green_dark);
                    break;
                case "bueno":
                    backgroundColor = ContextCompat.getColor(context, R.color.orange_light);
                    textColor = ContextCompat.getColor(context, R.color.orange_dark);
                    break;
                case "regular":
                    backgroundColor = ContextCompat.getColor(context, R.color.orange_light);
                    textColor = ContextCompat.getColor(context, R.color.orange_dark);
                    break;
                case "necesita atención":
                    backgroundColor = ContextCompat.getColor(context, R.color.red_light);
                    textColor = ContextCompat.getColor(context, R.color.red_dark);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.gray_light);
                    textColor = ContextCompat.getColor(context, R.color.gray_dark);
                    break;
            }

            estadoCard.setCardBackgroundColor(backgroundColor);
            tvEstadoRendimiento.setTextColor(textColor);
        }
    }
}