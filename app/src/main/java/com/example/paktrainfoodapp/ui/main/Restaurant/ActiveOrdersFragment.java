package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActiveOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;

    private ArrayList<MenuItem> orderList;
    private OrdersAdapter adapter;

    private FirebaseFirestore firestore;
    private String restaurantUid;

    private ListenerRegistration orderListener;

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
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (!restaurantUid.isEmpty()) {
            loadOrders();
        }
    }

    private void loadOrders() {

        if (orderListener != null) {
            orderListener.remove();
        }

        orderListener = firestore.collection("Orders")
                .whereEqualTo("restaurantUid", restaurantUid)
                .whereEqualTo("orderStatus", "Active")
                .addSnapshotListener((query, e) -> {

                    if (e != null || query == null || !isAdded()) {
                        return;
                    }

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        MenuItem item = new MenuItem();

                        item.setId(doc.getId());

                        item.setPassengerUid(
                                doc.getString("passengerUid")
                        );

                        item.setDocPath(
                                doc.getReference().getPath()
                        );

                        // TOTAL PRICE
                        Double totalPrice = doc.getDouble("totalPrice");

                        if (totalPrice != null) {

                            Map<String, Double> varMap =
                                    new HashMap<>();

                            varMap.put("Total", totalPrice);

                            item.setVariations(varMap);
                        }

                        orderList.add(item);
                    }

                    adapter.notifyDataSetChanged();

                    boolean empty = orderList.isEmpty();

                    recyclerView.setVisibility(
                            empty ? View.GONE : View.VISIBLE
                    );

                    layoutNoOrders.setVisibility(
                            empty ? View.VISIBLE : View.GONE
                    );
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (orderListener != null) {
            orderListener.remove();
        }
    }

    // ====================== ADAPTER ======================

    private class OrdersAdapter
            extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

        private final ArrayList<MenuItem> items;

        OrdersAdapter(ArrayList<MenuItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(
                            R.layout.passanger_order_item_simple,
                            parent,
                            false
                    );

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h,
                                     int position) {

            MenuItem m = items.get(position);

            h.txtOrderId.setText("#" + m.getId());

            double total = 0;

            if (m.getVariations() != null
                    && !m.getVariations().isEmpty()) {

                total = m.getVariations()
                        .values()
                        .iterator()
                        .next();
            }

            h.txtTotalPrice.setText("Total: Rs " + total);

            // SHOW ACCEPT BUTTON
            h.btnAccept.setVisibility(View.VISIBLE);
            h.btnDelete.setVisibility(View.VISIBLE);
            // ACCEPT ORDER
            h.btnAccept.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Accept Order")
                        .setMessage("Accept this order?")
                        .setPositiveButton("Yes",
                                (d, w) ->
                                        updateOrderStatus(m, "Accepted"))
                        .setNegativeButton("No", null)
                        .show();
            } );

            // CANCEL ORDER
            h.btnDelete.setOnClickListener(v -> {

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Cancel Order")
                        .setMessage("Cancel this order?")
                        .setPositiveButton("Yes",
                                (d, w) ->
                                        updateOrderStatus(m, "Cancelled"))
                                       .setNegativeButton("No", null)
                             .show();
            });

            // OPEN DETAIL
            h.itemView.setOnClickListener(v -> {

                Fragment detailFragment =
                        OrderDetailFragment.newInstance(
                                m.getId()
                        );

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.main_container,
                                detailFragment
                        )
                        .addToBackStack("order_detail")
                        .commit();
            });
        }

        private void updateOrderStatus(MenuItem m,
                                       String status) {

            DocumentReference globalRef =
                    firestore.document(m.getDocPath());

            DocumentReference passRef =
                    firestore.collection("Users")
                            .document("Passenger")
                            .collection("OrderNow")
                            .document(m.getPassengerUid())
                            .collection("Orders")
                            .document(m.getId());

            WriteBatch batch = firestore.batch();

            batch.update(globalRef,
                    "orderStatus",
                    status);

            batch.update(passRef,
                    "orderStatus",
                    status);

            batch.commit()
                    .addOnSuccessListener(a -> {

                        Toast.makeText(
                                getContext(),
                                "Order " + status,
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // ====================== VIEW HOLDER ======================

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtOrderId, txtTotalPrice;

            ImageView btnDelete, btnAccept;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                txtOrderId =
                        itemView.findViewById(R.id.txtOrderId);

                txtTotalPrice =
                        itemView.findViewById(R.id.txtTotalPrice);

                btnDelete =
                        itemView.findViewById(R.id.btnDelete);

                btnAccept =
                        itemView.findViewById(R.id.btnAccept);
            }
        }
    }
}



//




