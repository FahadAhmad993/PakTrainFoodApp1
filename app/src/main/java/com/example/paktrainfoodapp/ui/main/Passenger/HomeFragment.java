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

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.Passenger.Passenger_Fragment_Loader;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class HomeFragment extends Fragment {

    private AutoCompleteTextView actvFrom, actvTo, actvTrain, actvMealStation;
    private Button btnNext;
    private TextView tvRoutePreview;

    private FirebaseFirestore db;

    private final List<String> allStations = new ArrayList<>();
    private final List<String> trainNames = new ArrayList<>();

    private final Map<String, List<String>> trainRoutes = new HashMap<>();
    private final Map<String, String> trainRouteIds = new HashMap<>();

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passenger_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        actvFrom = view.findViewById(R.id.actv_from);
        actvTo = view.findViewById(R.id.actv_to);
        actvTrain = view.findViewById(R.id.actv_train);
        actvMealStation = view.findViewById(R.id.actv_meal_station);
        btnNext = view.findViewById(R.id.btn_next_to_restaurants);
        tvRoutePreview = view.findViewById(R.id.tv_route_preview);

        db = FirebaseFirestore.getInstance();

        loadAllData();

        // Dropdowns automation triggers
        actvFrom.setOnClickListener(v -> actvFrom.showDropDown());
        actvTo.setOnClickListener(v -> actvTo.showDropDown());
        actvTrain.setOnClickListener(v -> actvTrain.showDropDown());
        actvMealStation.setOnClickListener(v -> actvMealStation.showDropDown());

        actvFrom.setOnItemClickListener((parent, view1, position, id) -> filterTrains());
        actvTo.setOnItemClickListener((parent, view1, position, id) -> filterTrains());

        actvTrain.setOnItemClickListener((parent, view12, position, id) -> {
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
                Toast.makeText(getContext(), "Select From, To, Train and Meal Station", Toast.LENGTH_SHORT).show();
                return;
            }

            String routeId = trainRouteIds.get(train);
            if (routeId == null) {
                Toast.makeText(getContext(), "Route ID missing", Toast.LENGTH_SHORT).show();
                return;
            }

            Passanger_Resturent_list_Fragment fragment = new Passanger_Resturent_list_Fragment();
            Bundle b = new Bundle();
            b.putString("selectedCity", mealStation);
            b.putString("TRAIN_NAME", train);
            b.putString("TRAIN_ID", train);
            b.putString("ROUTE_ID", routeId);
            b.putString("FROM", from);
            b.putString("TO", to);
            fragment.setArguments(b);

            // Loader wrapper handling
            Fragment parentFrag = getParentFragment();
            if (parentFrag instanceof Passenger_Fragment_Loader) {
                ((Passenger_Fragment_Loader) parentFrag).openRestaurantList(fragment);
            } else {
                getParentFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(getId(), fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    // ================= LOAD DATA WITH SAFETY =================
    private void loadAllData() {
        db.collection("RailwaySystem")
                .document("main")
                .collection("Stations")
                .get()
                .addOnSuccessListener(stations -> {
                    if (!isAdded() || getContext() == null) return; // Crash guard
                    allStations.clear();
                    for (DocumentSnapshot doc : stations.getDocuments()) {
                        allStations.add(doc.getId());
                    }
                    setupStationAdapter();
                });

        db.collection("RailwaySystem")
                .document("main")
                .collection("Trains")
                .get()
                .addOnSuccessListener(trains -> {
                    if (!isAdded() || getContext() == null) return; // Crash guard
                    trainNames.clear();
                    trainRouteIds.clear();
                    for (DocumentSnapshot doc : trains.getDocuments()) {
                        String name = doc.getString("name");
                        String number = doc.getString("number");
                        String routeId = doc.getString("routeId");

                        if (name == null || routeId == null)
                            continue;

                        String fullName = (number != null) ? name + " (" + number + ")" : name;
                        trainNames.add(fullName);
                        trainRouteIds.put(fullName, routeId);
                    }
                    setupTrainAdapter();
                    preloadRoutes();
                });
    }

    // ================= PRELOAD ROUTES WITH BACKGROUND LOOP PROTECTION =================
    private void preloadRoutes() {
        db.collection("RailwaySystem")
                .document("main")
                .collection("Routes")
                .get()
                .addOnSuccessListener(routes -> {
                    if (!isAdded() || getContext() == null) return; // Crash guard
                    trainRoutes.clear();
                    for (DocumentSnapshot doc : routes.getDocuments()) {
                        String routeId = doc.getId();
                        List<Map<String, Object>> stationMaps = (List<Map<String, Object>>) doc.get("stations");

                        if (stationMaps == null)
                            continue;

                        List<String> stationNames = new ArrayList<>();
                        for (Map<String, Object> stationMap : stationMaps) {
                            String stationName = (String) stationMap.get("name");
                            if (stationName != null) {
                                stationNames.add(stationName);
                            }
                        }

                        for (String train : trainRouteIds.keySet()) {
                            if (routeId.equals(trainRouteIds.get(train))) {
                                trainRoutes.put(train, stationNames);
                            }
                        }
                    }
                });
    }

    private void setupStationAdapter() {
        if (!isAdded() || getContext() == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                allStations
        );
        actvFrom.setAdapter(adapter);
        actvTo.setAdapter(adapter);
    }

    private void setupTrainAdapter() {
        if (!isAdded() || getContext() == null) return;
        actvTrain.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                trainNames
        ));
    }

    private void filterTrains() {
        String from = actvFrom.getText().toString().trim();
        String to = actvTo.getText().toString().trim();

        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) {
            return;
        }

        List<String> filtered = new ArrayList<>();
        for (String train : trainRoutes.keySet()) {
            List<String> route = trainRoutes.get(train);
            if (route == null)
                continue;

            int fromIndex = route.indexOf(from);
            int toIndex = route.indexOf(to);

            if (fromIndex != -1 && toIndex != -1 && fromIndex < toIndex) {
                filtered.add(train);
            }
        }

        if (isAdded() && getContext() != null) {
            actvTrain.setAdapter(new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    filtered
            ));
        }

        actvTrain.setText("");
        actvMealStation.setText("");
        tvRoutePreview.setText("");
    }

    private void showRoute(String train) {
        List<String> route = trainRoutes.get(train);
        if (route == null || route.isEmpty())
            return;

        String from = actvFrom.getText().toString().trim();
        String to = actvTo.getText().toString().trim();

        int fromIndex = route.indexOf(from);
        int toIndex = route.indexOf(to);

        if (fromIndex == -1 || toIndex == -1 || fromIndex > toIndex) {
            return;
        }

        List<String> mealStations = new ArrayList<>(route.subList(fromIndex, toIndex + 1));

        if (isAdded() && getContext() != null) {
            actvMealStation.setAdapter(new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    mealStations
            ));
        }

        tvRoutePreview.setText("Route: " + TextUtils.join(" → ", mealStations));
    }
}



