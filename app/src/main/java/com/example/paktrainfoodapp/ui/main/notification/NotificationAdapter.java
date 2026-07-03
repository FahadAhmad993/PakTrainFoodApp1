package com.example.paktrainfoodapp.ui.main.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;

import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context context;
    private final List<NotificationModel> list;

    public NotificationAdapter(Context context,
                               List<NotificationModel> list) {

        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_notification,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        NotificationModel model = list.get(position);

        holder.txtTitle.setText(model.getTitle());
        holder.txtBody.setText(model.getBody());
        if (model.getCreatedAt() != null) {

            Date date = model.getCreatedAt().toDate();

            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd MMM yyyy  hh:mm a", Locale.getDefault());

            holder.txtTime.setText(sdf.format(date));

        } else {

            holder.txtTime.setText("");

        }

        // Read / Unread Dot
        if (model.isRead()) {

            holder.viewUnread.setVisibility(View.GONE);

        } else {

            holder.viewUnread.setVisibility(View.VISIBLE);

        }

        // Future Click
        holder.itemView.setOnClickListener(v -> {

            // Firestore Read

            // Open Order Screen

        });

    }

    @Override
    public int getItemCount() {

        return list.size();

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;

        TextView txtTitle;
        TextView txtBody;
        TextView txtTime;

        View viewUnread;

        ViewHolder(@NonNull View itemView) {

            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgIcon);

            txtTitle = itemView.findViewById(R.id.txtTitle);

            txtBody = itemView.findViewById(R.id.txtBody);

            txtTime = itemView.findViewById(R.id.txtTime);

            viewUnread = itemView.findViewById(R.id.viewUnread);

        }

    }

}