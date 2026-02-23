package com.example.paktrainfoodapp.ui.main.Passenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;

import java.util.ArrayList;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private List<RestaurantModel> list;
    private OnItemClickListener listener;

    // Interface for click events (like favorite)
    public interface OnItemClickListener {
        void onFavoriteClick(int position);
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public RestaurantAdapter(ArrayList<RestaurantModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.passanger_resturent_layout, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RestaurantModel model = list.get(position);

        holder.tvName.setText(model.getRestaurantName());
        holder.tvInfo.setText(model.getCity());

        // Base64 image decode safely
        if (model.getImageBase64() != null && !model.getImageBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(model.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imgRestaurant.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imgRestaurant.setImageResource(R.drawable.station3); // fallback image
            }
        } else {
            holder.imgRestaurant.setImageResource(R.drawable.station3); // default image
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgRestaurant, btnFavorite;
        TextView tvName, tvInfo, tvRating, tvAd;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            imgRestaurant = itemView.findViewById(R.id.img_restaurant);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            tvName = itemView.findViewById(R.id.tv_restaurant_name);
            tvInfo = itemView.findViewById(R.id.tv_restaurant_info);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvAd = itemView.findViewById(R.id.tv_ad);

            // Favorite click
            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFavoriteClick(position);
                    }
                }
            });

            // Whole item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
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
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.paktrainfoodapp.ui.main.Passenger.RestaurantModel;
//import com.example.paktrainfoodapp.R;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
//
//    List<RestaurantModel> list;
//
//    public RestaurantAdapter(ArrayList<com.example.paktrainfoodapp.ui.main.Passenger.RestaurantModel> list) {
//        this.list = list;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.passanger_resturent_layout, parent, false);  // your first XML card
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//
//        RestaurantModel model = list.get(position);
//
//        holder.tvName.setText(model.getRestaurantName());
//        holder.tvInfo.setText(model.getCity());
//
//        // Base64 image decode
//        if (model.getImageBase64() != null) {
//            try {
//                byte[] bytes = Base64.decode(model.getImageBase64(), Base64.DEFAULT);
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                holder.imgRestaurant.setImageBitmap(bitmap);
//            } catch (Exception e) { e.printStackTrace(); }
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//
//        ImageView imgRestaurant;
//        TextView tvName, tvInfo;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            imgRestaurant = itemView.findViewById(R.id.img_restaurant);
//            tvName = itemView.findViewById(R.id.tv_restaurant_name);
//            tvInfo = itemView.findViewById(R.id.tv_restaurant_info);
//        }
//    }
//}
