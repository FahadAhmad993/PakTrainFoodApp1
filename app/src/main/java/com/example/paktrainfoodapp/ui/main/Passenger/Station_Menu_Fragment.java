package com.example.paktrainfoodapp.ui.main.Passenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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

    // Restaurant data
    private String restaurantId = "";
    private String restaurantName = "";
    private String restaurantImageBase64 = "";
    private String restaurantInfo = "";
    private String mealStation = "";

    // Route data (IMPORTANT FOR ORDER SYSTEM)
    private String trainId = "";
    private String routeId = "";
    private String fromStation = "";
    private String toStation = "";
    private String trainName = "";

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

        // ================= GET DATA =================
        Bundle args = getArguments();
        if (args != null) {

            restaurantId = args.getString("RESTAURANT_UID", "");
            restaurantName = args.getString("RESTAURANT_NAME", "");
            restaurantImageBase64 = args.getString("RESTAURANT_IMAGE", "");
            restaurantInfo = args.getString("RESTAURANT_INFO", "Fast Food");

            mealStation = args.getString("MEAL_STATION", "");

            // 🔥 ROUTE DATA
            trainId = args.getString("TRAIN_ID", "");
            routeId = args.getString("ROUTE_ID", "");
            fromStation = args.getString("FROM", "");
            toStation = args.getString("TO", "");
            trainName = args.getString("TRAIN_NAME", "");
        }

        // ================= UI =================
        txtRestaurantName.setText(restaurantName);
        txtRestaurantInfo.setText(restaurantInfo);

        if (restaurantImageBase64 != null && !restaurantImageBase64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(restaurantImageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                imgRestaurant.setImageBitmap(bitmap);
            } catch (Exception e) {
                imgRestaurant.setImageResource(R.drawable.station3);
            }
        } else {
            imgRestaurant.setImageResource(R.drawable.station3);
        }

        // ================= BACK =================
        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // ================= LOAD MENU =================
        loadMenuItemsByRestaurantId(restaurantId);

        // ================= ITEM CLICK =================
        adapter.setOnMenuItemClickListener(new MenuAdapter.OnMenuItemClickListener() {

            @Override
            public void onAddToCart(MenuitemModel item) {
                Toast.makeText(requireContext(),
                        item.getName() + " added to cart",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteOrder(MenuitemModel item) {}

            @Override
            public void onBuyNow(MenuitemModel item) {

                OrderNowFragment dialog = OrderNowFragment.newInstance(
                        item.getName(),
                        item.getPrice(),
                        item.getDescription(),
                        restaurantName,
                        restaurantId,
                        item.getImageUrl(),
                        mealStation,

                        // 🔥 FULL ROUTE CONTEXT PASS
                        trainId,
                        routeId,
                        fromStation,
                        toStation
                );

                dialog.show(getParentFragmentManager(), "OrderNowDialog");
            }
        });
    }

    // ================= LOAD MENU =================
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
                        Toast.makeText(requireContext(),
                                "No menu items found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}


