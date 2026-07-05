package com.example.paktrainfoodapp.ui.main.Delivery.order;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryBoyModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;


import java.util.ArrayList;

public class Order_Complete_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ArrayList<DeliveryBoyModel> orderList;
    private CompleteAdapter adapter;

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

        riderId = (auth.getCurrentUser() != null)
                ? auth.getCurrentUser().getUid()
                : "";

        orderList = new ArrayList<>();
        adapter = new CompleteAdapter(orderList);
        recyclerView.setAdapter(adapter);

        loadOrders();

        return view;
    }

    // ================= LOAD ORDERS =================
    private void loadOrders() {

        if (riderId == null || riderId.isEmpty()) return;

        db.collection("Orders")
                .whereEqualTo("acceptedBy", riderId)
                .addSnapshotListener((query, e) -> {

                    if (e != null || query == null) return;

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        String status = doc.getString("orderStatus");
                        Double price = doc.getDouble("totalPrice");

                        // ONLY THESE STATUS FROM YOUR FLOW
                        if (!"pick_up".equals(status) &&
                                !"completed".equals(status)) {
                            continue;
                        }

                        DeliveryBoyModel model = new DeliveryBoyModel(
                                doc.getId(),
                                price != null ? price : 0.0,
                                doc.getReference().getPath()
                        );

                        model.setStatus(status);
                        orderList.add(model);
                    }

                    adapter.notifyDataSetChanged();

                    boolean empty = orderList.isEmpty();
                    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
                    layoutNoOrders.setVisibility(empty ? View.VISIBLE : View.GONE);
                });
    }

    // ================= ADAPTER =================
    private class CompleteAdapter extends RecyclerView.Adapter<CompleteAdapter.VH> {

        private final ArrayList<DeliveryBoyModel> list;

        CompleteAdapter(ArrayList<DeliveryBoyModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.passanger_order_item_simple, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {

            DeliveryBoyModel model = list.get(position);

            h.txtOrderId.setText("Order #" + model.getOrderId());
            h.txtPrice.setText("Rs " + model.getTotalPrice());
h.timeRow.setVisibility(View.VISIBLE);
            String status = model.getStatus();

            // RESET
            h.btnAction.setEnabled(true);
            h.btnAction.setAlpha(1f);

            // ================= STATUS UI =================

            if ("pick_up".equals(status)) {

                h.btnAction.setText("Hand Over to Passenger");

                h.btnAction.setOnClickListener(v -> {

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Delivery")
                            .setMessage("Kya aap order handover kar chuke hain?")
                            .setPositiveButton("YES", (dialog, which) -> {

                                updateStatus(model, "completed");

                                Toast.makeText(getContext(),
                                        "Order Delivered",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("NO", null)
                            .show();
                });
            }

            else if ("completed".equals(status)) {

                h.btnAction.setText("Completed");
                h.btnAction.setEnabled(false);
                h.btnAction.setAlpha(0.5f);
                h.txtTime.setText("Arrive");
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {

            TextView txtOrderId, txtPrice, txtTime;
            Button btnAction;
            LinearLayout timeRow;

            VH(@NonNull View itemView) {
                super(itemView);

                txtOrderId = itemView.findViewById(R.id.txtOrderId);
                txtPrice = itemView.findViewById(R.id.txtTotalPrice);
                btnAction = itemView.findViewById(R.id.btnReady);
                timeRow = itemView.findViewById(R.id.timeRow);
                txtTime=itemView.findViewById(R.id.txtTimer);
            }
        }
    }

    // ================= UPDATE STATUS =================
    private void updateStatus(DeliveryBoyModel model, String status) {

        db.collection("Orders")
                .document(model.getOrderId())
                .update("orderStatus", status);
    }
}