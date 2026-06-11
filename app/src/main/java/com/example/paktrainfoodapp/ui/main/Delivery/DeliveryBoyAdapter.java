package com.example.paktrainfoodapp.ui.main.Delivery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;

import java.util.ArrayList;

public class DeliveryBoyAdapter extends RecyclerView.Adapter<DeliveryBoyAdapter.VH> {

    public interface OnActionClick {
        void onItemClick(DeliveryBoyModel order, int position);
        void onAccept(DeliveryBoyModel order, int position);
        void onButtonClick(DeliveryBoyModel order, int position);
    }

    private final Context context;
    private final ArrayList<DeliveryBoyModel> list;
    private final OnActionClick listener;

    public DeliveryBoyAdapter(Context context,
                              ArrayList<DeliveryBoyModel> list,
                              OnActionClick listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.passanger_order_item_simple, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        DeliveryBoyModel order = list.get(position);

        h.txtOrderId.setText("#" + order.getOrderId());
        h.txtTotalPrice.setText("Rs " + order.getTotalPrice());

        String status = order.getStatus();

        // RESET
        h.btnAccept.setVisibility(View.GONE);
        h.btnReady.setVisibility(View.GONE);
        h.timeRow.setVisibility(View.VISIBLE);
        h.txtTimer.setVisibility(View.VISIBLE);

        // ================= READY =================
        if ("ready_for_delivery".equals(status)) {

            h.btnAccept.setVisibility(View.VISIBLE);
            h.txtTimer.setVisibility(View.GONE);

            // 🟢 ICON CLICK
            h.btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccept(order, h.getAdapterPosition());
                }
            });

            // 🟡 ITEM CLICK
            h.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(order, h.getAdapterPosition());
                }
            });
        }

        // ================= ACCEPTED =================
        else if ("accepted_by_rider".equals(status)) {

            h.btnReady.setVisibility(View.VISIBLE);
            h.btnReady.setText("ARRIVED");
            h.btnReady.setEnabled(true);
            h.btnReady.setAlpha(1f);
            h.txtTimer.setVisibility(View.GONE);

        }

        // ================= ARRIVED =================
        else if ("arrive_rider_at_resturent".equals(status)) {

            h.btnReady.setVisibility(View.VISIBLE);
            h.btnReady.setText("READY FOR PICKUP");
            h.btnReady.setEnabled(false);
            h.btnReady.setAlpha(0.4f);
            h.txtTimer.setVisibility(View.GONE);
        }

        // ================= DROPPED =================
        else if ("dropped".equals(status)) {

            h.btnReady.setVisibility(View.VISIBLE);
            h.btnReady.setText("PICK UP");
            h.btnReady.setEnabled(true);
            h.btnReady.setAlpha(1f);
            h.txtTimer.setVisibility(View.GONE);
        }

        // 🔵 BUTTON CLICK (ONLY ONE LISTENER)
        h.btnReady.setOnClickListener(v -> {
            if (listener != null) {
                listener.onButtonClick(order, h.getAdapterPosition());
            }
        });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        ImageView btnAccept;
        TextView txtOrderId, txtTotalPrice, txtTimer;
        Button btnReady;
        View timeRow;

        VH(@NonNull View itemView) {
            super(itemView);

            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);

            btnReady = itemView.findViewById(R.id.btnReady);
            btnAccept = itemView.findViewById(R.id.btnAccept);

            timeRow = itemView.findViewById(R.id.timeRow);
            txtTimer = itemView.findViewById(R.id.txtTimer);
        }
    }
}










