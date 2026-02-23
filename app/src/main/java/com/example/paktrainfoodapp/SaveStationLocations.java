package com.example.paktrainfoodapp;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

public class SaveStationLocations {

    public static void saveAllStations() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // --- Station List with Lat / Lng ---
        HashMap<String, double[]> stations = new HashMap<>();

        stations.put("Malakwal", new double[]{32.55307, 73.21293});
        stations.put("MandiBahauddin", new double[]{32.58816, 73.49734});
        stations.put("Lahore", new double[]{31.57712, 74.33618});
        stations.put("Rawalpindi", new double[]{33.60340, 73.04760});
        stations.put("Karachi", new double[]{24.95200, 67.22620});
        stations.put("Sargodha", new double[]{32.08530, 72.67420});
        stations.put("Gujrat", new double[]{32.57420, 74.07820});
        stations.put("Multan", new double[]{30.20000, 71.45000});
        stations.put("Sindh", new double[]{25.89430, 68.52470});
        stations.put("Khanewal", new double[]{30.30000, 71.93300});
        stations.put("Rohri", new double[]{27.69680, 68.90610});
        stations.put("Peshawar", new double[]{34.00230, 71.55030});
        stations.put("Havelian", new double[]{34.05950, 73.15940});
        stations.put("Lalamusa", new double[]{32.70360, 73.95740});

        // --- SAVE IN FIRESTORE ---
        for (String name : stations.keySet()) {

            double[] latLng = stations.get(name);

            HashMap<String, Object> stationData = new HashMap<>();
            stationData.put("lat", latLng[0]);
            stationData.put("lng", latLng[1]);

            db.collection("Users")
                    .document("StationLocation")
                    .collection("Stations")
                    .document(name)
                    .set(stationData);
        }
    }
}
