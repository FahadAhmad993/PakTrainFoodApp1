package com.example.paktrainfoodapp.ui.main.Delivery;

import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Restaurant.OrderDetailFragment;
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

        // ✅ ADAPTER FIXED CALLBACK
        adapter = new DeliveryBoyAdapter(requireContext(), orderList,
                new DeliveryBoyAdapter.OnActionClick() {

                    @Override
                    public void onItemClick(DeliveryBoyModel order, int position) {
                        openDetail(order);
                    }

                    @Override
                    public void onAccept(DeliveryBoyModel order, int position) {
                        showAcceptDialog(order, position);
                    }

                    @Override
                    public void onButtonClick(DeliveryBoyModel order, int position) {
                        handleButton(order, position);
                    }
                });

        recyclerView.setAdapter(adapter);

        loadNearbyOrders();

        return view;
    }

    // ================= POPUP =================
    private void showAcceptDialog(DeliveryBoyModel order, int position) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Accept Order")
                .setMessage("Kya aap ye order accept karna chahte hain?")
                .setPositiveButton("YES", (dialog, which) -> acceptOrder(order, position))
                .setNegativeButton("NO", null)
                .show();
    }

    // ================= ACCEPT ORDER =================
    private void acceptOrder(DeliveryBoyModel order, int position) {

        db.collection("Orders")
                .document(order.getOrderId())
                .update(
                        "orderStatus", "accepted_by_rider",
                        "acceptedBy", riderId
                )
                .addOnSuccessListener(unused -> {

                    Toast.makeText(getContext(),
                            "Order Accepted",
                            Toast.LENGTH_SHORT).show();

                    if (position != RecyclerView.NO_POSITION &&
                            position < orderList.size()) {

                        orderList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // ================= DETAIL OPEN =================
    private void openDetail(DeliveryBoyModel order) {

        OrderDetailFragment fragment =
                OrderDetailFragment.newInstance(order.getOrderId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .addToBackStack("order_detail")
                .commit();
    }

    // ================= LOAD ORDERS =================
    private void loadNearbyOrders() {

        db.collection("Orders")
                .whereEqualTo("orderStatus", "ready_for_delivery")
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
    private void handleButton(DeliveryBoyModel order, int position) {

        String status = order.getStatus();

        if ("accepted_by_rider".equals(status)) {

            updateStatus(order, "arrive_rider_at_resturent");
            Toast.makeText(getContext(), "Arrived marked", Toast.LENGTH_SHORT).show();

        } else if ("arrive_rider_at_resturent".equals(status)) {

            Toast.makeText(getContext(), "Wait for restaurant", Toast.LENGTH_SHORT).show();

        } else if ("dropped".equals(status)) {

            new AlertDialog.Builder(requireContext())
                    .setTitle("Pickup Order")
                    .setMessage("Confirm pickup?")
                    .setPositiveButton("YES", (d, w) -> {
                        updateStatus(order, "pick_up");
                        orderList.remove(position);
                        adapter.notifyItemRemoved(position);
                    })
                    .setNegativeButton("NO", null)
                    .show();
        }
    }

    private void updateStatus(DeliveryBoyModel order, String arriveRiderAtResturent) {
    }
}






