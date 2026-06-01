package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import android.widget.LinearLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class passanger_orderDetailFragment extends Fragment {

    private TextView txtOrderId,txtTotalPrice;
    private RecyclerView recyclerView;

    private FirebaseFirestore firestore;
    private String orderId;

    private OrderItemsAdapter adapter;
    private List<MenuitemModel> itemList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_passanger_order_detail, container, false);

        txtOrderId = view.findViewById(R.id.txtOrderId);
        recyclerView = view.findViewById(R.id.recyclerView);
        txtTotalPrice = view.findViewById(R.id.txtTotalPrice);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();

        // Bottom Navigation Hide
        LinearLayout bottomNav =
                requireActivity().findViewById(R.id.bottom_nav);

        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        // Get Order ID
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }

        // Handle Hardware Back Button
        handleBackPress();

        // Load Data
        loadOrderDetails();

        return view;
    }

    // Hardware Back Button
    private void handleBackPress() {

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
            }
        };

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), callback);
    }

    // Load Order Details
    private void loadOrderDetails() {

        firestore.collection("Orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    txtOrderId.setText("Order ID: " + orderId);
                    Double totalPrice = doc.getDouble("totalPrice");

                    if (totalPrice != null) {
                        txtTotalPrice.setText("Total Price: Rs " + totalPrice);
                    } else {
                        txtTotalPrice.setText("Total Price: Rs 0");
                    }

                    List<Map<String, Object>> cartItems =
                            (List<Map<String, Object>>) doc.get("cartItems");

                    itemList.clear();

                    if (cartItems != null) {

                        for (Map<String, Object> m : cartItems) {

                            MenuitemModel item = new MenuitemModel();

                            item.setName(String.valueOf(m.get("name")));
                            item.setDescription(String.valueOf(m.get("description")));
                            item.setRestaurantName(String.valueOf(m.get("restaurantName")));
                            item.setImageUrl(String.valueOf(m.get("imageUrl")));

                            Object priceObj = m.get("price");

                            if (priceObj != null) {
                                item.setPrice(Double.parseDouble(priceObj.toString()));
                            } else {
                                item.setPrice(0);
                            }

// Quantity
                            Object quantityObj = m.get("quantity");

                            if (quantityObj != null) {
                                item.setQuantity(Integer.parseInt(quantityObj.toString()));
                            } else {
                                item.setQuantity(1);
                            }

                            itemList.add(item);
                        }
                    }

                    adapter = new OrderItemsAdapter(itemList);
                    recyclerView.setAdapter(adapter);

                })
                .addOnFailureListener(e ->
                        txtOrderId.setText("Failed to load order"));
    }

    // Show Bottom Navigation Again
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        LinearLayout bottomNav =
                requireActivity().findViewById(R.id.bottom_nav);

        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }
}




