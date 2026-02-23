package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.paktrainfoodapp.R;
import com.google.android.material.tabs.TabLayout;

public class returent_OrdersFragment extends Fragment {

    private TabLayout tabsOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_returent__orders, container, false);

        tabsOrders = view.findViewById(R.id.tabsOrders);

        // 🔹 Create Tabs
        tabsOrders.addTab(tabsOrders.newTab().setText("Active"));
        tabsOrders.addTab(tabsOrders.newTab().setText("Accepted"));
        tabsOrders.addTab(tabsOrders.newTab().setText("Delivered"));
        tabsOrders.addTab(tabsOrders.newTab().setText("Completed"));

        // 🔹 Default Load: Active Orders
        replaceChildFragment(new ActiveOrdersFragment());

        tabsOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selected = null;
                switch (tab.getPosition()) {
                    case 0:
                        selected = new ActiveOrdersFragment();
                        break;
                    case 1:
                        selected = new AcceptedOrdersFragment();
                        break;
                    case 2:
                        selected = new DeliveredOrdersFragment();
                        break;
                    case 3:
                        selected = new CompletedOrdersFragment();
                        break;
                }
                replaceChildFragment(selected);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    private void replaceChildFragment(Fragment fragment) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.orders_tab_container, fragment);
        ft.commit();
    }
}





