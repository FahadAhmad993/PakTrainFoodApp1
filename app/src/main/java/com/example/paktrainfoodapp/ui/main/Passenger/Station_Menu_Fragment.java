//package com.example.paktrainfoodapp.ui.main.Passenger;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.ImageButton;
//import android.widget.ProgressBar;
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
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Station_Menu_Fragment extends Fragment {
//
//    private ImageView imgRestaurant;
//    private TextView txtRestaurantName, txtRestaurantInfo;
//    private ImageButton btnBack;
//    private RecyclerView recyclerView;
//    private ProgressBar progressBar;
//
//    private final List<MenuitemModel> itemList = new ArrayList<>();
//    private MenuAdapter adapter;
//    private FirebaseFirestore db;
//
//    private String restaurantId = "";
//    private String restaurantName = "";
//    private String restaurantImageBase64 = "";
//    private String restaurantInfo = "";
//
//    public Station_Menu_Fragment() {}
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_passanger_station__menu_, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//
//        // 🔹 Bind views
//        imgRestaurant = view.findViewById(R.id.img_restaurant);
//        txtRestaurantName = view.findViewById(R.id.tv_restaurant_name);
//        txtRestaurantInfo = view.findViewById(R.id.tv_restaurant_info);
//        btnBack = view.findViewById(R.id.btnBack);
//        recyclerView = view.findViewById(R.id.recyclerMenu);
//        progressBar = view.findViewById(R.id.progressBar);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        adapter = new MenuAdapter(itemList);
//        recyclerView.setAdapter(adapter);
//
//        db = FirebaseFirestore.getInstance();
//
//        // 🔹 Get restaurant data from bundle
//        if (getArguments() != null) {
//            restaurantId = getArguments().getString("RESTAURANT_UID", "");
//            restaurantName = getArguments().getString("RESTAURANT_NAME", "");
//            restaurantImageBase64 = getArguments().getString("RESTAURANT_IMAGE", "");
//            restaurantInfo = getArguments().getString("RESTAURANT_INFO", "Fast Food • Pakistani");
//        }
//
//        // 🔹 Show restaurant details
//        txtRestaurantName.setText(restaurantName);
//        txtRestaurantInfo.setText(restaurantInfo);
//
//        if (!restaurantImageBase64.isEmpty()) {
//            byte[] decodedBytes = Base64.decode(restaurantImageBase64, Base64.DEFAULT);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//            imgRestaurant.setImageBitmap(bitmap);
//        } else {
//            imgRestaurant.setImageResource(R.drawable.station3); // default image
//        }
//
//        // 🔹 Back button
//        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
//
//        // 🔹 Load menu items
//        loadMenuItemsByRestaurantId(restaurantId);
//
//        // 🔹 Menu item click events
//        adapter.setOnMenuItemClickListener(new MenuAdapter.OnMenuItemClickListener() {
//            @Override
//            public void onAddToCart(MenuitemModel item) {
//                Toast.makeText(requireContext(), item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onDeleteOrder(MenuitemModel item) {}
//
//            @Override
//            public void onBuyNow(MenuitemModel item) {
//                Passanger_order_more_Fragment dialog = Passanger_order_more_Fragment.newInstance(
//                        item.getName(),                       // Food Name
//                        String.valueOf(item.getPrice()),      // Price
//                        item.getDescription(),                // Description
//                        item.getImageUrl(),                   // Image Base64
//                        restaurantName,                       // Restaurant Name
//                        restaurantId,                         // Restaurant UID
//                        ""                                    // City (agar chahiye to pass karen)
//                );
//                dialog.show(getParentFragmentManager(), "OrderMoreDialog");
//            }
//        });
//    }
//
//    // 🔹 Load menu items
//    private void loadMenuItemsByRestaurantId(String restaurantId) {
//        progressBar.setVisibility(View.VISIBLE);
//        itemList.clear();
//
//        db.collection("Users")
//                .document("Restaurant")
//                .collection("VerifiedRegister")
//                .document(restaurantId)
//                .collection("MenuItems")
//                .get()
//                .addOnSuccessListener(menuItems -> {
//                    for (QueryDocumentSnapshot doc : menuItems) {
//                        MenuitemModel item = doc.toObject(MenuitemModel.class);
//                        item.setId(doc.getId());
//                        item.setRestaurantId(restaurantId);
//                        item.setRestaurantName(restaurantName);
//                        itemList.add(item);
//                    }
//
//                    adapter.notifyDataSetChanged();
//                    progressBar.setVisibility(View.GONE);
//
//                    if (itemList.isEmpty()) {
//                        Toast.makeText(requireContext(), "No menu items available", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    progressBar.setVisibility(View.GONE);
//                    Toast.makeText(requireContext(), "Failed to load menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                });
//    }
//}





package com.example.paktrainfoodapp.ui.main.Passenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BaseBundle;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Station_Menu_Fragment extends Fragment {

    private ImageView imgRestaurant;
    private TextView txtRestaurantName, txtRestaurantInfo;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private final List<MenuitemModel> itemList = new ArrayList<>();
    private MenuAdapter adapter;
    private FirebaseFirestore db;

    private String restaurantId = "";
    private String restaurantName = "";
    private String restaurantImageBase64 = "";
    private String restaurantInfo = "";
    private  String MealStation = "";

    public Station_Menu_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passanger_station__menu_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // 🔹 Bind views
        imgRestaurant = view.findViewById(R.id.img_restaurant);
        txtRestaurantName = view.findViewById(R.id.tv_restaurant_name);
        txtRestaurantInfo = view.findViewById(R.id.tv_restaurant_info);
        btnBack = view.findViewById(R.id.btnBack);
        recyclerView = view.findViewById(R.id.recyclerMenu);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MenuAdapter(itemList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 🔹 Get restaurant data from bundle
        if (getArguments() != null) {
            restaurantId = getArguments().getString("RESTAURANT_UID", "");
            restaurantName = getArguments().getString("RESTAURANT_NAME", "");
            restaurantImageBase64 = getArguments().getString("RESTAURANT_IMAGE", "");
            restaurantInfo = getArguments().getString("RESTAURANT_INFO", "Fast Food • Pakistani");
            MealStation = getArguments().getString("MEAL_STATION", "");
        }

        // 🔹 Show restaurant details
        txtRestaurantName.setText(restaurantName);
        txtRestaurantInfo.setText(restaurantInfo);

        if (!restaurantImageBase64.isEmpty()) {
            byte[] decodedBytes = android.util.Base64.decode(restaurantImageBase64, android.util.Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imgRestaurant.setImageBitmap(bitmap);
        } else {
            imgRestaurant.setImageResource(R.drawable.station3); // default image
        }

        // 🔹 Back button
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // 🔹 Load menu items
        loadMenuItemsByRestaurantId(restaurantId);

        // 🔹 Menu item click events
        adapter.setOnMenuItemClickListener(new MenuAdapter.OnMenuItemClickListener() {
            @Override
            public void onAddToCart(MenuitemModel item) {
                Toast.makeText(requireContext(), item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteOrder(MenuitemModel item) {}

            @Override
            public void onBuyNow(MenuitemModel item) {
//                OrderNowFragment dialog = OrderNowFragment.newInstance(
                OrderNowFragment dialog = OrderNowFragment.newInstance(
                        item.getName(),
                        item.getPrice(),
                        item.getDescription(),
                        restaurantName,
                        restaurantId,
                        item.getImageUrl(),
                        MealStation

                );
                dialog.show(getParentFragmentManager(), "OrderNowDialog");
            }
        });
    }

    // 🔹 Load menu items
    private void loadMenuItemsByRestaurantId(String restaurantId) {
        progressBar.setVisibility(View.VISIBLE);
        itemList.clear();

        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(restaurantId)
                .collection("MenuItems")
                .get()
                .addOnSuccessListener(menuItems -> {
                    for (QueryDocumentSnapshot doc : menuItems) {
                        MenuitemModel item = doc.toObject(MenuitemModel.class);
                        item.setId(doc.getId());
                        item.setRestaurantId(restaurantId);
                        item.setRestaurantName(restaurantName);
                        itemList.add(item);
                    }

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (itemList.isEmpty()) {
                        Toast.makeText(requireContext(), "No menu items available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to load menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}




