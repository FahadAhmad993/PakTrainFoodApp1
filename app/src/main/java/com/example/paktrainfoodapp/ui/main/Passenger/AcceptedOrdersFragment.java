package com.example.paktrainfoodapp.ui.main.Passenger;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AcceptedOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutNoOrders;
    private ArrayList<MenuitemModel> orderList;
    private AcceptedOrdersAdapter adapter;
    private FirebaseFirestore firestore;
    private String uid;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_passanger_orders_accept_pending_complete, container, false);

        recyclerView = view.findViewById(R.id.recyclerOrders);
        layoutNoOrders = view.findViewById(R.id.layoutNoOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        adapter = new AcceptedOrdersAdapter(orderList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            uid = null;
        }

        if (uid == null) {
            recyclerView.setVisibility(View.GONE);
            layoutNoOrders.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadAcceptedOrders();
        return view;
    }

    private void loadAcceptedOrders() {
        firestore.collection("Users")
                .document("Passenger")
                .collection("OrderNow")
                .document(uid)
                .collection("Orders")
                .whereEqualTo("orderStatus", "Accepted")
                .addSnapshotListener((snap, e) -> {

                    if (e != null) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    orderList.clear();

                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            MenuitemModel item = new MenuitemModel();
                            item.setId(doc.getId());
                            item.setName(doc.getString("itemName"));
                            item.setDescription(doc.getString("itemDesc"));
                            item.setImageUrl(doc.getString("itemImage"));
                            item.setPrice(doc.getDouble("itemPrice") != null ? doc.getDouble("itemPrice") : 0);
                            orderList.add(item);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        layoutNoOrders.setVisibility(View.VISIBLE);
                    } else {
                        layoutNoOrders.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    //  Adapter
    private static class AcceptedOrdersAdapter extends RecyclerView.Adapter<AcceptedOrdersAdapter.VH> {

        private final ArrayList<MenuitemModel> items;

        AcceptedOrdersAdapter(ArrayList<MenuitemModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.passanger_item_menu, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {

            MenuitemModel m = items.get(position);

            holder.txtName.setText(m.getName());
            holder.txtPrice.setText("Rs. " + m.getPrice());
            holder.txtDesc.setText(m.getDescription());

            // Decode image safely
            if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(m.getImageUrl(), Base64.DEFAULT);
                    holder.imgFood.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
                } catch (Exception ignored) {}
            }

            // Hide unnecessary buttons
            holder.btnAddCart.setVisibility(View.GONE);
            holder.btnBuyNow.setVisibility(View.GONE);
            holder.btnDeleteOrder.setVisibility(View.GONE);
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);

            // Show Accepted badge
            holder.txtBadge.setVisibility(View.VISIBLE);
            holder.txtBadge.setText("Accepted");
            holder.txtBadge.setBackgroundResource(R.drawable.bg_badge_green);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {

            ImageView imgFood;
            TextView txtName, txtPrice, txtDesc, txtBadge;
            Button btnAddCart, btnBuyNow, btnDeleteOrder, btnAccept, btnCancel;

            VH(View v) {
                super(v);
                imgFood = v.findViewById(R.id.imgFood);
                txtName = v.findViewById(R.id.txtName);
                txtPrice = v.findViewById(R.id.txtPrice);
                txtDesc = v.findViewById(R.id.txtDesc);
                txtBadge = v.findViewById(R.id.txtStatusBadge);

                btnAddCart = v.findViewById(R.id.btnAddCart);
                btnBuyNow = v.findViewById(R.id.btnBuyNow);
                btnDeleteOrder = v.findViewById(R.id.btnDeleteOrder);
                btnAccept = v.findViewById(R.id.btnAcceptOrder);
                btnCancel = v.findViewById(R.id.btnCancelOrder);
            }
        }
    }
}




