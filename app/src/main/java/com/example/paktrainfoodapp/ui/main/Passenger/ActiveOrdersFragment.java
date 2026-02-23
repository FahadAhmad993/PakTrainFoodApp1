package com.example.paktrainfoodapp.ui.main.Passenger;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class ActiveOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;
    private ArrayList<MenuitemModel> orderList;
    private OrdersAdapter adapter;
    private FirebaseFirestore firestore;
    private String uid;
    private ListenerRegistration listenerRegistration; // for realtime updates

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passanger_orders_accept_pending_complete, container, false);

        recyclerView = view.findViewById(R.id.recyclerOrders);
        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        adapter = new OrdersAdapter(orderList, firestore, uid, this);
        recyclerView.setAdapter(adapter);

        if (uid == null) {
            recyclerView.setVisibility(View.GONE);
            layoutNoOrders.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Login required", Toast.LENGTH_SHORT).show();
            return view;
        }

        listenOrdersRealtime();

        return view;
    }

    private void listenOrdersRealtime() {
        if (uid == null) return;
        CollectionReference ordersRef = firestore.collection("Users")
                .document("Passenger")
                .collection("OrderNow")
                .document(uid)
                .collection("Orders");

        if (listenerRegistration != null) listenerRegistration.remove();

        listenerRegistration = ordersRef.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Listen failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (snap == null) return;

            orderList.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                String orderStatus = doc.getString("orderStatus");
                if ("Active".equalsIgnoreCase(orderStatus) || "Cancelled".equalsIgnoreCase(orderStatus)) {
                    MenuitemModel item = new MenuitemModel();
                    item.setId(doc.getId());
                    item.setName(doc.getString("itemName"));
                    item.setPrice(doc.getDouble("itemPrice") != null ? doc.getDouble("itemPrice") : 0);
                    item.setDescription(doc.getString("itemDesc"));
                    item.setRestaurantName(doc.getString("restaurantName"));
                    item.setRestaurantUid(doc.getString("restaurantUid"));
                    item.setImageUrl(doc.getString("itemImage") != null ? doc.getString("itemImage") : "");
                    item.setOrderStatus(orderStatus != null ? orderStatus : "Active");

                    orderList.add(item);
                }
            }

            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
            layoutNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
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

    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

        private final ArrayList<MenuitemModel> items;
        private final FirebaseFirestore firestore;
        private final String uid;
        private final Fragment fragment;

        OrdersAdapter(ArrayList<MenuitemModel> items, FirebaseFirestore firestore, String uid, Fragment fragment) {
            this.items = items;
            this.firestore = firestore;
            this.uid = uid;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.passanger_item_menu, parent, false);
            return new OrderViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            MenuitemModel item = items.get(position);

            holder.txtName.setText(item.getName());
            holder.txtPrice.setText("Rs. " + item.getPrice());
            holder.txtDesc.setText(item.getDescription());
            holder.txtRest.setText("By " + item.getRestaurantName());

            // Image decode safe
            String img = item.getImageUrl();
            if (img != null && !img.isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(img, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    holder.imgFood.setImageBitmap(bitmap != null ? bitmap : BitmapFactory.decodeResource(fragment.getResources(), R.drawable.ic_food_placeholder));
                } catch (Exception e) {
                    holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
                }
            } else {
                holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
            }

            // Default hide buttons
            holder.btnAddCart.setVisibility(View.GONE);
            holder.btnBuyNow.setVisibility(View.GONE);

            String status = item.getOrderStatus();
            boolean isCancelled = "Cancelled".equalsIgnoreCase(status);
            boolean isActive = "Active".equalsIgnoreCase(status);
            boolean isAccepted = "Accepted".equalsIgnoreCase(status);

            if (isCancelled) {
                if (holder.btnDeleteOrder != null) holder.btnDeleteOrder.setVisibility(View.VISIBLE);
                if (holder.btnAccept != null) holder.btnAccept.setVisibility(View.GONE);
                if (holder.btnCancel != null) holder.btnCancel.setVisibility(View.GONE);

                // 🔹 Now use drawable background with rounded corners + soft red
                holder.itemView.setBackgroundResource(R.drawable.bg_order_cancelled);
                holder.itemView.setElevation(6f); // halka shadow effect for soft red box

                // Show red badge
                TextView badge = holder.itemView.findViewById(R.id.txtStatusBadge);
                if (badge != null) {
                    badge.setVisibility(View.VISIBLE);
                    badge.setText("Canceled");
                    badge.setBackgroundResource(R.drawable.bg_badge_red);
                }
            }
            else if (isActive) {
                if (holder.btnDeleteOrder != null) holder.btnDeleteOrder.setVisibility(View.VISIBLE);
                if (holder.btnAccept != null) holder.btnAccept.setVisibility(View.GONE);
                if (holder.btnCancel != null) holder.btnCancel.setVisibility(View.GONE);

                holder.itemView.setBackgroundResource(R.drawable.bg_order_normal);
                TextView badge = holder.itemView.findViewById(R.id.txtStatusBadge);
                if (badge != null) badge.setVisibility(View.GONE);

            } else if (isAccepted) {
                if (holder.btnDeleteOrder != null) holder.btnDeleteOrder.setVisibility(View.GONE);
                if (holder.btnAccept != null) holder.btnAccept.setVisibility(View.GONE);
                if (holder.btnCancel != null) holder.btnCancel.setVisibility(View.GONE);
                holder.itemView.setBackgroundResource(R.drawable.bg_order_normal);
                TextView badge = holder.itemView.findViewById(R.id.txtStatusBadge);
                if (badge != null) badge.setVisibility(View.GONE);
            }

            holder.btnDeleteOrder.setOnClickListener(v -> {
                new AlertDialog.Builder(fragment.requireContext())
                        .setTitle("Delete Order")
                        .setMessage("Are you sure you want to delete this order?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            firestore.collection("Users")
                                    .document("Passenger")
                                    .collection("OrderNow")
                                    .document(uid)
                                    .collection("Orders")
                                    .document(item.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        String restUid = item.getRestaurantUid();
                                        if (restUid != null && !restUid.isEmpty()) {
                                            firestore.collection("Users")
                                                    .document("Restaurant")
                                                    .collection("VerifiedRegister")
                                                    .document(restUid)
                                                    .collection("Orders")
                                                    .document(item.getId())
                                                    .delete();
                                        }
                                        int pos = holder.getAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION) {
                                            items.remove(pos);
                                            notifyItemRemoved(pos);
                                        }
                                        Toast.makeText(fragment.requireContext(), "Order deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(fragment.requireContext(), "Failed to delete order", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class OrderViewHolder extends RecyclerView.ViewHolder {
            ImageView imgFood;
            TextView txtName, txtPrice, txtDesc, txtRest;
            Button btnAddCart, btnBuyNow, btnDeleteOrder, btnAccept, btnCancel;

            OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                imgFood = itemView.findViewById(R.id.imgFood);
                txtName = itemView.findViewById(R.id.txtName);
                txtPrice = itemView.findViewById(R.id.txtPrice);
                txtDesc = itemView.findViewById(R.id.txtDesc);
                txtRest = itemView.findViewById(R.id.txtRestName);
                btnAddCart = itemView.findViewById(R.id.btnAddCart);
                btnBuyNow = itemView.findViewById(R.id.btnBuyNow);
                btnDeleteOrder = itemView.findViewById(R.id.btnDeleteOrder);
                btnAccept = itemView.findViewById(R.id.btnAcceptOrder);
                btnCancel = itemView.findViewById(R.id.btnCancelOrder);
            }
        }
    }
}





