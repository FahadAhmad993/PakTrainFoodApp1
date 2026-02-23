package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class AcceptedOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;
    private ArrayList<MenuItem> orderList;
    private OrdersAdapter adapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_restaurant_orders_accept_pending_complete, container, false);

        recyclerView = view.findViewById(R.id.recyclerOrders);
        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadOrders("Accepted");
        return view;
    }

    private void loadOrders(String status) {
        String restId = auth.getCurrentUser().getUid();

        // collectionGroup query for Accepted Orders
        firestore.collectionGroup("Orders")
                .whereEqualTo("restaurantUid", restId)
                .whereEqualTo("orderStatus", status)
                .get()
                .addOnSuccessListener(query -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        MenuItem item = new MenuItem();
                        item.setId(doc.getId());
                        item.setName(doc.getString("itemName"));
                        item.setPrice(doc.getDouble("itemPrice") != null ? doc.getDouble("itemPrice") : 0.0);
                        item.setDescription(doc.getString("itemDesc"));
                        item.setImageUrl(doc.getString("itemImage"));
                        item.setPassengerUid(doc.getString("passengerUid"));
                        item.setDocPath(doc.getReference().getPath());
                        item.setMeta(doc.getData());
                        orderList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                    layoutNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    recyclerView.setVisibility(View.GONE);
                    layoutNoOrders.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ===================== Orders Adapter =====================
    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
        private final ArrayList<MenuItem> items;

        OrdersAdapter(ArrayList<MenuItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.passanger_item_menu, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            MenuItem m = items.get(pos);

            h.name.setText(m.getName());
            h.price.setText("Rs. " + m.getPrice());
            h.desc.setText(m.getDescription());

            // Decode base64 image
            if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
                try {
                    byte[] dec = Base64.decode(m.getImageUrl(), Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    if (bmp != null) h.image.setImageBitmap(bmp);
                    else h.image.setImageResource(R.drawable.ic_food_placeholder);
                } catch (Exception e) {
                    h.image.setImageResource(R.drawable.ic_food_placeholder);
                }
            } else {
                h.image.setImageResource(R.drawable.ic_food_placeholder);
            }

            // Hide passenger-only buttons
            if (h.btnAddToCart != null) h.btnAddToCart.setVisibility(View.GONE);
            if (h.btnDelete != null) h.btnDelete.setVisibility(View.GONE);
            if (h.btnBuyNow != null) h.btnBuyNow.setVisibility(View.GONE);

            // Show restaurant action buttons
            if (h.btnDelivered != null) {
                h.btnDelivered.setVisibility(View.VISIBLE);
                h.btnDelivered.setText("Delivered To");
                h.btnDelivered.setBackgroundTintList(
                        h.itemView.getContext().getResources().getColorStateList(R.color.teal_500)
                );
                h.btnDelivered.setTextColor(
                        h.itemView.getContext().getResources().getColor(android.R.color.white)
                );

                h.btnDelivered.setOnClickListener(v -> openDeliveryBoysDialog(m, pos));
            }

            if (h.txtBadge != null) {
                h.txtBadge.setVisibility(View.VISIBLE);
                h.txtBadge.setText("Accepted");
                h.txtBadge.setBackgroundResource(R.drawable.bg_badge_green);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, price, desc, txtBadge;
            ImageView image;
            Button btnAddToCart, btnDelete, btnBuyNow, btnDelivered;

            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.txtName);
                price = v.findViewById(R.id.txtPrice);
                desc = v.findViewById(R.id.txtDesc);
                image = v.findViewById(R.id.imgFood);
                txtBadge = v.findViewById(R.id.txtStatusBadge);
                btnDelivered = v.findViewById(R.id.btnDeliveredOrder);
                btnAddToCart = v.findViewById(R.id.btnAddCart);
                btnDelete = v.findViewById(R.id.btnDeleteOrder);
                btnBuyNow = v.findViewById(R.id.btnBuyNow);
            }
        }
    }

    // ===================== Delivery Dialog =====================
    private void openDeliveryBoysDialog(MenuItem orderItem, int orderPosition) {
        String restId = auth.getCurrentUser().getUid();

        firestore.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(restId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(getContext(), "Restaurant data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String city = doc.getString("city");
                    if (city == null || city.isEmpty()) {
                        Toast.makeText(getContext(), "City not found for restaurant", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showDeliveryDialogForCity(orderItem, orderPosition, city);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeliveryDialogForCity(MenuItem orderItem, int orderPosition, String city) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Delivery Boy (" + city + ")");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_resturent__delivery, null);
        RecyclerView rv = dialogView.findViewById(R.id.recyclerDeliveryBoys);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        final ArrayList<DeliveryBoyInfo> deliveryList = new ArrayList<>();
        final DeliveryAssignAdapter assignAdapter = new DeliveryAssignAdapter(deliveryList, orderItem, orderPosition);
        rv.setAdapter(assignAdapter);

        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", (d, which) -> d.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        firestore.collection("Users")
                .document("Delivery")
                .collection("VerifiedRegister")
                .whereEqualTo("city", city)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    deliveryList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        DeliveryBoyInfo info = new DeliveryBoyInfo();
                        info.uid = doc.getId();
                        info.name = doc.getString("name");
                        info.phone = doc.getString("phone");
                        info.email = doc.getString("email");
                        info.onlineStatus = doc.getString("onlineStatus");
                        deliveryList.add(info);
                    }

                    for (DeliveryBoyInfo b : deliveryList) {
                        firestore.collection("Users")
                                .document("Delivery")
                                .collection("Register")
                                .document(b.uid)
                                .get()
                                .addOnSuccessListener(imageDoc -> {
                                    if (imageDoc.exists()) {
                                        String base64 = imageDoc.getString("imageBase64");
                                        b.imageBase64 = base64;
                                        int idx = deliveryList.indexOf(b);
                                        if (idx != -1) assignAdapter.notifyItemChanged(idx);
                                    }
                                });
                    }

                    assignAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading delivery boys: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ===================== Delivery Adapter =====================
    private class DeliveryAssignAdapter extends RecyclerView.Adapter<DeliveryAssignAdapter.DBViewHolder> {
        private final ArrayList<DeliveryBoyInfo> list;
        private final MenuItem order;
        private final int orderPos;

        DeliveryAssignAdapter(ArrayList<DeliveryBoyInfo> list, MenuItem order, int orderPos) {
            this.list = list;
            this.order = order;
            this.orderPos = orderPos;
        }

        @NonNull
        @Override
        public DBViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_boy, parent, false);
            return new DBViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DBViewHolder h, int pos) {
            DeliveryBoyInfo b = list.get(pos);

            h.tvName.setText(b.name != null ? b.name : "No Name");
            h.tvPhone.setText(b.phone != null ? b.phone : "No Phone");
            h.tvEmail.setText(b.email != null ? b.email : "No Email");
            h.txtDeliveryOnlineStatus.setText(b.onlineStatus != null ? b.onlineStatus : "Offline");

            if (b.imageBase64 != null) {
                try {
                    byte[] dec = Base64.decode(b.imageBase64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    if (bmp != null) h.imgDeliveryBoy.setImageBitmap(bmp);
                } catch (Exception ignored) {}
            }

            h.btnDeliverOrder.setOnClickListener(v -> {
                h.btnDeliverOrder.setEnabled(false);
                assignOrderToDeliveryBoy(order, orderPos, b.uid, success -> {
                    if (success) {
                        Toast.makeText(v.getContext(), "Order assigned to " + b.name, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "Failed to assign", Toast.LENGTH_SHORT).show();
                        h.btnDeliverOrder.setEnabled(true);
                    }
                });
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class DBViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPhone, tvEmail, txtDeliveryOnlineStatus;
            ImageView imgDeliveryBoy;
            Button btnDeliverOrder;

            DBViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvName);
                tvPhone = v.findViewById(R.id.tvPhone);
                tvEmail = v.findViewById(R.id.tvEmail);
                txtDeliveryOnlineStatus = new TextView(v.getContext());
                btnDeliverOrder = v.findViewById(R.id.btnDeliverOrder);
                imgDeliveryBoy = v.findViewById(R.id.imgDeliveryBoy);
            }
        }
    }

    // ===================== Assign Logic =====================
    private void assignOrderToDeliveryBoy(MenuItem order, int orderPos, String deliveryUid, AssignmentCallback callback) {
        if (order == null || order.getId() == null || order.getDocPath() == null) {
            callback.onComplete(false);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("orderStatus", "Assigned");
        updates.put("assignedTo", deliveryUid);
        updates.put("assignedAt", FieldValue.serverTimestamp());

        DocumentReference orderRef = firestore.document(order.getDocPath());
        orderRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> orderData = new HashMap<>();
                    if (order.getMeta() != null) orderData.putAll(order.getMeta());
                    orderData.put("orderStatus", "New");
                    orderData.put("assignedTo", deliveryUid);
                    orderData.put("assignedFromRestaurant", auth.getCurrentUser().getUid());
                    orderData.put("originalOrderId", order.getId());
                    orderData.put("assignedAt", FieldValue.serverTimestamp());

                    DocumentReference deliveryOrderRef = firestore.collection("Users")
                            .document("Delivery")
                            .collection("VerifiedRegister")
                            .document(deliveryUid)
                            .collection("Orders")
                            .document(order.getId());

                    deliveryOrderRef.set(orderData)
                            .addOnSuccessListener(v -> {
                                if (orderPos >= 0 && orderPos < orderList.size()) {
                                    orderList.remove(orderPos);
                                    adapter.notifyItemRemoved(orderPos);
                                }
                                callback.onComplete(true);
                            })
                            .addOnFailureListener(e -> callback.onComplete(false));
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    private interface AssignmentCallback {
        void onComplete(boolean success);
    }

    private static class DeliveryBoyInfo {
        String uid;
        String name;
        String phone;
        String email;
        String onlineStatus;
        String imageBase64;
    }
}






//package com.example.paktrainfoodapp.ui.main.Restaurant;
//
//import android.app.AlertDialog;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Base64;
//import android.view.*;
//import android.widget.*;
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.paktrainfoodapp.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.*;
//import java.util.*;
//
//public class AcceptedOrdersFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private LinearLayout layoutNoOrders;
//    private ArrayList<MenuItem> orderList;
//    private OrdersAdapter adapter;
//    private FirebaseFirestore firestore;
//    private FirebaseAuth auth;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_restaurant_orders_accept_pending_complete, container, false);
//
//        recyclerView = view.findViewById(R.id.recyclerOrders);
//        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        orderList = new ArrayList<>();
//        adapter = new OrdersAdapter(orderList);
//        recyclerView.setAdapter(adapter);
//
//        firestore = FirebaseFirestore.getInstance();
//        auth = FirebaseAuth.getInstance();
//
//        loadOrders("Accepted");
//        return view;
//    }
//
//    private void loadOrders(String status) {
//        String restId = auth.getCurrentUser().getUid();
//
//        firestore.collection("Users")
//                .document("Restaurant")
//                .collection("VerifiedRegister")
//                .document(restId)
//                .collection("Orders")
//                .whereEqualTo("orderStatus", status)
//                .get()
//                .addOnSuccessListener(query -> {
//                    orderList.clear();
//                    for (QueryDocumentSnapshot doc : query) {
//                        MenuItem item = new MenuItem();
//                        item.setId(doc.getId());
//                        item.setName(doc.getString("itemName"));
//                        Double price = doc.getDouble("itemPrice");
//                        if (price == null) price = 0.0;
//                        item.setPrice(price);
//                        item.setDescription(doc.getString("itemDesc"));
//                        item.setImageUrl(doc.getString("itemImage"));
//                        item.setMeta(doc.getData());
//                        item.setDocPath(doc.getReference().getPath());
//                        orderList.add(item);
//                    }
//                    adapter.notifyDataSetChanged();
//
//                    if (orderList.isEmpty()) {
//                        recyclerView.setVisibility(View.GONE);
//                        layoutNoOrders.setVisibility(View.VISIBLE);
//                    } else {
//                        recyclerView.setVisibility(View.VISIBLE);
//                        layoutNoOrders.setVisibility(View.GONE);
//                    }
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
//
//    // ===================== Orders Adapter =====================
//    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
//        private final ArrayList<MenuItem> items;
//
//        OrdersAdapter(ArrayList<MenuItem> items) {
//            this.items = items;
//        }
//
//        @NonNull
//        @Override
//        public OrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.passanger_item_menu, parent, false);
//            return new OrdersAdapter.ViewHolder(v);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull OrdersAdapter.ViewHolder h, int pos) {
//            MenuItem m = items.get(pos);
//            h.name.setText(m.getName());
//            h.price.setText("Rs. " + m.getPrice());
//            h.desc.setText(m.getDescription());
//
//            // Image decode
//            if (m.getImageUrl() != null) {
//                try {
//                    byte[] dec = Base64.decode(m.getImageUrl(), Base64.DEFAULT);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(dec, 0, dec.length);
//                    if (bitmap != null) h.image.setImageBitmap(bitmap);
//                } catch (Exception ignored) {}
//            }
//            // Hide Add to Cart and Buy Now buttons if available
//            Button btnAddToCart = h.itemView.findViewById(R.id.btnAddCart);
//            Button btnBuyNow = h.itemView.findViewById(R.id.btnBuyNow);
//
//            if (btnAddToCart != null) btnAddToCart.setVisibility(View.GONE);
//            if (btnBuyNow != null) btnBuyNow.setVisibility(View.GONE);
//            if (h.btnDelivered != null) {
//                h.btnDelivered.setVisibility(View.VISIBLE);
//                h.btnDelivered.setText("Delivered To");
//                h.btnDelivered.setBackgroundTintList(
//                        h.itemView.getContext().getResources().getColorStateList(R.color.teal_500)
//                );
//                h.btnDelivered.setTextColor(
//                        h.itemView.getContext().getResources().getColor(android.R.color.white)
//                );
//
//                h.btnDelivered.setOnClickListener(v -> openDeliveryBoysDialog(m, pos));
//            }
//
//            if (h.txtBadge != null) {
//                h.txtBadge.setVisibility(View.VISIBLE);
//                h.txtBadge.setText("Accepted");
//                h.txtBadge.setBackgroundResource(R.drawable.bg_badge_green);
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return items.size();
//        }
//
//        class ViewHolder extends RecyclerView.ViewHolder {
//            TextView name, price, desc, txtBadge;
//            ImageView image;
//            Button btnDelivered;
//
//            ViewHolder(View v) {
//                super(v);
//                name = v.findViewById(R.id.txtName);
//                price = v.findViewById(R.id.txtPrice);
//                desc = v.findViewById(R.id.txtDesc);
//                image = v.findViewById(R.id.imgFood);
//                txtBadge = v.findViewById(R.id.txtStatusBadge);
//                btnDelivered = v.findViewById(R.id.btnDeliveredOrder);
//            }
//        }
//    }
//
//    // ===================== Delivery Dialog =====================
//    private void openDeliveryBoysDialog(MenuItem orderItem, int orderPosition) {
//        String restId = auth.getCurrentUser().getUid();
//
//        firestore.collection("Users")
//                .document("Restaurant")
//                .collection("VerifiedRegister")
//                .document(restId)
//                .get()
//                .addOnSuccessListener(doc -> {
//                    if (!doc.exists()) {
//                        Toast.makeText(getContext(), "Restaurant data not found", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    String city = doc.getString("city");
//                    if (city == null || city.isEmpty()) {
//                        Toast.makeText(getContext(), "City not found for restaurant", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    showDeliveryDialogForCity(orderItem, orderPosition, city);
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
//
//    private void showDeliveryDialogForCity(MenuItem orderItem, int orderPosition, String city) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("Choose Delivery Boy (" + city + ")");
//
//        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_resturent__delivery, null);
//        RecyclerView rv = dialogView.findViewById(R.id.recyclerDeliveryBoys);
//        rv.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        final ArrayList<DeliveryBoyInfo> deliveryList = new ArrayList<>();
//        final DeliveryAssignAdapter assignAdapter = new DeliveryAssignAdapter(deliveryList, orderItem, orderPosition);
//        rv.setAdapter(assignAdapter);
//
//        builder.setView(dialogView);
//        builder.setNegativeButton("Cancel", (d, which) -> d.dismiss());
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        firestore.collection("Users")
//                .document("Delivery")
//                .collection("VerifiedRegister")
//                .whereEqualTo("city", city)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    deliveryList.clear();
//                    for (QueryDocumentSnapshot doc : querySnapshot) {
//                        DeliveryBoyInfo info = new DeliveryBoyInfo();
//                        info.uid = doc.getId();
//                        info.name = doc.getString("name");
//                        info.phone = doc.getString("phone");
//                        info.email = doc.getString("email");
//                        info.onlineStatus = doc.getString("onlineStatus");
//                        deliveryList.add(info);
//                    }
//
//                    for (DeliveryBoyInfo b : deliveryList) {
//                        firestore.collection("Users")
//                                .document("Delivery")
//                                .collection("Register")
//                                .document(b.uid)
//                                .get()
//                                .addOnSuccessListener(imageDoc -> {
//                                    if (imageDoc.exists()) {
//                                        String base64 = imageDoc.getString("imageBase64");
//                                        b.imageBase64 = base64;
//                                        int idx = deliveryList.indexOf(b);
//                                        if (idx != -1) assignAdapter.notifyItemChanged(idx);
//                                    }
//                                });
//                    }
//
//                    assignAdapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(getContext(), "Error loading delivery boys: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
//
//    // ===================== Delivery Adapter =====================
//    private class DeliveryAssignAdapter extends RecyclerView.Adapter<DeliveryAssignAdapter.DBViewHolder> {
//        private final ArrayList<DeliveryBoyInfo> list;
//        private final MenuItem order;
//        private final int orderPos;
//
//        DeliveryAssignAdapter(ArrayList<DeliveryBoyInfo> list, MenuItem order, int orderPos) {
//            this.list = list;
//            this.order = order;
//            this.orderPos = orderPos;
//        }
//
//        @NonNull
//        @Override
//        public DBViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_boy, parent, false);
//            return new DBViewHolder(v);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull DBViewHolder h, int pos) {
//            DeliveryBoyInfo b = list.get(pos);
//
//            h.tvName.setText(b.name != null ? b.name : "No Name");
//            h.tvPhone.setText(b.phone != null ? b.phone : "No Phone");
//            h.tvEmail.setText(b.email != null ? b.email : "No Email");
//
//            // Online status
//            h.txtDeliveryOnlineStatus.setText(b.onlineStatus != null ? b.onlineStatus : "Offline");
//
//            if (b.imageBase64 != null) {
//                try {
//                    byte[] dec = Base64.decode(b.imageBase64, Base64.DEFAULT);
//                    Bitmap bmp = BitmapFactory.decodeByteArray(dec, 0, dec.length);
//                    if (bmp != null) h.imgDeliveryBoy.setImageBitmap(bmp);
//                } catch (Exception ignored) {}
//            }
//
//            h.btnDeliverOrder.setOnClickListener(v -> {
//                h.btnDeliverOrder.setEnabled(false);
//                assignOrderToDeliveryBoy(order, orderPos, b.uid, success -> {
//                    if (success) {
//                        Toast.makeText(v.getContext(), "Order assigned to " + b.name, Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(v.getContext(), "Failed to assign", Toast.LENGTH_SHORT).show();
//                        h.btnDeliverOrder.setEnabled(true);
//                    }
//                });
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return list.size();
//        }
//
//        class DBViewHolder extends RecyclerView.ViewHolder {
//            TextView tvName, tvPhone, tvEmail, txtDeliveryOnlineStatus;
//            ImageView imgDeliveryBoy;
//            Button btnDeliverOrder;
//
//            DBViewHolder(View v) {
//                super(v);
//                tvName = v.findViewById(R.id.tvName);
//                tvPhone = v.findViewById(R.id.tvPhone);
//                tvEmail = v.findViewById(R.id.tvEmail);
//                txtDeliveryOnlineStatus = new TextView(v.getContext());
//                btnDeliverOrder = v.findViewById(R.id.btnDeliverOrder);
//                imgDeliveryBoy = v.findViewById(R.id.imgDeliveryBoy);
//            }
//        }
//    }
//
//    // ===================== Assign Logic =====================
//    private void assignOrderToDeliveryBoy(MenuItem order, int orderPos, String deliveryUid, AssignmentCallback callback) {
//        if (order == null || order.getId() == null || order.getDocPath() == null) {
//            callback.onComplete(false);
//            return;
//        }
//
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("orderStatus", "Assigned");
//        updates.put("assignedTo", deliveryUid);
//        updates.put("assignedAt", FieldValue.serverTimestamp());
//
//        DocumentReference orderRef = firestore.document(order.getDocPath());
//        orderRef.update(updates)
//                .addOnSuccessListener(aVoid -> {
//                    Map<String, Object> orderData = new HashMap<>();
//                    if (order.getMeta() != null) orderData.putAll(order.getMeta());
//                    orderData.put("orderStatus", "New");
//                    orderData.put("assignedTo", deliveryUid);
//                    orderData.put("assignedFromRestaurant", auth.getCurrentUser().getUid());
//                    orderData.put("originalOrderId", order.getId());
//                    orderData.put("assignedAt", FieldValue.serverTimestamp());
//
//                    DocumentReference deliveryOrderRef = firestore.collection("Users")
//                            .document("Delivery")
//                            .collection("VerifiedRegister")
//                            .document(deliveryUid)
//                            .collection("Orders")
//                            .document(order.getId());
//
//                    deliveryOrderRef.set(orderData)
//                            .addOnSuccessListener(v -> {
//                                if (orderPos >= 0 && orderPos < orderList.size()) {
//                                    orderList.remove(orderPos);
//                                    adapter.notifyItemRemoved(orderPos);
//                                }
//                                callback.onComplete(true);
//                            })
//                            .addOnFailureListener(e -> callback.onComplete(false));
//                })
//                .addOnFailureListener(e -> callback.onComplete(false));
//    }
//
//    private interface AssignmentCallback {
//        void onComplete(boolean success);
//    }
//
//    private static class DeliveryBoyInfo {
//        String uid;
//        String name;
//        String phone;
//        String email;
//        String onlineStatus;
//        String imageBase64;
//    }
//}
//
//
//
//
//
//
