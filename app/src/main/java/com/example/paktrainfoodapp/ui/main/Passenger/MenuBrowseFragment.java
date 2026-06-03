package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import java.util.concurrent.atomic.AtomicInteger;

public class MenuBrowseFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private final List<MenuitemModel> itemList = new ArrayList<>();
    private MenuAdapter adapter;

    public MenuBrowseFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passanger_menu_browse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerBrowseMenu);
        progressBar = view.findViewById(R.id.progressBarBrowse);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MenuAdapter(itemList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadAllMenuItems();
    }

    private void loadAllMenuItems() {
        progressBar.setVisibility(View.VISIBLE);
        itemList.clear();

        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .get()
                .addOnSuccessListener(restaurants -> {
                    // 1️⃣ Crash Guard: Check if fragment is still attached to activity
                    if (!isAdded() || getContext() == null) return;

                    if (restaurants.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No restaurants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final int totalRestaurants = restaurants.size();
                    final AtomicInteger remainingRestaurants = new AtomicInteger(totalRestaurants);

                    for (QueryDocumentSnapshot restDoc : restaurants) {
                        String restaurantId = restDoc.getId();
                        String restaurantName = restDoc.getString("restaurantName");
                        String restaurantCity = restDoc.getString("restaurantCity");

                        db.collection("Users")
                                .document("Restaurant")
                                .collection("VerifiedRegister")
                                .document(restaurantId)
                                .collection("MenuItems")
                                .get()
                                .addOnSuccessListener(menuItems -> {
                                    // 2️⃣ Crash Guard for Async Callbacks
                                    if (!isAdded() || getContext() == null) return;

                                    for (QueryDocumentSnapshot menuDoc : menuItems) {
                                        MenuitemModel item = menuDoc.toObject(MenuitemModel.class);
                                        item.setId(menuDoc.getId());
                                        item.setRestaurantId(restaurantId);
                                        item.setRestaurantName(restaurantName);
                                        item.setRestaurantCity(restaurantCity);
                                        itemList.add(item);
                                    }

                                    checkLoadingComplete(remainingRestaurants);
                                })
                                .addOnFailureListener(e -> {
                                    if (!isAdded() || getContext() == null) return;
                                    checkLoadingComplete(remainingRestaurants);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // 3️⃣ Crash Guard for Root Failure
                    if (!isAdded() || getContext() == null) return;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Helper method to monitor counter atomic ticks and safely push notifications/UI updates
     */
    private void checkLoadingComplete(AtomicInteger remaining) {
        if (remaining.decrementAndGet() == 0) {
            // UI Thread synchronization protection
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);

            if (itemList.isEmpty()) {
                Toast.makeText(getContext(), "No menu items available", Toast.LENGTH_SHORT).show();
            }
        }
    }
}



//

