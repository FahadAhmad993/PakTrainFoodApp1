package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class CompletedOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;

    private ArrayList<MenuItem> orderList;
    private OrdersAdapter adapter;

    private FirebaseFirestore firestore;
    private String restaurantUid;

    private ListenerRegistration listener;

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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!restaurantUid.isEmpty()) {
            loadOrders();
        }
    }

    // ================= LOAD ORDERS =================
    private void loadOrders() {

        if (listener != null) listener.remove();

        listener = firestore.collection("Orders")
                .whereEqualTo("restaurantUid", restaurantUid)
                .addSnapshotListener((query, e) -> {

                    if (e != null || query == null || !isAdded()) return;

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        String status = doc.getString("orderStatus");
                        if (status == null) continue;

                        // ONLY pick_up + completed
                        if (!status.equals("pick_up") &&
                                !status.equals("completed")) {
                            continue;
                        }

                        MenuItem item = new MenuItem();
                        item.setId(doc.getId());
                        item.setStatus(status);

                        Long time = doc.getLong("etaEndTime");
                        item.setEtaEndTime(time != null ? time : 0L);

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

            double total = (m.getVariations() != null && !m.getVariations().isEmpty())
                    ? m.getVariations().values().iterator().next()
                    : 0;

            h.txtTotalPrice.setText("Rs " + total);

            String status = m.getStatus();

            // ================= UI RESET =================
            h.btnReady.setVisibility(View.VISIBLE);
            h.btnReady.setEnabled(false);
            h.btnReady.setClickable(false);
            h.btnReady.setAlpha(0.6f);
            h.timeRow.setVisibility(View.VISIBLE);


            // ================= STATUS UI =================
            switch (status) {

                case "pick_up":
                    h.btnReady.setText("Order Delivered to Rider");
                    break;

                case "completed":
                    h.btnReady.setText("Completed");
                    h.txtTimer.setText("Arrived");
                    break;

                default:
                    h.btnReady.setText(status);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtOrderId, txtTotalPrice, txtTimer;
            Button btnReady;
LinearLayout timeRow;
            ViewHolder(@NonNull View itemView) {
                super(itemView);

                txtOrderId = itemView.findViewById(R.id.txtOrderId);
                txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
                txtTimer = itemView.findViewById(R.id.txtTimer);
                btnReady = itemView.findViewById(R.id.btnReady);
                timeRow = itemView.findViewById(R.id.timeRow);
            }
        }
    }
}