//package com.example.paktrainfoodapp.ui.main.Restaurant;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.paktrainfoodapp.R;
//
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.tabs.TabLayout;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.util.ArrayList;
//
//public class returent_OrdersFragment extends Fragment {
//
//    private MaterialToolbar toolbarOrders;
//    private TabLayout tabsOrders;
//    private RecyclerView recyclerView;
//    private TextView tvEmptyState;
//
//    private OrdersAdapter adapter;
//    private ArrayList<MenuItem> orderList;
//    private FirebaseFirestore firestore;
//    private FirebaseAuth auth;
//    private String uid;
//    private String currentStatus = "Active";
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_returent__orders, container, false);
//
//        // 🔹 UI initialization
//        toolbarOrders = view.findViewById(R.id.toolbarOrders);
//        tabsOrders = view.findViewById(R.id.tabsOrders);
//        recyclerView = view.findViewById(R.id.recyclerOrders);
//        tvEmptyState = view.findViewById(R.id.tvEmptyState);
//
//        // 🔹 Toolbar setup
//        toolbarOrders.setTitle("Order Section");
//        toolbarOrders.setNavigationOnClickListener(v -> {
//            if (getActivity() != null) getActivity().onBackPressed();
//        });
//
//        // 🔹 Recycler setup
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        orderList = new ArrayList<>();
//        adapter = new OrdersAdapter(orderList);
//        recyclerView.setAdapter(adapter);
//
//        // 🔹 Firebase setup
//        firestore = FirebaseFirestore.getInstance();
//        auth = FirebaseAuth.getInstance();
//        if (auth.getCurrentUser() != null) {
//            uid = auth.getCurrentUser().getUid();
//        }
//
//        // 🔹 Tabs
//        tabsOrders.addTab(tabsOrders.newTab().setText("Active"));
//        tabsOrders.addTab(tabsOrders.newTab().setText("Accepted"));
//        tabsOrders.addTab(tabsOrders.newTab().setText("Delivered"));
//        tabsOrders.addTab(tabsOrders.newTab().setText("Completed"));
//
//        // Load first tab
//        loadOrders(currentStatus);
//
//        tabsOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                currentStatus = tab.getText().toString();
//                loadOrders(currentStatus);
//            }
//
//            @Override public void onTabUnselected(TabLayout.Tab tab) {}
//            @Override public void onTabReselected(TabLayout.Tab tab) {}
//        });
//
//        return view;
//    }
//
//    // 🔹 Show message when no data
//    private void showEmptyState(boolean show, String message) {
//        if (show) {
//            tvEmptyState.setText(message);
//            tvEmptyState.setVisibility(View.VISIBLE);
//            recyclerView.setVisibility(View.GONE);
//        } else {
//            tvEmptyState.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    // 🔹 Load orders from Firestore by status
//    private void loadOrders(String status) {
//        orderList.clear();
//        showEmptyState(false, "");
//
//        firestore.collection("Users")
//                .document("Restaurant")
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
//                        item.setPrice(price != null ? price : 0);
//                        item.setDescription(doc.getString("itemDesc"));
//                        item.setRestaurantName(doc.getString("restaurantName"));
//                        item.setImageUrl(doc.getString("itemImage"));
//                        item.setOrderStatus(doc.getString("orderStatus"));
//                        orderList.add(item);
//                    }
//                    adapter.notifyDataSetChanged();
//
//                    if (orderList.isEmpty()) {
//                        String msg;
//                        switch (status.toLowerCase()) {
//                            case "active": msg = "There is no active order now."; break;
//                            case "accepted": msg = "There is no accepted order now."; break;
//                            case "delivered": msg = "There is no delivered order now."; break;
//                            case "completed": msg = "There is no completed order now."; break;
//                            default: msg = "No orders found."; break;
//                        }
//                        showEmptyState(true, msg);
//                    } else {
//                        showEmptyState(false, "");
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(getContext(), "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    showEmptyState(true, "Failed to load orders.");
//                });
//    }
//
//    // 🔹 RecyclerView Adapter
//    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
//        private final ArrayList<MenuItem> items;
//
//        OrdersAdapter(ArrayList<MenuItem> items) {
//            this.items = items;
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
//            MenuItem item = items.get(position);
//
//            holder.txtName.setText(item.getName());
//            holder.txtPrice.setText("Rs. " + item.getPrice());
//            holder.txtDesc.setText(item.getDescription());
//
//            // 🔹 Decode Base64 Image
//            String img = item.getImageUrl();
//            if (img != null && !img.isEmpty()) {
//                try {
//                    byte[] decoded = Base64.decode(img, Base64.DEFAULT);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
//                    holder.imgFood.setImageBitmap(bitmap);
//                } catch (Exception e) {
//                    holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
//                }
//            } else {
//                holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
//            }
//
//            // Hide edit/delete buttons
//            holder.btnEdit.setVisibility(View.GONE);
//            holder.btnDelete.setVisibility(View.GONE);
//
//            // Accept Button
//            holder.btnAccept.setOnClickListener(v -> {
//                FirebaseFirestore.getInstance()
//                        .collection("Users")
//                        .document("Restaurant")
//                        .collection("Orders")
//                        .document(item.getId())
//                        .update("orderStatus", "Accepted")
//                        .addOnSuccessListener(aVoid -> {
//                            Toast.makeText(v.getContext(), "Order Accepted", Toast.LENGTH_SHORT).show();
//                            items.remove(position);
//                            notifyItemRemoved(position);
//                        })
//                        .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//            });
//
//            // Cancel Button
//            holder.btnCancel.setOnClickListener(v -> {
//                FirebaseFirestore.getInstance()
//                        .collection("Users")
//                        .document("Restaurant")
//                        .collection("Orders")
//                        .document(item.getId())
//                        .update("orderStatus", "Cancelled")
//                        .addOnSuccessListener(aVoid -> {
//                            Toast.makeText(v.getContext(), "Order Cancelled", Toast.LENGTH_SHORT).show();
//                            items.remove(position);
//                            notifyItemRemoved(position);
//                        })
//                        .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return items.size();
//        }
//
//        // 🔹 ViewHolder
//        static class OrderViewHolder extends RecyclerView.ViewHolder {
//            ImageView imgFood;
//            TextView txtName, txtPrice, txtDesc;
//            ImageButton btnEdit, btnDelete;
//            Button btnAccept, btnCancel;
//
//            OrderViewHolder(@NonNull View itemView) {
//                super(itemView);
//                imgFood = itemView.findViewById(R.id.item_image);
//                txtName = itemView.findViewById(R.id.item_name);
//                txtPrice = itemView.findViewById(R.id.item_price);
//                txtDesc = itemView.findViewById(R.id.item_desc);
//                btnEdit = itemView.findViewById(R.id.btn_edit);
//                btnDelete = itemView.findViewById(R.id.btn_delete);
//
//                btnAccept = itemView.findViewById(R.id.btnAcceptOrder);
//                btnCancel = itemView.findViewById(R.id.btnCancelOrder);
//            }
//        }
//    }
//}
//
//
//
