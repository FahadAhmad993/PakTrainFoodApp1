package com.example.paktrainfoodapp.ui;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class FirebaseSeeder {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ================= RUN ALL =================
    public void seedAll() {
        uploadStations();
        uploadRoutes();
        uploadTrains();
    }

    private DocumentReference root() {
        return db.collection("RailwaySystem").document("main");
    }

    // ================= STATIONS =================
    private void uploadStations() {

        addStation("SialkotJn", 32.4945, 74.5229);
        addStation("Chawinda", 32.344, 74.705);
        addStation("Pasrur", 32.262, 74.664);
        addStation("NarowalJn", 32.100, 74.874);
        addStation("LahoreJn", 31.565, 74.341);
        addStation("RaiwindJn", 31.248, 74.215);
        addStation("Okara", 30.808, 73.445);
        addStation("Sahiwal", 30.668, 73.106);
        addStation("MultanCantt", 30.199, 71.475);
        addStation("Bahawalpur", 29.395, 71.683);
        addStation("RahimYarKhan", 28.420, 70.298);
        addStation("RohriJn", 27.683, 68.895);
        addStation("HyderabadJn", 25.396, 68.357);
        addStation("KarachiCantt", 24.854, 67.009);

        addStation("Rawalpindi", 33.565, 73.016);
        addStation("GujarKhan", 33.254, 73.304);
        addStation("Jhelum", 32.940, 73.727);
        addStation("LalaMusaJn", 32.704, 73.958);
        addStation("Gujranwala", 32.187, 74.194);
        addStation("WazirabadJn", 32.444, 74.115);
        addStation("PeshawarCantt", 34.015, 71.580);
        addStation("Faisalabad", 31.418, 73.079);
        addStation("SargodhaJn", 32.083, 72.671);
        addStation("MandiBahauddin", 32.585, 73.491);
        addStation("MalakwalJn", 32.553, 73.212);
    }

    private void addStation(String name, double lat, double lng) {

        Map<String, Object> map = new HashMap<>();
        map.put("lat", lat);
        map.put("lng", lng);
        map.put("isActive", true);

        root().collection("Stations")
                .document(name)
                .set(map);
    }

    // ================= ROUTES (INDEX BASED FIXED) =================
    private void uploadRoutes() {

        addRoute("route_awan_up", "Karachi → Peshawar", Arrays.asList(
                station("KarachiCantt", 0),
                station("HyderabadJn", 1),
                station("RohriJn", 2),
                station("RahimYarKhan", 3),
                station("Bahawalpur", 4),
                station("MultanCantt", 5),
                station("Sahiwal", 6),
                station("Okara", 7),
                station("LahoreJn", 8),
                station("Gujranwala", 9),
                station("WazirabadJn", 10),
                station("LalaMusaJn", 11),
                station("Jhelum", 12),
                station("GujarKhan", 13),
                station("Rawalpindi", 14),
                station("PeshawarCantt", 15)
        ));

        addRoute("route_hazara", "Karachi → Rawalpindi", Arrays.asList(
                station("KarachiCantt", 0),
                station("HyderabadJn", 1),
                station("RohriJn", 2),
                station("Bahawalpur", 3),
                station("MultanCantt", 4),
                station("SargodhaJn", 5),
                station("MalakwalJn", 6),
                station("MandiBahauddin", 7),
                station("LalaMusaJn", 8),
                station("Jhelum", 9),
                station("Rawalpindi", 10)
        ));

        addRoute("route_allama_iqbal", "Sialkot → Karachi", Arrays.asList(
                station("SialkotJn", 0),
                station("Chawinda", 1),
                station("Pasrur", 2),
                station("NarowalJn", 3),
                station("LahoreJn", 4),
                station("RaiwindJn", 5),
                station("Okara", 6),
                station("Sahiwal", 7),
                station("MultanCantt", 8),
                station("Bahawalpur", 9),
                station("RahimYarKhan", 10),
                station("RohriJn", 11),
                station("HyderabadJn", 12),
                station("KarachiCantt", 13)
        ));
    }

    private Map<String, Object> station(String name, int index) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("index", index);
        return m;
    }

    private void addRoute(String id, String name, List<Map<String, Object>> stations) {

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("stations", stations);
        map.put("isActive", true);

        root().collection("Routes")
                .document(id)
                .set(map);
    }

    // ================= TRAINS =================
    private void uploadTrains() {

        addTrain("train_13", "Awan Express", "13UP", "route_awan_up");
        addTrain("train_11", "Hazara Express", "11UP", "route_hazara");
        addTrain("train_10N", "Allama Iqbal Express", "10N", "route_allama_iqbal");
    }

    private void addTrain(String id, String name, String number, String routeId) {

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("number", number);
        map.put("routeId", routeId);
        map.put("avgSpeed", 60);
        map.put("stopTime", 10);
        map.put("isActive", true);
        map.put("isRunning", true);

        root().collection("Trains")
                .document(id)
                .set(map);
    }
}