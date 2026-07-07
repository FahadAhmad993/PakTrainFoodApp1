package com.example.paktrainfoodapp.ui.main.Delivery.order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryBoyAdapter;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryBoyModel;
import com.example.paktrainfoodapp.ui.main.Restaurant.order.OrderDetailFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Order_Accept_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ArrayList<DeliveryBoyModel> orderList;
    private DeliveryBoyAdapter adapter;

    private String riderId;

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

        riderId = (auth.getCurrentUser() != null)
                ? auth.getCurrentUser().getUid()
                : "";

        adapter = new DeliveryBoyAdapter(requireContext(), orderList,
                new DeliveryBoyAdapter.OnActionClick() {

                    @Override
                    public void onItemClick(DeliveryBoyModel order, int position) {
                        openOrderDetail(order);
                    }

                    @Override
                    public void onAccept(DeliveryBoyModel order, int position) {
                        handleAction(order, position);
                    }

                    @Override
                    public void onButtonClick(DeliveryBoyModel order, int position) {
                        handleAction(order, position);
                    }
                });

        recyclerView.setAdapter(adapter);

        loadAcceptedOrders();

        return view;
    }

    // ================= LOAD =================
    private void loadAcceptedOrders() {

        if (riderId == null || riderId.isEmpty()) return;

        db.collection("Orders")
                .whereEqualTo("acceptedBy", riderId)
                .whereIn("orderStatus", Arrays.asList(
                        "accepted_by_rider",
                        "arrive_rider_at_resturent",
                        "dropped"
                ))
                .addSnapshotListener((query, e) -> {

                    if (e != null || query == null) return;

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        DeliveryBoyModel order =
                                new DeliveryBoyModel(
                                        doc.getId(),
                                        doc.getDouble("totalPrice") != null
                                                ? doc.getDouble("totalPrice")
                                                : 0.0,
                                        doc.getReference().getPath()
                                );

                        order.setStatus(doc.getString("orderStatus"));
                        orderList.add(order);
                    }

                    adapter.notifyDataSetChanged();

                    boolean empty = orderList.isEmpty();
                    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
                    layoutNoOrders.setVisibility(empty ? View.VISIBLE : View.GONE);
                });
    }

    // ================= FLOW LOGIC =================
    private void handleAction(DeliveryBoyModel order, int position) {

        String status = order.getStatus();

        // 🟢 STEP 1 → ARRIVED
        if ("accepted_by_rider".equals(status)) {

            updateStatus(order, "arrive_rider_at_resturent");
        }

        // 🟡 STEP 2 → WAIT (no action)
        else if ("arrive_rider_at_resturent".equals(status)) {

            Toast.makeText(getContext(),
                    "Wait for restaurant to drop order",
                    Toast.LENGTH_SHORT).show();
        }

        // 🔵 STEP 3 → PICKUP
        else if ("dropped".equals(status)) {

            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Pickup")
                    .setMessage("Kya aap order pick karna chahte hain?")
                    .setPositiveButton("YES", (dialog, which) -> {

                        updateStatus(order, "pick_up");

                        // remove from list instantly
                        orderList.remove(position);
                        adapter.notifyItemRemoved(position);
                    })
                    .setNegativeButton("NO", null)
                    .show();
        }
    }

    // ================= DETAIL =================
    private void openOrderDetail(DeliveryBoyModel order) {

        OrderDetailFragment fragment =
                OrderDetailFragment.newInstance(order.getOrderId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // ================= UPDATE =================
    private void updateStatus(DeliveryBoyModel order, String status) {

        db.collection("Orders")
                .document(order.getOrderId())
                .update("orderStatus", status);
    }
}