//package com.example.paktrainfoodapp.ui.main.Passenger;
//
//import android.app.AlertDialog;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.paktrainfoodapp.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.util.ArrayList;
//
//public class ActiveOrdersFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private LinearLayout layoutNoOrders;
//    private ArrayList<MenuitemModel> orderList;
//    private OrdersAdapter adapter;
//    private FirebaseFirestore firestore;
//    private String uid;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_passanger_orders_accept_pending_complete, container, false);
//
//        recyclerView = view.findViewById(R.id.recyclerOrders);
//        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        orderList = new ArrayList<>();
//        firestore = FirebaseFirestore.getInstance();
//        uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
//                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
//
//        if (uid == null) {
//            recyclerView.setVisibility(View.GONE);
//            layoutNoOrders.setVisibility(View.VISIBLE);
//            Toast.makeText(getContext(), "Login required", Toast.LENGTH_SHORT).show();
//            return view;
//        }
//
//        adapter = new OrdersAdapter(orderList, firestore, uid, this);
//        recyclerView.setAdapter(adapter);
//
//        loadOrders("Active"); // load only active orders
//        return view;
//    }
//
//    // 🔹 Firestore fetch method
//    private void loadOrders(String status) {
//        if (uid == null) return;
//
//        firestore.collection("Users")
//                .document("Passenger")
//                .collection("OrderNow")
//                .document(uid)
//                .collection("Orders")
//                .get()
//                .addOnSuccessListener(query -> {
//                    orderList.clear();
//
//                    for (QueryDocumentSnapshot doc : query) {
//                        String orderStatus = doc.getString("orderStatus");
//                        if (orderStatus != null && orderStatus.equalsIgnoreCase(status)) {
//
//                            MenuitemModel item = new MenuitemModel();
//                            item.setId(doc.getId());
//                            item.setName(doc.getString("itemName"));
//                            item.setPrice(doc.getDouble("itemPrice") != null ? doc.getDouble("itemPrice") : 0);
//                            item.setDescription(doc.getString("itemDesc"));
//                            item.setRestaurantName(doc.getString("restaurantName"));
//                            item.setRestaurantUid(doc.getString("restaurantUid"));
//                            item.setImageUrl(doc.getString("itemImage") != null ? doc.getString("itemImage") : "");
//                            item.setOrderStatus(orderStatus);
//
//                            orderList.add(item);
//                        }
//                    }
//
//                    adapter.notifyDataSetChanged();
//                    recyclerView.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
//                    layoutNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(getContext(), "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    recyclerView.setVisibility(View.GONE);
//                    layoutNoOrders.setVisibility(View.VISIBLE);
//                });
//    }
//
//    // 🔹 Adapter class
//    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
//
//        private final ArrayList<MenuitemModel> items;
//        private final FirebaseFirestore firestore;
//        private final String uid;
//        private final Fragment fragment;
//
//        OrdersAdapter(ArrayList<MenuitemModel> items, FirebaseFirestore firestore, String uid, Fragment fragment) {
//            this.items = items;
//            this.firestore = firestore;
//            this.uid = uid;
//            this.fragment = fragment;
//        }
//
//        @NonNull
//        @Override
//        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.passanger_item_menu, parent, false);
//            return new OrderViewHolder(v);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
//            MenuitemModel item = items.get(position);
//
//            holder.txtName.setText(item.getName());
//            holder.txtPrice.setText("Rs. " + item.getPrice());
//            holder.txtDesc.setText(item.getDescription());
//            holder.txtRest.setText("By " + item.getRestaurantName());
//
//            // 🔹 Image decoding
//            String img = item.getImageUrl();
//            if (img != null && !img.isEmpty()) {
//                try {
//                    byte[] decoded = Base64.decode(img, Base64.DEFAULT);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
//                    holder.imgFood.setImageBitmap(bitmap != null ? bitmap : BitmapFactory.decodeResource(fragment.getResources(), R.drawable.ic_food_placeholder));
//                } catch (Exception e) {
//                    holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
//                }
//            } else {
//                holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
//            }
//
//            // 🔹 Buttons visibility
//            holder.btnAddCart.setVisibility(View.GONE);
//            holder.btnBuyNow.setVisibility(View.GONE);
//            holder.btnDeleteOrder.setVisibility(View.VISIBLE);
//
//            // 🔹 Delete order
//            holder.btnDeleteOrder.setOnClickListener(v -> {
//                new AlertDialog.Builder(fragment.requireContext())
//                        .setTitle("Delete Order")
//                        .setMessage("Are you sure you want to delete this order?")
//                        .setPositiveButton("Yes", (dialog, which) -> {
//                            firestore.collection("Users")
//                                    .document("Passenger")
//                                    .collection("OrderNow")
//                                    .document(uid)
//                                    .collection("Orders")
//                                    .document(item.getId())
//                                    .delete()
//                                    .addOnSuccessListener(aVoid -> {
//                                        firestore.collection("Users")
//                                                .document("Restaurant")
//                                                .collection("VerifiedRegister")
//                                                .document(item.getRestaurantUid())
//                                                .collection("Orders")
//                                                .document(item.getId())
//                                                .delete();
//
//                                        int pos = holder.getAdapterPosition();
//                                        if(pos != RecyclerView.NO_POSITION){
//                                            items.remove(pos);
//                                            notifyItemRemoved(pos);
//                                        }
//                                        Toast.makeText(fragment.requireContext(), "Order deleted", Toast.LENGTH_SHORT).show();
//                                    })
//                                    .addOnFailureListener(e -> Toast.makeText(fragment.requireContext(), "Failed to delete order", Toast.LENGTH_SHORT).show());
//                        })
//                        .setNegativeButton("No", null)
//                        .show();
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return items.size();
//        }
//
//        static class OrderViewHolder extends RecyclerView.ViewHolder {
//            ImageView imgFood;
//            TextView txtName, txtPrice, txtDesc, txtRest;
//            Button btnAddCart, btnBuyNow, btnDeleteOrder;
//
//            OrderViewHolder(@NonNull View itemView) {
//                super(itemView);
//                imgFood = itemView.findViewById(R.id.imgFood);
//                txtName = itemView.findViewById(R.id.txtName);
//                txtPrice = itemView.findViewById(R.id.txtPrice);
//                txtDesc = itemView.findViewById(R.id.txtDesc);
//                txtRest = itemView.findViewById(R.id.txtRestName);
//                btnAddCart = itemView.findViewById(R.id.btnAddCart);
//                btnBuyNow = itemView.findViewById(R.id.btnBuyNow);
//                btnDeleteOrder = itemView.findViewById(R.id.btnDeleteOrder);
//            }
//        }
//    }
//}
//
