package com.example.paktrainfoodapp.ui.main.Passenger.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;

import java.util.ArrayList;

public class OrderSummaryAdapter
        extends RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder> {

    private final ArrayList<CartItem> list;

    public OrderSummaryAdapter(ArrayList<CartItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_order_summary,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        CartItem item = list.get(position);

        holder.tvItemName.setText(
                item.getName() +
                        " x" +
                        item.getQuantity()
        );

        holder.tvItemPrice.setText(
                "Rs. " +
                        (int) item.getTotal()
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvItemName;
        TextView tvItemPrice;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvItemName =
                    itemView.findViewById(R.id.tvItemName);

            tvItemPrice =
                    itemView.findViewById(R.id.tvItemPrice);
        }
    }
}