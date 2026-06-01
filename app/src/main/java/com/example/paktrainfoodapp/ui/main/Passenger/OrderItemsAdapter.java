package com.example.paktrainfoodapp.ui.main.Passenger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.R;

import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ViewHolder> {

    private List<MenuitemModel> list;

    public OrderItemsAdapter(List<MenuitemModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.passanger_item_menu, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MenuitemModel item = list.get(position);

        holder.txtName.setText(item.getName());
        holder.txtDesc.setText(item.getDescription());
        holder.txtPrice.setText("Rs " + item.getPrice());
        holder.txtRestName.setText(item.getRestaurantName());

        // ================= IMAGE LOAD (IMAGEBB URL) =================
        String imageUrl = item.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {

            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.hlo)
                    .error(R.drawable.hlo)
                    .into(holder.imgItem);
        } else {
            holder.imgItem.setImageResource(R.drawable.hlo);
        }
        holder.txtQuantity.setVisibility(View.VISIBLE);
        holder.txtQuantity.setText("Quantity: " + item.getQuantity());
        // ================= HIDE BUTTONS =================
        if (holder.btnAddToCart != null)
            holder.btnAddToCart.setVisibility(View.GONE);

        if (holder.btnBuyNow != null)
            holder.btnBuyNow.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtDesc, txtPrice, txtRestName;
        ImageView imgItem;
        View btnAddToCart, btnBuyNow;
        TextView txtQuantity;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtDesc = itemView.findViewById(R.id.txtDesc);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtRestName = itemView.findViewById(R.id.txtRestName);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);

            // ✅ IMAGE ID FIXED HERE
            imgItem = itemView.findViewById(R.id.imgFood);

            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnBuyNow = itemView.findViewById(R.id.btnBuyNow);
        }
    }
}




