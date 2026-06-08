package com.example.paktrainfoodapp.ui.main.Passenger;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.utils.Refreshable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class CompletedOrdersFragment extends Fragment implements Refreshable {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;

    private ArrayList<OrderModel> orderList;
    private OrdersAdapter adapter;

    private FirebaseFirestore firestore;

    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_passanger_orders_accept_pending_complete,
                container,
                false
        );

        recyclerView = view.findViewById(R.id.recyclerOrders);
        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        adapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        loadCompletedOrders();

        return view;
    }

    private void loadCompletedOrders() {

        CollectionReference ref = firestore.collection("Orders");

        if (listenerRegistration != null)
            listenerRegistration.remove();

        listenerRegistration = ref.addSnapshotListener((snap, e) -> {

            if (e != null || snap == null) return;

            orderList.clear();

            for (DocumentSnapshot doc : snap.getDocuments()) {

                String status = doc.getString("orderStatus");

                // ONLY THESE STATUSES
                if (!"pick_up".equalsIgnoreCase(status)
                        && !"completed".equalsIgnoreCase(status)) {
                    continue;
                }

                String orderId = doc.getId();

                Double totalPrice = doc.getDouble("totalPrice");
                double price = totalPrice != null ? totalPrice : 0;

                orderList.add(new OrderModel(orderId, price, status));
            }

            adapter.notifyDataSetChanged();

            boolean empty = orderList.isEmpty();
            recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
            layoutNoOrders.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    // ================= ADAPTER =================
    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {

        private final ArrayList<OrderModel> items;

        OrdersAdapter(ArrayList<OrderModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.passanger_order_item_simple, parent, false);

            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {

            OrderModel order = items.get(position);

            holder.txtOrderId.setText("#" + order.getOrderId());
            holder.txtTotalPrice.setText("Total: Rs " + order.getTotalPrice());

            holder.timeRow.setVisibility(View.VISIBLE);
            holder.btnReady.setEnabled(false);
            holder.btnReady.setAlpha(0.6f);

            String status = order.getStatus();

            if ("pick_up".equalsIgnoreCase(status)) {
                holder.btnReady.setText("Rider Pick up order");
            }

            else if ("completed".equalsIgnoreCase(status)) {
                holder.btnReady.setText("Completed");
            }

            // CLICK → TOAST ONLY
            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(v.getContext(),
                            "Order: " + order.getOrderId(),
                            Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {

            TextView txtOrderId, txtTotalPrice;
            LinearLayout timeRow;
            Button btnReady;

            VH(@NonNull View itemView) {
                super(itemView);

                txtOrderId = itemView.findViewById(R.id.txtOrderId);
                txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);

                timeRow = itemView.findViewById(R.id.timeRow);
                btnReady = itemView.findViewById(R.id.btnReady);
            }
        }
    }

    @Override
    public void refreshData() {
        orderList.clear();
        adapter.notifyDataSetChanged();
        loadCompletedOrders();
    }
}