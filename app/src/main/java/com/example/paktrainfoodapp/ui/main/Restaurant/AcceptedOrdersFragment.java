package com.example.paktrainfoodapp.ui.main.Restaurant;

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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AcceptedOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;

    private ArrayList<MenuItem> orderList;
    private OrdersAdapter adapter;

    private FirebaseFirestore firestore;
    private String restaurantUid;

    private ListenerRegistration orderListener;

    private FusedLocationProviderClient fusedLocationClient;
    private double restaurantLat = 0.0;
    private double restaurantLng = 0.0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_restaurant_orders_accept_pending_complete,
                container,
                false
        );

        recyclerView = view.findViewById(R.id.recyclerOrders);
        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        restaurantUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!restaurantUid.isEmpty()) {
            loadAcceptedOrders();
        }
    }

    // ================= LOAD ORDERS =================
    private void loadAcceptedOrders() {

        if (orderListener != null) orderListener.remove();

        orderListener = firestore.collection("Orders")
                .whereEqualTo("restaurantId", restaurantUid)
                .addSnapshotListener((query, e) -> {

                    if (e != null || query == null || !isAdded()) return;

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        String status = doc.getString("orderStatus");
                        if (status == null) continue;

                        // REMOVE ORDER WHEN RIDER ACCEPTS
                        if ("accepted_by_rider".equals(status)) {
                            continue;
                        }
                        // ONLY VALID STATUSES
                        if (!status.equals("Accepted") &&
                                !status.equals("ready_for_delivery")) continue;

                        MenuItem item = new MenuItem();
                        item.setId(doc.getId());
                        item.setPassengerUid(doc.getString("passengerUid"));
                        item.setDocPath(doc.getReference().getPath());
                        item.setStatus(status);

                        Long eta = doc.getLong("etaEndTime");
                        item.setEtaEndTime(eta != null ? eta : 0L);

                        Double price = doc.getDouble("totalPrice");
                        if (price != null) {
                            Map<String, Double> map = new HashMap<>();
                            map.put("Total", price);
                            item.setVariations(map);
                        }

                        orderList.add(item);
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
        if (orderListener != null) orderListener.remove();
    }

    // ================= LOCATION =================
    private void getRestaurantLocation(Runnable callback) {

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                restaurantLat = location.getLatitude();
                restaurantLng = location.getLongitude();
            }
            callback.run();
        });
    }

    // ================= ADAPTER =================
    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

        private final ArrayList<MenuItem> items;
        private final Handler handler = new Handler(Looper.getMainLooper());

        OrdersAdapter(ArrayList<MenuItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.passanger_order_item_simple, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {

            MenuItem m = items.get(position);

            h.txtOrderId.setText("#" + m.getId());

            double total = (m.getVariations() != null && !m.getVariations().isEmpty())
                    ? m.getVariations().values().iterator().next()
                    : 0;

            h.txtTotalPrice.setText("Rs " + total);

            // RESET UI
            h.btnAccept.setVisibility(View.GONE);
            h.btnDelete.setVisibility(View.GONE);

            h.timeRow.setVisibility(View.VISIBLE);
            h.btnReady.setVisibility(View.VISIBLE);
            h.btnReady.setEnabled(true);

            String status = m.getStatus();

// RESET default
            h.btnReady.setEnabled(true);
            h.btnReady.setAlpha(1f);
            h.btnReady.setVisibility(View.VISIBLE);

// STATUS UI CONTROL
            switch (status) {

                case "Accepted":
                    h.btnReady.setText("Ready For Delivery");
                    h.btnReady.setEnabled(true);
                    h.btnReady.setAlpha(1f);
                    break;

                case "ready_for_delivery":
                    h.btnReady.setText("Waiting for Rider...");
                    h.btnReady.setEnabled(false);
                    h.btnReady.setAlpha(0.5f);
                    break;

                case "accepted_by_rider":
                    h.itemView.setVisibility(View.GONE); // REMOVE FROM LIST UI
                    return;
            }

            updateTimer(h, m.getEtaEndTime());

            // ================= READY CLICK =================
            h.btnReady.setOnClickListener(v -> {

                if (!"Accepted".equals(m.getStatus())) return;

                h.btnReady.setEnabled(false);
                h.btnReady.setAlpha(0.5f);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Ready for Delivery")
                        .setMessage("Kya khana tayar hai?")
                        .setPositiveButton("Yes", (d, w) -> {

                            getRestaurantLocation(() -> {

                                Map<String, Object> map = new HashMap<>();
                                map.put("orderStatus", "ready_for_delivery");
                                map.put("restaurantLat", restaurantLat);
                                map.put("restaurantLng", restaurantLng);
                                map.put("readyTime", System.currentTimeMillis());

                                firestore.collection("Orders")
                                        .document(m.getId())
                                        .update(map)
                                        .addOnSuccessListener(unused -> {

                                            m.setStatus("ready_for_delivery");

                                            Toast.makeText(getContext(),
                                                    "Order Ready For Delivery",
                                                    Toast.LENGTH_SHORT).show();

                                            adapter.notifyItemChanged(h.getAdapterPosition());
                                        })
                                        .addOnFailureListener(e -> {

                                            // rollback UI
                                            h.btnReady.setEnabled(true);
                                            h.btnReady.setAlpha(1f);

                                            Toast.makeText(getContext(),
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        });
                            });

                        })
                        .setNegativeButton("No", (d, w) -> {
                            h.btnReady.setEnabled(true);
                            h.btnReady.setAlpha(1f);
                        })
                        .show();
            });

            // ================= DETAIL CLICK =================
            h.itemView.setOnClickListener(v -> {

                Fragment f = OrderDetailFragment.newInstance(m.getId());

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, f)
                        .addToBackStack("order_detail")
                        .commit();
            });
        }

        // ================= TIMER =================
        private void updateTimer(ViewHolder h, long etaEndTime) {

            long remaining = etaEndTime - System.currentTimeMillis();

            if (remaining > 0) {

                long mins = (remaining / 1000) / 60;
                long secs = (remaining / 1000) % 60;

                h.txtTimer.setText(mins + "m " + secs + "s");

                handler.postDelayed(() -> updateTimer(h, etaEndTime), 1000);

            } else {
                h.txtTimer.setText("Arriving");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtOrderId, txtTotalPrice, txtTimer;
            ImageView btnDelete, btnAccept;
            Button btnReady;
            LinearLayout timeRow;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                txtOrderId = itemView.findViewById(R.id.txtOrderId);
                txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
                txtTimer = itemView.findViewById(R.id.txtTimer);

                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnAccept = itemView.findViewById(R.id.btnAccept);

                btnReady = itemView.findViewById(R.id.btnReady);
                timeRow = itemView.findViewById(R.id.timeRow);
            }
        }
    }
}












