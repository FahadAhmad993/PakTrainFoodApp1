package com.example.paktrainfoodapp.ui.main.Delivery;

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

import java.util.List;

public class DeliveryBoyAdapter extends RecyclerView.Adapter<DeliveryBoyAdapter.ViewHolder> {

    private List<DeliveryBoyModel> deliveryBoyList;

    public DeliveryBoyAdapter(List<DeliveryBoyModel> deliveryBoyList) {
        this.deliveryBoyList = deliveryBoyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery_boy, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeliveryBoyModel boy = deliveryBoyList.get(position);

        // --- Set Name, Phone, Email ---
        holder.tvName.setText(boy.getName() != null && !boy.getName().isEmpty() ? boy.getName() : "DeliveryBoy");
        holder.tvPhone.setText(boy.getPhone() != null ? boy.getPhone() : "N/A");
        holder.tvEmail.setText(boy.getEmail() != null ? boy.getEmail() : "N/A");

        // --- Load Image from Base64 string ---
        if (boy.getImageBase64() != null && !boy.getImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(boy.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgDeliveryBoy.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                holder.imgDeliveryBoy.setImageResource(R.drawable.ic_food_placeholder);
            }
        } else {
            holder.imgDeliveryBoy.setImageResource(R.drawable.ic_food_placeholder);
        }

        // --- Button Click Event ---
        holder.btnDeliverOrder.setOnClickListener(v ->
                Toast.makeText(v.getContext(),
                        "Deliver Order clicked for " + (boy.getName() != null ? boy.getName() : "DeliveryBoy"),
                        Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return deliveryBoyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvEmail;
        ImageView imgDeliveryBoy;
        Button btnDeliverOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            imgDeliveryBoy = itemView.findViewById(R.id.imgDeliveryBoy);
            btnDeliverOrder = itemView.findViewById(R.id.btnDeliverOrder);
        }
    }
}





//package com.example.paktrainfoodapp.ui.main.Delivery;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.example.paktrainfoodapp.R;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//
//import java.util.List;
//
//public class DeliveryBoyAdapter extends RecyclerView.Adapter<DeliveryBoyAdapter.ViewHolder> {
//
//    private List<DeliveryBoyModel> deliveryBoyList;
//
//    public DeliveryBoyAdapter(List<DeliveryBoyModel> deliveryBoyList) {
//        this.deliveryBoyList = deliveryBoyList;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_delivery_boy, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        DeliveryBoyModel boy = deliveryBoyList.get(position);
//
//        holder.tvName.setText(boy.getName() != null && !boy.getName().isEmpty() ? boy.getName() : "DeliveryBoy");
//        holder.tvPhone.setText(boy.getPhone() != null ? boy.getPhone() : "N/A");
//        holder.tvEmail.setText(boy.getEmail() != null ? boy.getEmail() : "N/A");
//
//        // --- Load image from Firebase Storage ---
//        if (boy.getImageBase64() != null && !boy.getImageBase64().isEmpty()) {
//            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(boy.getImageBase64());
//            Glide.with(holder.imgDeliveryBoy.getContext())
//                    .load(storageRef)
//                    .placeholder(R.drawable.ic_food_placeholder)
//                    .error(R.drawable.ic_food_placeholder)
//                    .into(holder.imgDeliveryBoy);
//        } else {
//            holder.imgDeliveryBoy.setImageResource(R.drawable.ic_food_placeholder);
//        }
//
//        holder.btnDeliverOrder.setOnClickListener(v ->
//                Toast.makeText(v.getContext(),
//                        "Deliver Order clicked for " + (boy.getName() != null ? boy.getName() : "DeliveryBoy"),
//                        Toast.LENGTH_SHORT).show()
//        );
//    }
//
//    @Override
//    public int getItemCount() {
//        return deliveryBoyList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView tvName, tvPhone, tvEmail;
//        ImageView imgDeliveryBoy;
//        Button btnDeliverOrder;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvName = itemView.findViewById(R.id.tvName);
//            tvPhone = itemView.findViewById(R.id.tvPhone);
//            tvEmail = itemView.findViewById(R.id.tvEmail);
//            imgDeliveryBoy = itemView.findViewById(R.id.imgDeliveryBoy);
//            btnDeliverOrder = itemView.findViewById(R.id.btnDeliverOrder);
//        }
//    }
//}
