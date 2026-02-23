package com.example.paktrainfoodapp.ui.main.Delivery;

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
import com.example.paktrainfoodapp.ui.main.Restaurant.MenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class Order_Accept_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ArrayList<MenuItem> orderList;
    private DeliveryNewOrderAdapter adapter;
    private String deliveryBoyId;
    private final Map<String, String> restaurantCache = new HashMap<>(); // cache to avoid multiple lookups

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delivery_order_new_accept_complete, container, false);

        recyclerView = view.findViewById(R.id.recycler_delivery_Orders);
        layoutNoOrders = view.findViewById(R.id.layoutNo_delivery_Orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        orderList = new ArrayList<>();
        deliveryBoyId = auth.getCurrentUser().getUid();

        adapter = new DeliveryNewOrderAdapter(orderList);
        recyclerView.setAdapter(adapter);

        loadAssignedOrders();

        return view;
    }

    private void loadAssignedOrders() {
        CollectionReference ordersRef = db.collection("Users")
                .document("Delivery")
                .collection("VerifiedRegister")
                .document(deliveryBoyId)
                .collection("Orders");

        ordersRef.whereEqualTo("orderStatus", "Accepted")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        MenuItem order = new MenuItem();
                        order.setId(doc.getId());
                        order.setName(doc.getString("itemName"));
                        order.setPrice(doc.getDouble("itemPrice") != null ? doc.getDouble("itemPrice") : 0.0);
                        order.setDescription(doc.getString("itemDesc"));
                        order.setImageUrl(doc.getString("itemImage"));
                        order.setDocPath(doc.getReference().getPath());

                        String restaurantId = doc.getString("assignedFromRestaurant");

                        if (restaurantId != null && !restaurantId.isEmpty()) {
                            if (restaurantCache.containsKey(restaurantId)) {
                                order.setRestaurantName(restaurantCache.get(restaurantId));
                            } else {
                                // fetch restaurant name once and cache it
                                db.collection("Users")
                                        .document("Restaurant")
                                        .collection("VerifiedRegister")
                                        .document(restaurantId)
                                        .get()
                                        .addOnSuccessListener(restaurantDoc -> {
                                            String restName = restaurantDoc.exists() ?
                                                    restaurantDoc.getString("restaurantName") : "Unknown Restaurant";
                                            restaurantCache.put(restaurantId, restName);
                                            order.setRestaurantName(restName);
                                            adapter.notifyDataSetChanged();
                                        })
                                        .addOnFailureListener(e -> order.setRestaurantName("Unknown Restaurant"));
                            }
                        } else {
                            order.setRestaurantName("Unknown Restaurant");
                        }

                        orderList.add(order);
                    }

                    if (orderList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        layoutNoOrders.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        layoutNoOrders.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ========================= ADAPTER =========================
    private class DeliveryNewOrderAdapter extends RecyclerView.Adapter<DeliveryNewOrderAdapter.ViewHolder> {

        private final ArrayList<MenuItem> orders;

        DeliveryNewOrderAdapter(ArrayList<MenuItem> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivery_boy1, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            MenuItem order = orders.get(pos);

            h.tvName.setText(order.getName() != null ? order.getName() : "Unnamed Item");
            h.tvPhone.setText("From Restaurant: " + (order.getRestaurantName() != null ? order.getRestaurantName() : "Unknown"));
            h.tvEmail.setText(order.getDescription() != null ? order.getDescription() : "No description");

            // Load image
            if (order.getImageUrl() != null && !order.getImageUrl().isEmpty()) {
                try {
                    byte[] dec = Base64.decode(order.getImageUrl(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    if (bitmap != null) h.imgDeliveryBoy.setImageBitmap(bitmap);
                    else h.imgDeliveryBoy.setImageResource(R.drawable.ic_food_placeholder);
                } catch (Exception ignored) {
                    h.imgDeliveryBoy.setImageResource(R.drawable.ic_food_placeholder);
                }
            } else {
                h.imgDeliveryBoy.setImageResource(R.drawable.ic_food_placeholder);
            }

            // 🔹 Hide Accept & Cancel Buttons
            h.btnAccept.setVisibility(View.GONE);
            h.btnCancel.setVisibility(View.GONE);

            // 🔹 Show Live Tracking Button
            h.btnLiveTracking.setVisibility(View.VISIBLE);
            h.btnLiveTracking.setOnClickListener(v -> {
                String passengerUid = "tZtn3qs6NXd7hrnmmGcWxlGnZJ72";
                if(passengerUid != null && !passengerUid.isEmpty()) {
                    LiveTrackingFragment fragment = new LiveTrackingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("passengerUid", passengerUid);
                    fragment.setArguments(bundle);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_container, fragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), "Passenger UID missing!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPhone, tvEmail;
            ImageView imgDeliveryBoy;
            Button btnLiveTracking, btnAccept, btnCancel;

            ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvName);
                tvPhone = v.findViewById(R.id.tvPhone);
                tvEmail = v.findViewById(R.id.tvEmail);
                imgDeliveryBoy = v.findViewById(R.id.imgDeliveryBoy);

                // Buttons
                btnLiveTracking = v.findViewById(R.id.btnLiveTracking);
                btnAccept = v.findViewById(R.id.btnDeliverOrder);
                btnCancel = v.findViewById(R.id.btnCancel);
            }
        }
    }
}
