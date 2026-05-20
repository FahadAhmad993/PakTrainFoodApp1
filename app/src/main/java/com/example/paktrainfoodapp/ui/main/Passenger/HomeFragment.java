package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.paktrainfoodapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class HomeFragment extends Fragment {

    private AutoCompleteTextView actvFrom, actvTo, actvTrain, actvMealStation;
    private Button btnNext;
    private TextView tvRoutePreview;

    private FirebaseFirestore db;

    private List<String> allStations = new ArrayList<>();
    private List<String> trainNames = new ArrayList<>();

    // train -> route stations
    private Map<String, List<String>> trainRoutes = new HashMap<>();

    // IMPORTANT FIX: trainName -> routeId mapping
    private Map<String, String> trainRouteIds = new HashMap<>();

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passenger_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        actvFrom = view.findViewById(R.id.actv_from);
        actvTo = view.findViewById(R.id.actv_to);
        actvTrain = view.findViewById(R.id.actv_train);
        actvMealStation = view.findViewById(R.id.actv_meal_station);
        btnNext = view.findViewById(R.id.btn_next_to_restaurants);
        tvRoutePreview = view.findViewById(R.id.tv_route_preview);

        db = FirebaseFirestore.getInstance();

        loadAllData();

        actvFrom.setOnClickListener(v -> actvFrom.showDropDown());
        actvTo.setOnClickListener(v -> actvTo.showDropDown());
        actvTrain.setOnClickListener(v -> actvTrain.showDropDown());
        actvMealStation.setOnClickListener(v -> actvMealStation.showDropDown());

        actvFrom.setOnItemClickListener((p, v1, pos, id) -> filterTrains());
        actvTo.setOnItemClickListener((p, v1, pos, id) -> filterTrains());

        actvTrain.setOnItemClickListener((parent, view1, position, id) -> {
            String train = (String) parent.getItemAtPosition(position);
            showRoute(train);
        });

        btnNext.setOnClickListener(v -> {

            String from = actvFrom.getText().toString().trim();
            String to = actvTo.getText().toString().trim();
            String train = actvTrain.getText().toString().trim();
            String mealStation = actvMealStation.getText().toString().trim();

            if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)
                    || TextUtils.isEmpty(train) || TextUtils.isEmpty(mealStation)) {

                Toast.makeText(getContext(),
                        "Select From, To, Train and Meal Station",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String routeId = trainRouteIds.get(train);

            if (routeId == null) {
                Toast.makeText(getContext(),
                        "Route ID missing for selected train",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Passanger_Resturent_list_Fragment fragment = new Passanger_Resturent_list_Fragment();

            Bundle b = new Bundle();

            // DO NOT CHANGE (your requirement)
            b.putString("selectedCity", mealStation);

            // FIXED KEYS (IMPORTANT)
            b.putString("TRAIN_NAME", train);
            b.putString("TRAIN_ID", train); // 🔥 FIX: now trainId is not empty anymore
            b.putString("ROUTE_ID", routeId);
            b.putString("FROM", from);
            b.putString("TO", to);

            fragment.setArguments(b);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_holder, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    // ================= LOAD DATA =================
    private void loadAllData() {

        db.collection("RailwaySystem")
                .document("main")
                .collection("Stations")
                .get()
                .addOnSuccessListener(stations -> {

                    allStations.clear();

                    for (var doc : stations.getDocuments()) {
                        allStations.add(doc.getId());
                    }

                    setupStationAdapter();
                });

        db.collection("RailwaySystem")
                .document("main")
                .collection("Trains")
                .get()
                .addOnSuccessListener(trains -> {

                    trainNames.clear();
                    trainRouteIds.clear();

                    for (var doc : trains.getDocuments()) {

                        String name = doc.getString("name");
                        String number = doc.getString("number");
                        String routeId = doc.getString("routeId");

                        if (name == null || routeId == null) continue;

                        String fullName = name + " (" + number + ")";

                        trainNames.add(fullName);
                        trainRouteIds.put(fullName, routeId);
                    }

                    setupTrainAdapter();
                    preloadRoutes();
                });
    }

    // ================= ROUTES =================
    private void preloadRoutes() {

        db.collection("RailwaySystem")
                .document("main")
                .collection("Routes")
                .get()
                .addOnSuccessListener(routes -> {

                    trainRoutes.clear();

                    for (var doc : routes.getDocuments()) {

                        String routeId = doc.getId();
                        List<String> stations = (List<String>) doc.get("stations");

                        if (stations == null) continue;

                        for (String train : trainRouteIds.keySet()) {

                            if (routeId.equals(trainRouteIds.get(train))) {
                                trainRoutes.put(train, stations);
                            }
                        }
                    }
                });
    }

    private void setupStationAdapter() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                allStations
        );

        actvFrom.setAdapter(adapter);
        actvTo.setAdapter(adapter);
    }

    private void setupTrainAdapter() {

        actvTrain.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                trainNames
        ));
    }

    // ================= FILTER =================
    private void filterTrains() {

        String from = actvFrom.getText().toString();
        String to = actvTo.getText().toString();

        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) return;

        List<String> filtered = new ArrayList<>();

        for (String train : trainRoutes.keySet()) {

            List<String> route = trainRoutes.get(train);

            if (route == null) continue;

            int fromIndex = route.indexOf(from);
            int toIndex = route.indexOf(to);

            if (fromIndex != -1 && toIndex != -1 && fromIndex < toIndex) {
                filtered.add(train);
            }
        }

        actvTrain.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                filtered
        ));
    }

    // ================= ROUTE PREVIEW =================
    private void showRoute(String train) {

        List<String> route = trainRoutes.get(train);

        if (route == null) return;

        actvMealStation.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                route
        ));

        tvRoutePreview.setText("Route: " + TextUtils.join(" → ", route));
    }
}
