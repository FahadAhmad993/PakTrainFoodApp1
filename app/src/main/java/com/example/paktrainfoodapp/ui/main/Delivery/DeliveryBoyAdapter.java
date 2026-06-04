package com.example.paktrainfoodapp.ui.main.Delivery;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;

import java.util.ArrayList;

public class DeliveryBoyAdapter extends RecyclerView.Adapter<DeliveryBoyAdapter.VH> {

    public interface OnAcceptClick {
        void onAccept(DeliveryBoyModel order, int position);
    }

    private final Context context;
    private final ArrayList<DeliveryBoyModel> list;
    private final OnAcceptClick listener;

    public DeliveryBoyAdapter(
            Context context,
            ArrayList<DeliveryBoyModel> list,
            OnAcceptClick listener) {

        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.passanger_order_item_simple,
                        parent,
                        false
                );

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VH holder,
            int position) {

        DeliveryBoyModel order = list.get(position);

        holder.txtOrderId.setText(
                "Order ID: " + order.getOrderId());

        holder.txtTotalPrice.setText(
                "Rs. " + (int) order.getTotalPrice());

        holder.btnAccept.setVisibility(View.VISIBLE);

        holder.btnAccept.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Accept Order")
                    .setMessage("Accept this order?")
                    .setPositiveButton("Yes",
                            (dialog, which) -> {

                                if (listener != null) {
                                    listener.onAccept(
                                            order,
                                            holder.getAdapterPosition()
                                    );
                                }
                            })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtOrderId;
        TextView txtTotalPrice;
        ImageView btnAccept;

        VH(@NonNull View itemView) {
            super(itemView);

            txtOrderId =
                    itemView.findViewById(R.id.txtOrderId);

            txtTotalPrice =
                    itemView.findViewById(R.id.txtTotalPrice);

            btnAccept =
                    itemView.findViewById(R.id.btnAccept);
        }
    }
}



//






