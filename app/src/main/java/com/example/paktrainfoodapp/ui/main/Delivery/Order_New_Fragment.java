package com.example.paktrainfoodapp.ui.main.Delivery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class Order_New_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ArrayList<DeliveryBoyModel> orderList;
    private DeliveryBoyAdapter adapter;

    private String riderId;

    private double riderLat = 0;
    private double riderLng = 0;

    private final Map<String, String> restaurantCache = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_delivery_order_new_accept_complete,
                container,
                false
        );

        recyclerView = view.findViewById(R.id.recyclerOrders);
        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        orderList = new ArrayList<>();

        if (auth.getCurrentUser() != null) {
            riderId = auth.getCurrentUser().getUid();
        }

        // ✅ ADAPTER
        adapter = new DeliveryBoyAdapter(
                requireContext(),
                orderList,
                (order, position) -> {

                    db.document(order.getDocPath())
                            .update(
                                    "orderStatus", "Accept",
                                    "acceptedBy", riderId
                            )
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        getContext(),
                                        "Order Accepted",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                });

        recyclerView.setAdapter(adapter);

        getRiderLocation();

        return view;
    }

    // ================= RIDER LOCATION =================
    private void getRiderLocation() {

        com.google.android.gms.location.FusedLocationProviderClient client =
                com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        client.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        riderLat = location.getLatitude();
                        riderLng = location.getLongitude();

                        loadNearbyOrders(); // IMPORTANT
                    }
                });
    }

    // ================= LOAD NEARBY ORDERS =================
    private void loadNearbyOrders() {

        db.collection("Orders")
                .whereEqualTo("orderStatus", "WFR")
                .addSnapshotListener((query, e) -> {

                    if (e != null || query == null) return;

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        Double totalPrice = doc.getDouble("totalPrice");

                        if (totalPrice == null) {
                            totalPrice = 0.0;
                        }

                        DeliveryBoyModel order =
                                new DeliveryBoyModel(
                                        doc.getId(),
                                        totalPrice,
                                        doc.getReference().getPath()
                                );

                        orderList.add(order);
                    }

                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        layoutNoOrders.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        layoutNoOrders.setVisibility(View.GONE);
                    }
                });
    }
}




