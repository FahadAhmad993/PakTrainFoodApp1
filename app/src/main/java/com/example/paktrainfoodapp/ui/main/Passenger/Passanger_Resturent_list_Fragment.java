package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Passanger_Resturent_list_Fragment extends Fragment {

    private RecyclerView rv;
    private ArrayList<RestaurantModel> list;
    private RestaurantAdapter adapter;
    private FirebaseFirestore db;

    private TextView tvTopTitle;
    private ProgressBar progressBar;

    private String selectedCity;
    private String trainName;
    private String routeId;
    private String fromStation;
    private String toStation;

    public Passanger_Resturent_list_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_passanger__resturent_list_,
                container,
                false
        );

        rv = view.findViewById(R.id.rv_restaurants);
        tvTopTitle = view.findViewById(R.id.tv_top_title);
        progressBar = view.findViewById(R.id.progressBar1);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new RestaurantAdapter(list);

        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // ================= GET ARGUMENTS =================
        if (getArguments() != null) {

            selectedCity = getArguments().getString("selectedCity");
            trainName = getArguments().getString("TRAIN_NAME");
            routeId = getArguments().getString("ROUTE_ID");
            fromStation = getArguments().getString("FROM");
            toStation = getArguments().getString("TO");
        }

        if (selectedCity == null) {
            selectedCity = "Unknown";
        }

        tvTopTitle.setText("Restaurants in " + selectedCity);

        // Debug (future ETA tracking)
        android.util.Log.d("ROUTE_INFO",
                "Train=" + trainName +
                        " Route=" + routeId +
                        " From=" + fromStation +
                        " To=" + toStation +
                        " Meal=" + selectedCity
        );

        loadRestaurants(selectedCity);

        // ================= CLICK HANDLING =================
        adapter.setOnItemClickListener(new RestaurantAdapter.OnItemClickListener() {
            @Override
            public void onFavoriteClick(int position) {}

            @Override
            public void onItemClick(int position) {

                RestaurantModel model = list.get(position);

                Station_Menu_Fragment fragment = new Station_Menu_Fragment();

                Bundle bundle = new Bundle();
                bundle.putString("RESTAURANT_UID", model.getUid());
                bundle.putString("RESTAURANT_NAME", model.getRestaurantName());
                bundle.putString("RESTAURANT_IMAGE", model.getImageBase64());

                // IMPORTANT: route context pass forward
                bundle.putString("MEAL_STATION", selectedCity);
                bundle.putString("TRAIN_NAME", trainName);
                bundle.putString("ROUTE_ID", routeId);
                bundle.putString("FROM", fromStation);
                bundle.putString("TO", toStation);

                fragment.setArguments(bundle);

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_holder, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    // ================= LOAD RESTAURANTS =================
    private void loadRestaurants(String city) {

        progressBar.setVisibility(View.VISIBLE);

        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .whereEqualTo("city", city)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    list.clear();

                    for (DocumentSnapshot doc : querySnapshot) {

                        String uid = doc.getId();
                        String restaurantName = doc.getString("restaurantName");
                        String cityName = doc.getString("city");

                        db.collection("Users")
                                .document("Restaurant")
                                .collection("Register")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(imageDoc -> {

                                    String imageBase64 = null;

                                    if (imageDoc.exists()) {
                                        imageBase64 = imageDoc.getString("imageBase64");
                                    }

                                    list.add(new RestaurantModel(
                                            uid,
                                            restaurantName,
                                            cityName,
                                            imageBase64
                                    ));

                                    adapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
                    }

                    if (querySnapshot.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "No restaurants found in " + city,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}





