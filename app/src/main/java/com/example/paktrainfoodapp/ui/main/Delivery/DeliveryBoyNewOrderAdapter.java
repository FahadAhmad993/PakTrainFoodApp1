package com.example.paktrainfoodapp.ui.main.Delivery;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Restaurant.MenuItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DeliveryBoyNewOrderAdapter extends RecyclerView.Adapter<DeliveryBoyNewOrderAdapter.ViewHolder> {

    private final List<MenuItem> orderList;
    private final FirebaseFirestore db;
    private final OnOrderActionListener listener;

    public DeliveryBoyNewOrderAdapter(List<MenuItem> orderList, OnOrderActionListener listener) {
        this.orderList = orderList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery_boy1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem order = orderList.get(position);

        //  Set order info (assuming fields are from restaurant order)
        holder.tvName.setText(order.getName());
        holder.tvPhone.setText(order.getRestaurantCity());
        holder.tvEmail.setText(order.getRestaurantName());

        //  Set image if Base64
        if (order.getImageUrl() != null && order.getImageUrl().startsWith("data:image")) {
            try {
                String base64String = order.getImageUrl().split(",")[1];
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imgDeliveryBoy.setImageBitmap(decodedBitmap);
            } catch (Exception e) {
                holder.imgDeliveryBoy.setImageResource(R.drawable.ic_food_placeholder);
            }
        } else {
            holder.imgDeliveryBoy.setImageResource(R.drawable.ic_food_placeholder);
        }

        // Accept Order
        holder.btnDeliverOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Accept Order?")
                    .setMessage("Do you want to accept this order?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("users")
                                .document(order.getRestaurantId()) // 🔹 Example structure: users/{restaurantId}/orders
                                .collection("orders")
                                .document(order.getId())
                                .update("status", "Accepted by DeliveryBoy")
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(v.getContext(), "Order Accepted!", Toast.LENGTH_SHORT).show();
                                    listener.onAccept(order);
                                    orderList.remove(position);
                                    notifyItemRemoved(position);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Cancel Order
        holder.btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Cancel Order?")
                    .setMessage("Are you sure you want to cancel this order?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("users")
                                .document(order.getRestaurantId())
                                .collection("orders")
                                .document(order.getId())
                                .update("status", "Cancelled by DeliveryBoy")
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(v.getContext(), "Order Cancelled", Toast.LENGTH_SHORT).show();
                                    listener.onCancel(order);
                                    orderList.remove(position);
                                    notifyItemRemoved(position);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // 🔹 Interface for handling Accept / Cancel actions
    public interface OnOrderActionListener {
        void onAccept(MenuItem order);
        void onCancel(MenuItem order);
    }

    // 🔹 ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvEmail;
        ImageView imgDeliveryBoy;
        Button btnDeliverOrder, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            imgDeliveryBoy = itemView.findViewById(R.id.imgDeliveryBoy);
            btnDeliverOrder = itemView.findViewById(R.id.btnDeliverOrder);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}





