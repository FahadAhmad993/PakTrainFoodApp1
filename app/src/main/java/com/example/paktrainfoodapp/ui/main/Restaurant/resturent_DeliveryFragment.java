package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryBoyAdapter;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryBoyModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class resturent_DeliveryFragment extends Fragment {

    private RecyclerView recyclerView;
    private DeliveryBoyAdapter adapter;
    private List<DeliveryBoyModel> deliveryBoyList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resturent__delivery, container, false);

        recyclerView = view.findViewById(R.id.recyclerDeliveryBoys);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        loadRestaurantCity();

        return view;
    }

    // --- Step 1: Get Restaurant City ---
    private void loadRestaurantCity() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String city = documentSnapshot.getString("city");
                        if (city != null && !city.isEmpty()) {
                            loadDeliveryBoys(city);
                        } else {
                            Toast.makeText(getContext(), "City not found for restaurant", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Restaurant data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading restaurant: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- Step 2: Load Delivery Boys ---
    private void loadDeliveryBoys(String city) {
        db.collection("Users")
                .document("Delivery")
                .collection("VerifiedRegister")
                .whereEqualTo("city", city)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    deliveryBoyList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        DeliveryBoyModel boy = doc.toObject(DeliveryBoyModel.class);
                        String uid = doc.getId();
                        boy.setUid(uid); // save uid for fetching image later
                        deliveryBoyList.add(boy);

                        // --- Step 3: Fetch Image from separate path ---
                        db.collection("Users")
                                .document("Delivery")
                                .collection("Register")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(imageDoc -> {
                                    if (imageDoc.exists()) {
                                        String base64 = imageDoc.getString("imageBase64");
                                        boy.setImageBase64(base64);

                                        // Notify adapter for this updated item
                                        if (adapter != null) {
                                            int index = deliveryBoyList.indexOf(boy);
                                            if (index != -1) adapter.notifyItemChanged(index);
                                        }
                                    }
                                });
                    }

                    adapter = new DeliveryBoyAdapter(deliveryBoyList);
                    recyclerView.setAdapter(adapter);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading delivery boys: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}



