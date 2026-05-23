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
    private List<MenuitemModel> itemList = new ArrayList<>();
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
                    if (restaurants.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "No restaurants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AtomicInteger remainingRestaurants = new AtomicInteger(restaurants.size());

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
                                    for (QueryDocumentSnapshot menuDoc : menuItems) {
                                        MenuitemModel item = menuDoc.toObject(MenuitemModel.class);
                                        item.setId(menuDoc.getId());
                                        item.setRestaurantId(restaurantId);
                                        item.setRestaurantName(restaurantName);
                                        item.setRestaurantCity(restaurantCity);
                                        itemList.add(item);
                                    }

                                    if (remainingRestaurants.decrementAndGet() == 0) {
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);

                                        if (itemList.isEmpty()) {
                                            Toast.makeText(requireContext(), "No menu items available", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (remainingRestaurants.decrementAndGet() == 0) {
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);

                                        if (itemList.isEmpty()) {
                                            Toast.makeText(requireContext(), "No menu items available", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to load restaurants: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}




