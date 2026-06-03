package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class DeliveredOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;
    private ArrayList<MenuItem> orderList;
    private OrdersAdapter adapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             android.os.Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_restaurant_orders_accept_pending_complete, container, false);

        recyclerView = view.findViewById(R.id.recyclerOrders);
        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadDeliveredOrders();
        return view;
    }

    private void loadDeliveredOrders() {
        if (auth.getCurrentUser() == null) return;
        String restId = auth.getCurrentUser().getUid();

        firestore.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(restId)
                .collection("Orders")
                .whereIn("orderStatus", Arrays.asList("Assigned", "Delivered"))
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null || !isAdded()) return;

                    orderList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        MenuItem item = new MenuItem();
                        item.setId(doc.getId());
                        item.setName(doc.getString("itemName"));
                        item.setDescription(doc.getString("itemDesc"));
                        item.setImageUrl(doc.getString("itemImage"));
                        item.setMeta(doc.getData());
                        item.setDocPath(doc.getReference().getPath());
                        item.setOrderStatus(doc.getString("orderStatus"));

                        // ✅ FIX: Price handling via Variations Map
                        Double price = doc.getDouble("itemPrice");
                        if (price != null) {
                            Map<String, Double> varMap = new HashMap<>();
                            varMap.put("Default", price);
                            item.setVariations(varMap);
                        }
                        orderList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                    updateLayout();
                });
    }

    private void updateLayout() {
        if (orderList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutNoOrders.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutNoOrders.setVisibility(View.GONE);
        }
    }

    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
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

            // ✅ FIX: Price display from Map
            if (m.getVariations() != null && !m.getVariations().isEmpty()) {
                h.price.setText("Rs. " + m.getVariations().values().iterator().next());
            } else {
                h.price.setText("Rs. 0");
            }

            h.desc.setText(m.getDescription());

            if (m.getImageUrl() != null) {
                try {
                    byte[] dec = Base64.decode(m.getImageUrl(), Base64.DEFAULT);
                    h.image.setImageBitmap(BitmapFactory.decodeByteArray(dec, 0, dec.length));
                } catch (Exception ignored) {}
            }

            Button btnAddToCart = h.itemView.findViewById(R.id.btnAddCart);
            Button btnBuyNow = h.itemView.findViewById(R.id.btnBuyNow);
            if (btnAddToCart != null) btnAddToCart.setVisibility(View.GONE);
            if (btnBuyNow != null) btnBuyNow.setVisibility(View.GONE);

            if (h.txtBadge != null) {
                h.txtBadge.setVisibility(View.VISIBLE);
                if ("Assigned".equalsIgnoreCase(m.getOrderStatus())) {
                    h.txtBadge.setText("Assigned to Delivery");
                    h.txtBadge.setBackgroundResource(R.drawable.selected_circle_bg);
                } else {
                    h.txtBadge.setText("Delivered");
                    h.txtBadge.setBackgroundResource(R.drawable.bg_badge_green);
                }
            }

            if (h.btnDelivered != null) {
                if ("Assigned".equalsIgnoreCase(m.getOrderStatus())) {
                    h.btnDelivered.setVisibility(View.VISIBLE);
                    h.btnDelivered.setText("Mark as Delivered");
                    h.btnDelivered.setOnClickListener(v -> {
                        FirebaseFirestore.getInstance().document(m.getDocPath())
                                .update("orderStatus", "Delivered")
                                .addOnSuccessListener(a -> Toast.makeText(v.getContext(), "Updated", Toast.LENGTH_SHORT).show());
                    });
                } else {
                    h.btnDelivered.setVisibility(View.GONE);
                }
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, price, desc, txtBadge;
            ImageView image;
            Button btnDelivered;

            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.txtName);
                price = v.findViewById(R.id.txtPrice);
                desc = v.findViewById(R.id.txtDesc);
                image = v.findViewById(R.id.imgFood);
                txtBadge = v.findViewById(R.id.txtStatusBadge);
                btnDelivered = v.findViewById(R.id.btnDeliveredOrder);
            }
        }
    }
}