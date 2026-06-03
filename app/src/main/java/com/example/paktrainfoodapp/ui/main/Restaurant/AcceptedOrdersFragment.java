package com.example.paktrainfoodapp.ui.main.Restaurant;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private final Handler timerHandler = new Handler();

    // ================= LOCATION =================
    private FusedLocationProviderClient fusedLocationClient;
    private double restaurantLat = 0;
    private double restaurantLng = 0;

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

        // LOCATION INIT
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

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

        if (orderListener != null) {
            orderListener.remove();
        }

        orderListener = firestore.collection("Orders")
                .whereEqualTo("restaurantUid", restaurantUid)
                .whereIn("orderStatus",
                        java.util.Arrays.asList("Accepted", "WFR"))
                .addSnapshotListener((query, e) -> {

                    if (e != null || query == null || !isAdded()) return;

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        MenuItem item = new MenuItem();

                        item.setId(doc.getId());
                        item.setPassengerUid(doc.getString("passengerUid"));
                        item.setDocPath(doc.getReference().getPath());
                        item.setStatus(doc.getString("orderStatus"));

                        Double totalPrice = doc.getDouble("totalPrice");

                        if (totalPrice != null) {
                            Map<String, Double> map = new HashMap<>();
                            map.put("Total", totalPrice);
                            item.setVariations(map);
                        }

                        orderList.add(item);
                    }

                    adapter.notifyDataSetChanged();

                    boolean empty = orderList.isEmpty();

                    recyclerView.setVisibility(empty ? View.GONE : VISIBLE);
                    layoutNoOrders.setVisibility(empty ? VISIBLE : View.GONE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (orderListener != null) {
            orderListener.remove();
        }
    }

    // ================= LOCATION FUNCTION =================
    private void getRestaurantLocation(Runnable callback) {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

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

            double total = 0;
            if (m.getVariations() != null && !m.getVariations().isEmpty()) {
                total = m.getVariations().values().iterator().next();
            }

            h.txtTotalPrice.setText("Total: Rs " + total);

            h.btnAccept.setVisibility(View.GONE);
            h.btnDelete.setVisibility(View.GONE);
            h.timeRow.setVisibility(View.VISIBLE);

            String status = m.getStatus();

            if ("WFR".equalsIgnoreCase(status)) {

                h.btnReady.setEnabled(false);
                h.btnReady.setAlpha(0.4f);
                h.btnReady.setText("Sent to Rider");

            } else {

                h.btnReady.setEnabled(true);
                h.btnReady.setAlpha(1f);
                h.btnReady.setText("Ready For Delivery");
            }

            // ================= READY BUTTON =================
            h.btnReady.setOnClickListener(v -> {

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getContext());

                builder.setTitle("Confirmation");
                builder.setMessage("Is your food ready for delivery?");

                builder.setPositiveButton("Yes", (dialog, which) -> {

                    // 🔥 FIRST GET LOCATION THEN SAVE
                    getRestaurantLocation(() -> {

                        Map<String, Object> map = new HashMap<>();

                        map.put("orderStatus", "WFR");
                        map.put("restaurantLat", restaurantLat);
                        map.put("restaurantLng", restaurantLng);
                        map.put("wfrTime", System.currentTimeMillis());

                        firestore.collection("Orders")
                                .document(m.getId())
                                .update(map);

                        Toast.makeText(getContext(),
                                "Sent to Riders",
                                Toast.LENGTH_SHORT).show();
                    });

                });

                builder.setNegativeButton("No", (d, w) -> d.dismiss());

                builder.show();
            });

            // ================= DETAIL =================
            h.itemView.setOnClickListener(v -> {

                Fragment detailFragment =
                        OrderDetailFragment.newInstance(m.getId());

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, detailFragment)
                        .addToBackStack("order_detail")
                        .commit();
            });

            // ================= TIMER =================
            DocumentReference orderRef =
                    firestore.collection("Orders")
                            .document(m.getId());

            orderRef.addSnapshotListener((doc, error) -> {

                if (doc == null || !doc.exists()) return;

                Long etaEndTime = doc.getLong("etaEndTime");

                if (etaEndTime == null) return;

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        long remaining =
                                etaEndTime - System.currentTimeMillis();

                        if (remaining > 0) {

                            long mins = remaining / (60 * 1000);
                            long secs = (remaining / 1000) % 60;
                            long hrs = mins / 60;

                            mins = mins % 60;

                            String etaText;

                            if (hrs > 0) {
                                etaText = hrs + " hr " + mins + " min";
                            } else {
                                etaText = mins + " min " + secs + " sec";
                            }

                            h.txtTimer.setText(etaText);

                            timerHandler.postDelayed(this, 1000);

                        } else {
                            h.txtTimer.setText("Arriving");
                        }
                    }
                };

                runnable.run();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtOrderId, txtTotalPrice, txtTimer;
            ImageView btnDelete, btnAccept;
            android.widget.Button btnReady;
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







//









