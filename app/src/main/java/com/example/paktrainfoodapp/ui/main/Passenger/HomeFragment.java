package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.paktrainfoodapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private ImageView imageTop;
    private AutoCompleteTextView actvFrom, actvTo, actvTrain, actvMealStation;
    private ImageButton btnSwap;
    private Button btnNext;
    private TextView tvRoutePreview;

    private List<String> allStations = new ArrayList<>();
    private Map<String, List<String>> trainRoutes = new HashMap<>();
    private List<String> trainNames = new ArrayList<>();
    private FirebaseFirestore db;

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
        super.onViewCreated(view, savedInstanceState);

        imageTop = view.findViewById(R.id.image_station_top);
        actvFrom = view.findViewById(R.id.actv_from);
        actvTo = view.findViewById(R.id.actv_to);
        actvTrain = view.findViewById(R.id.actv_train);
        actvMealStation = view.findViewById(R.id.actv_meal_station);
        btnSwap = view.findViewById(R.id.btn_swap);
        btnNext = view.findViewById(R.id.btn_next_to_restaurants);
        tvRoutePreview = view.findViewById(R.id.tv_route_preview);

        db = FirebaseFirestore.getInstance();

        seedLocalData();
        setupStationAdapters();
        setupTrainAdapter();

        actvFrom.setOnClickListener(v -> actvFrom.showDropDown());
        actvTo.setOnClickListener(v -> actvTo.showDropDown());
        actvTrain.setOnClickListener(v -> actvTrain.showDropDown());
        actvMealStation.setOnClickListener(v -> actvMealStation.showDropDown());

        btnSwap.setOnClickListener(v -> swapFromTo());

        actvTrain.setOnItemClickListener((parent, view1, position, id) -> {
            String selTrain = (String) parent.getItemAtPosition(position);
            populateMealStationsForTrain(selTrain,
                    actvFrom.getText().toString(),
                    actvTo.getText().toString());
        });

        actvFrom.setOnItemClickListener((parent, view12, position, id) -> filterTrainsByStations());
        actvTo.setOnItemClickListener((parent, view13, position, id) -> filterTrainsByStations());

        btnNext.setOnClickListener(v -> {
            String from = actvFrom.getText().toString().trim();
            String to = actvTo.getText().toString().trim();
            String train = actvTrain.getText().toString().trim();
            String mealStation = actvMealStation.getText().toString().trim();

            if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)
                    || TextUtils.isEmpty(train) || TextUtils.isEmpty(mealStation)) {
                Toast.makeText(requireContext(),
                        "Please select From, To, Train and Meal Station", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to Station_Menu_Fragment
//            Station_Menu_Fragment fragment = new Station_Menu_Fragment();
            Passanger_Resturent_list_Fragment fragment = new Passanger_Resturent_list_Fragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectedCity", mealStation);
            fragment.setArguments(bundle);

            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_holder, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    private void swapFromTo() {
        String a = actvFrom.getText().toString();
        String b = actvTo.getText().toString();
        actvFrom.setText(b);
        actvTo.setText(a);
        filterTrainsByStations();
    }

    private void seedLocalData() {
        allStations = Arrays.asList(
                "Karachi Cantt", "Hyderabad Jn", "Sukkur", "Rohri Jn", "Multan Cantt",
                "Bahawalpur", "Rahim Yar Khan", "Sahiwal", "Faisalabad",
                "Sargodha Jn", "Gujranwala", "Wazirabad Jn", "Lahore Jn",
                "Gujrat", "Mandi Bahauddin", "Malakwal Jn", "Lalamusa Jn", "Jhelum",
                "Rawalpindi", "Islamabad", "Attock", "Peshawar Cantt"
        );

        trainRoutes.put("Khyber Mail", Arrays.asList(
                "Karachi Cantt", "Hyderabad Jn", "Rohri Jn", "Multan Cantt", "Lahore Jn", "Rawalpindi", "Peshawar Cantt"
        ));
        trainRoutes.put("Green Line", Arrays.asList(
                "Lahore Jn", "Faisalabad", "Sargodha Jn", "Malakwal Jn", "Lalamusa Jn"
        ));
        trainRoutes.put("Awam Express", Arrays.asList(
                "Peshawar Cantt", "Attock", "Rawalpindi", "Lahore Jn", "Karachi Cantt"
        ));
        trainNames.clear();
        trainNames.addAll(trainRoutes.keySet());
    }

    private void setupStationAdapters() {
        ArrayAdapter<String> stationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                allStations
        );
        actvFrom.setAdapter(stationAdapter);
        actvTo.setAdapter(stationAdapter);
    }

    private void setupTrainAdapter() {
        ArrayAdapter<String> trainAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                trainNames
        );
        actvTrain.setAdapter(trainAdapter);
    }

    private void filterTrainsByStations() {
        String from = actvFrom.getText().toString().trim();
        String to = actvTo.getText().toString().trim();

        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) {
            setupTrainAdapter();
            tvRoutePreview.setText("");
            actvMealStation.setText("");
            actvMealStation.setAdapter(null);
            return;
        }

        List<String> filtered = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : trainRoutes.entrySet()) {
            List<String> route = e.getValue();
            if (route.contains(from) && route.contains(to)) {
                filtered.add(e.getKey());
            }
        }

        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                filtered);
        actvTrain.setAdapter(a);

        tvRoutePreview.setText(filtered.size() + " trains found");
    }

    private void populateMealStationsForTrain(String trainName, String from, String to) {
        List<String> route = trainRoutes.get(trainName);
        if (route == null || route.isEmpty()) return;

        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                route
        );
        actvMealStation.setAdapter(mealAdapter);
        tvRoutePreview.setText("Route: " + TextUtils.join(" → ", route));
    }
}



