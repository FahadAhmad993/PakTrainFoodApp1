package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.utils.Refreshable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AcceptedOrdersFragment extends Fragment implements Refreshable {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;

    private ArrayList<OrderModel> orderList;
    private OrdersAdapter adapter;

    private FirebaseFirestore firestore;
    private String uid;

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

        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        adapter = new OrdersAdapter(orderList, firestore, uid, this);
        recyclerView.setAdapter(adapter);

        if (uid == null) {
            recyclerView.setVisibility(View.GONE);
            layoutNoOrders.setVisibility(View.VISIBLE);
            return view;
        }

        loadAcceptedOrders();

        return view;
    }

    private void loadAcceptedOrders() {

        CollectionReference ref = firestore.collection("Users")
                .document("Passenger")
                .collection("OrderNow")
                .document(uid)
                .collection("Orders");

        if (listenerRegistration != null)
            listenerRegistration.remove();

        listenerRegistration = ref.addSnapshotListener((snap, e) -> {

            if (e != null || snap == null) return;

            orderList.clear();

            for (DocumentSnapshot doc : snap.getDocuments()) {

                String status = doc.getString("orderStatus");

                // ONLY ACCEPTED
                if ("Accepted".equalsIgnoreCase(status)) {

                    String orderId = doc.getId();

                    Double totalPrice = doc.getDouble("totalPrice");
                    double price = totalPrice != null ? totalPrice : 0;

                    orderList.add(new OrderModel(orderId, price));
                }
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

    // ================= ADAPTER SAME AS ACTIVE =================
    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

        private final ArrayList<OrderModel> items;
        private final FirebaseFirestore firestore;
        private final String uid;
        private final Fragment fragment;

        OrdersAdapter(ArrayList<OrderModel> items,
                      FirebaseFirestore firestore,
                      String uid,
                      Fragment fragment) {

            this.items = items;
            this.firestore = firestore;
            this.uid = uid;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.passanger_order_item_simple, parent, false);

            return new OrderViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

            OrderModel order = items.get(position);

            holder.txtOrderId.setText("#" + order.getOrderId());
            holder.txtTotalPrice.setText("Total: Rs " + order.getTotalPrice());

            // OPEN DETAIL SAME AS ACTIVE
            holder.itemView.setOnClickListener(v -> {

                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                OrderModel selected = items.get(pos);

                passanger_orderDetailFragment detailFragment =
                        new passanger_orderDetailFragment();

                Bundle bundle = new Bundle();
                bundle.putString("orderId", selected.getOrderId());
                detailFragment.setArguments(bundle);

                fragment.requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_holder, detailFragment)
                        .addToBackStack("order_detail")
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class OrderViewHolder extends RecyclerView.ViewHolder {

            TextView txtOrderId, txtTotalPrice;

            OrderViewHolder(@NonNull View itemView) {
                super(itemView);

                txtOrderId = itemView.findViewById(R.id.txtOrderId);
                txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            }
        }
    }
    @Override
    public void refreshData() {

        recyclerView.setVisibility(View.GONE);

        orderList.clear();
        adapter.notifyDataSetChanged();

        loadAcceptedOrders();

        recyclerView.postDelayed(() -> {

            recyclerView.setVisibility(View.VISIBLE);

        }, 2000);
    }
}//