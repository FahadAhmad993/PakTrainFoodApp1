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

import java.util.ArrayList;

public class ActiveOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;
    private ArrayList<MenuItem> orderList;
    private OrdersAdapter adapter;
    private FirebaseFirestore firestore;
    private String restaurantUid;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

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

        if (!restaurantUid.isEmpty()) {
            loadOrders();
        } else {
            recyclerView.setVisibility(View.GONE);
            layoutNoOrders.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Login required", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    // ================= LOAD ACTIVE ORDERS =================

    private void loadOrders() {
        firestore.collectionGroup("Orders")
                .whereEqualTo("restaurantUid", restaurantUid)
                .whereEqualTo("orderStatus", "Active")
                .get()
                .addOnSuccessListener(query -> {

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        MenuItem item = new MenuItem();
                        item.setId(doc.getId());                 // ✅ orderId
                        item.setName(doc.getString("itemName"));
                        item.setPrice(doc.getDouble("itemPrice") != null
                                ? doc.getDouble("itemPrice") : 0);
                        item.setDescription(doc.getString("itemDesc"));
                        item.setImageUrl(doc.getString("itemImage"));
                        item.setPassengerUid(doc.getString("passengerUid")); // ✅ passengerUid

                        orderList.add(item);
                    }

                    adapter.notifyDataSetChanged();

                    layoutNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    recyclerView.setVisibility(View.GONE);
                    layoutNoOrders.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(),
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.passanger_item_menu, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder h,
                int position
        ) {

            MenuItem m = items.get(position);

            h.name.setText(m.getName());
            h.price.setText("Rs. " + m.getPrice());
            h.desc.setText(m.getDescription());

            // IMAGE
            if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
                try {
                    byte[] data = Base64.decode(m.getImageUrl(), Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    h.image.setImageBitmap(bmp);
                } catch (Exception e) {
                    h.image.setImageResource(R.drawable.ic_food_placeholder);
                }
            } else {
                h.image.setImageResource(R.drawable.ic_food_placeholder);
            }

            // BUTTON VISIBILITY
            h.btnAddToCart.setVisibility(View.GONE);
            h.btnDelete.setVisibility(View.GONE);
            h.btnBuyNow.setVisibility(View.GONE);

            h.btnAccept.setVisibility(View.VISIBLE);
            h.btnCancel.setVisibility(View.VISIBLE);

            // ACCEPT
            h.btnAccept.setOnClickListener(v ->
                    updateOrderStatus(m, "Accepted", h));

            // CANCEL
            h.btnCancel.setOnClickListener(v ->
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Cancel Order")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Yes",
                                    (d, w) -> updateOrderStatus(m, "Cancelled", h))
                            .setNegativeButton("No", null)
                            .show()
            );

            // ================= OPEN DETAIL FRAGMENT (CORRECT WAY) =================
            h.itemView.setOnClickListener(v -> {

                Fragment detailFragment = OrderDetailFragment.newInstance(
                        m.getId(),           // ✅ orderId
                        m.getPassengerUid()  // ✅ passengerUid
                );

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, detailFragment)
                        .addToBackStack("order_detail")
                        .commit();
            });
        }

        private void updateOrderStatus(
                MenuItem m,
                String status,
                ViewHolder h
        ) {

            firestore.collection("Users")
                    .document("Passenger")
                    .collection("OrderNow")
                    .document(m.getPassengerUid())
                    .collection("Orders")
                    .document(m.getId())
                    .update("orderStatus", status)
                    .addOnSuccessListener(a -> {

                        Toast.makeText(getContext(),
                                "Order " + status,
                                Toast.LENGTH_SHORT).show();

                        int pos = h.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            items.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Update failed",
                                    Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // ================= VIEW HOLDER =================

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView name, price, desc;
            ImageView image;
            Button btnAddToCart, btnDelete, btnBuyNow, btnAccept, btnCancel;

            ViewHolder(@NonNull View v) {
                super(v);

                name = v.findViewById(R.id.txtName);
                price = v.findViewById(R.id.txtPrice);
                desc = v.findViewById(R.id.txtDesc);
                image = v.findViewById(R.id.imgFood);

                btnAddToCart = v.findViewById(R.id.btnAddCart);
                btnDelete = v.findViewById(R.id.btnDeleteOrder);
                btnBuyNow = v.findViewById(R.id.btnBuyNow);
                btnAccept = v.findViewById(R.id.btnAcceptOrder);
                btnCancel = v.findViewById(R.id.btnCancelOrder);
            }
        }
    }
}





