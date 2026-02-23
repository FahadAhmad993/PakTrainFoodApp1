package com.example.paktrainfoodapp.ui.main.Passenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private final List<MenuitemModel> itemList;
    private OnMenuItemClickListener listener;
    private boolean showButtons = true;
    private boolean showDeleteButton = false;

    public MenuAdapter(List<MenuitemModel> itemList) {
        this.itemList = itemList;
    }

    public void setShowButtons(boolean show) {
        this.showButtons = show;
        notifyDataSetChanged();
    }

    public void setShowDeleteButton(boolean showDeleteButton) {
        this.showDeleteButton = showDeleteButton;
        notifyDataSetChanged();
    }

    public interface OnMenuItemClickListener {
        void onAddToCart(MenuitemModel item);
        void onBuyNow(MenuitemModel item);
        void onDeleteOrder(MenuitemModel item);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.listener = listener;
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
        MenuitemModel item = itemList.get(position);

        holder.txtName.setText(item.getName());
        holder.txtDesc.setText(item.getDescription());
        holder.txtPrice.setText("Rs. " + item.getPrice());
        holder.txtRestName.setText("By " + item.getRestaurantName());

        // ✅ Safe Base64 decoding
        setImageFromBase64(holder.imgFood, item.getImageUrl());

        // Button visibility
        if (showDeleteButton) {
            holder.btnDeleteOrder.setVisibility(View.VISIBLE);
            holder.btnAddCart.setVisibility(View.GONE);
            holder.btnBuyNow.setVisibility(View.GONE);
        } else {
            holder.btnDeleteOrder.setVisibility(View.GONE);
            holder.btnAddCart.setVisibility(showButtons ? View.VISIBLE : View.GONE);
            holder.btnBuyNow.setVisibility(showButtons ? View.VISIBLE : View.GONE);
        }

        // Button Clicks
        holder.btnAddCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(item);
        });

        holder.btnBuyNow.setOnClickListener(v -> {
            if (listener != null) listener.onBuyNow(item);
        });

        holder.btnDeleteOrder.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteOrder(item);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ✅ Separate method for Base64 decoding to avoid crashes
    private void setImageFromBase64(ImageView imageView, String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_food_placeholder);
            return;
        }

        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_food_placeholder);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.ic_food_placeholder);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView txtName, txtDesc, txtPrice, txtRestName;
        Button btnAddCart, btnBuyNow, btnDeleteOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            txtName = itemView.findViewById(R.id.txtName);
            txtDesc = itemView.findViewById(R.id.txtDesc);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtRestName = itemView.findViewById(R.id.txtRestName);
            btnAddCart = itemView.findViewById(R.id.btnAddCart);
            btnBuyNow = itemView.findViewById(R.id.btnBuyNow);
            btnDeleteOrder = itemView.findViewById(R.id.btnDeleteOrder);
        }
    }
}




//package com.example.paktrainfoodapp.ui.main.Passenger;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.paktrainfoodapp.R;
//
//import java.util.List;
//
//public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
//
//    private final List<MenuitemModel> itemList;
//    private OnMenuItemClickListener listener;
//    private boolean showButtons = true;
//    private boolean showDeleteButton = false;
//
//    public MenuAdapter(List<MenuitemModel> itemList) {
//        this.itemList = itemList;
//    }
//
//    public void setShowButtons(boolean show) {
//        this.showButtons = show;
//        notifyDataSetChanged();
//    }
//
//    public void setShowDeleteButton(boolean showDeleteButton) {
//        this.showDeleteButton = showDeleteButton;
//        notifyDataSetChanged();
//    }
//
//    public interface OnMenuItemClickListener {
//        void onAddToCart(MenuitemModel item);
//        void onBuyNow(MenuitemModel item);
//        void onDeleteOrder(MenuitemModel item);
//    }
//
//    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.passanger_item_menu, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        MenuitemModel item = itemList.get(position);
//
//        holder.txtName.setText(item.getName());
//        holder.txtDesc.setText(item.getDescription());
//        holder.txtPrice.setText("Rs. " + item.getPrice());
//        holder.txtRestName.setText("By " + item.getRestaurantName());
//
//        // ✅ Safe Base64 decode with null check
//        try {
//            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
//                byte[] decodedBytes = Base64.decode(item.getImageUrl(), Base64.DEFAULT);
//                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//                if (bitmap != null) {
//                    holder.imgFood.setImageBitmap(bitmap);
//                } else {
//                    holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
//                }
//            } else {
//                holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
//        }
//
//        // control button visibility
//        if (showDeleteButton) {
//            holder.btnDeleteOrder.setVisibility(View.VISIBLE);
//            holder.btnAddCart.setVisibility(View.GONE);
//            holder.btnBuyNow.setVisibility(View.GONE);
//        } else {
//            holder.btnDeleteOrder.setVisibility(View.GONE);
//            if (!showButtons) {
//                holder.btnAddCart.setVisibility(View.GONE);
//                holder.btnBuyNow.setVisibility(View.GONE);
//            } else {
//                holder.btnAddCart.setVisibility(View.VISIBLE);
//                holder.btnBuyNow.setVisibility(View.VISIBLE);
//            }
//        }
//
//        // Button Clicks
//        holder.btnAddCart.setOnClickListener(v -> {
//            if (listener != null) listener.onAddToCart(item);
//        });
//
//        holder.btnBuyNow.setOnClickListener(v -> {
//            if (listener != null) listener.onBuyNow(item);
//        });
//
//        holder.btnDeleteOrder.setOnClickListener(v -> {
//            if (listener != null) listener.onDeleteOrder(item);
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return itemList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView imgFood;
//        TextView txtName, txtDesc, txtPrice, txtRestName;
//        Button btnAddCart, btnBuyNow, btnDeleteOrder;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            imgFood = itemView.findViewById(R.id.imgFood);
//            txtName = itemView.findViewById(R.id.txtName);
//            txtDesc = itemView.findViewById(R.id.txtDesc);
//            txtPrice = itemView.findViewById(R.id.txtPrice);
//            txtRestName = itemView.findViewById(R.id.txtRestName);
//            btnAddCart = itemView.findViewById(R.id.btnAddCart);
//            btnBuyNow = itemView.findViewById(R.id.btnBuyNow);
//            btnDeleteOrder = itemView.findViewById(R.id.btnDeleteOrder);
//        }
//    }
//}
//
//
