package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paktrainfoodapp.R;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.VH> {

    private final List<MenuItem> items;
    private final Context ctx;
    private final OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEdit(MenuItem item);
        void onDelete(MenuItem item);
    }

    public MenuAdapter(Context ctx, List<MenuItem> items, OnItemActionListener listener) {
        this.ctx = ctx;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.restaurent_item_menu, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MenuItem it = items.get(position);

        holder.tvName.setText(it.getName());
        holder.tvDesc.setText(it.getDescription());
        holder.tvPrice.setText("Rs. " + (int) it.getPrice());
        holder.tvTime.setText(it.getTime() != null ? it.getTime() : "");

        // ✅ Safe Base64 decode
        try {
            if (it.getImageUrl() != null && !it.getImageUrl().isEmpty()) {
                byte[] bytes = Base64.decode(it.getImageUrl(), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) holder.iv.setImageBitmap(bmp);
                else holder.iv.setImageResource(R.drawable.ic_food_placeholder);
            } else {
                holder.iv.setImageResource(R.drawable.ic_food_placeholder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.iv.setImageResource(R.drawable.ic_food_placeholder);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(it);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(it);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tvName, tvDesc, tvPrice, tvTime;
        ImageButton btnEdit, btnDelete;

        VH(@NonNull View v) {
            super(v);
            iv = v.findViewById(R.id.item_image);
            tvName = v.findViewById(R.id.item_name);
            tvDesc = v.findViewById(R.id.item_desc);
            tvPrice = v.findViewById(R.id.item_price);
            tvTime = v.findViewById(R.id.item_time);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}




//package com.example.paktrainfoodapp.ui.main.Restaurant;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.util.Base64;
//import android.view.*;
//import android.widget.*;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.paktrainfoodapp.R;
//import java.util.List;
//
//public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.VH> {
//
//    private final List<MenuItem> items;
//    private final Context ctx;
//    private final OnItemActionListener listener;
//
//    public interface OnItemActionListener {
//        void onEdit(MenuItem item);
//        void onDelete(MenuItem item);
//    }
//
//    public MenuAdapter(Context ctx, List<MenuItem> items, OnItemActionListener listener) {
//        this.ctx = ctx;
//        this.items = items;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(ctx).inflate(R.layout.restaurent_item_menu, parent, false);
//        return new VH(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull VH holder, int position) {
//        MenuItem it = items.get(position);
//
//        holder.tvName.setText(it.getName());
//        holder.tvDesc.setText(it.getDescription());
//        holder.tvPrice.setText("Rs. " + (int) it.getPrice());
//        holder.tvTime.setText(it.getTime());
//
//        // ✅ Decode Base64 image
//        try {
//            if (it.getImageUrl() != null && !it.getImageUrl().isEmpty()) {
//                byte[] bytes = Base64.decode(it.getImageUrl(), Base64.DEFAULT);
//                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                holder.iv.setImageBitmap(bmp);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        holder.btnEdit.setOnClickListener(v -> {
//            if (listener != null) listener.onEdit(it);
//        });
//
//        holder.btnDelete.setOnClickListener(v -> {
//            if (listener != null) listener.onDelete(it);
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return items.size();
//    }
//
//    static class VH extends RecyclerView.ViewHolder {
//        ImageView iv;
//        TextView tvName, tvDesc, tvPrice, tvTime;
//        ImageButton btnEdit, btnDelete;
//
//        VH(@NonNull View v) {
//            super(v);
//            iv = v.findViewById(R.id.item_image);
//            tvName = v.findViewById(R.id.item_name);
//            tvDesc = v.findViewById(R.id.item_desc);
//            tvPrice = v.findViewById(R.id.item_price);
//            tvTime = v.findViewById(R.id.item_time);
//            btnEdit = v.findViewById(R.id.btn_edit);
//            btnDelete = v.findViewById(R.id.btn_delete);
//        }
//    }
//}
//
//
//
