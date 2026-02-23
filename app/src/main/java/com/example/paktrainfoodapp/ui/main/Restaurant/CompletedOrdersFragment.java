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

public class CompletedOrdersFragment extends Fragment {

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

        loadOrders("Completed");
        return view;
    }

    private void loadOrders(String status) {
        firestore.collection("Users")
                .document("Restaurant")
                .collection("Orders")
                .whereEqualTo("orderStatus", status)
                .get()
                .addOnSuccessListener(query -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        MenuItem item = new MenuItem();
                        item.setId(doc.getId());
                        item.setName(doc.getString("itemName"));
                        item.setPrice(doc.getDouble("itemPrice"));
                        item.setDescription(doc.getString("itemDesc"));
                        item.setImageUrl(doc.getString("itemImage"));
                        orderList.add(item);
                    }
                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        layoutNoOrders.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        layoutNoOrders.setVisibility(View.GONE);
                    }
                });
    }

    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
        private final ArrayList<MenuItem> items;

        OrdersAdapter(ArrayList<MenuItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.passanger_item_menu, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            MenuItem m = items.get(pos);
            h.name.setText(m.getName());
            h.price.setText("Rs. " + m.getPrice());
            h.desc.setText(m.getDescription());

            if (m.getImageUrl() != null) {
                byte[] dec = Base64.decode(m.getImageUrl(), Base64.DEFAULT);
                h.image.setImageBitmap(BitmapFactory.decodeByteArray(dec, 0, dec.length));
            }

            h.btnAccept.setOnClickListener(v -> {
                FirebaseFirestore.getInstance().collection("Users")
                        .document("Restaurant").collection("Orders")
                        .document(m.getId())
                        .update("orderStatus", "Accepted");
                Toast.makeText(v.getContext(), "Order Accepted", Toast.LENGTH_SHORT).show();
                items.remove(pos);
                notifyItemRemoved(pos);
            });
        }

        @Override public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, price, desc;
            ImageView image;
            Button btnAccept;
            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.txtName);
                price = v.findViewById(R.id.txtPrice);
                desc = v.findViewById(R.id.txtDesc);
                image = v.findViewById(R.id.imgFood);
                btnAccept = v.findViewById(R.id.btnAcceptOrder);
            }
        }
    }
}
