package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paktrainfoodapp.R;
import java.util.ArrayList;
import java.util.List;

public class resturent_DeliveryFragment extends Fragment {


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Sirf layout inflate karo
        View view = inflater.inflate(R.layout.fragment_resturent__delivery, container, false);



        return view;
    }
}


//
//package com.example.paktrainfoodapp.ui.main.Restaurant;
//
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//
//import com.example.paktrainfoodapp.R;
//import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryBoyAdapter;
//import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryBoyModel;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class resturent_DeliveryFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private LinearLayout layoutNoData;
//
//    private RestaurantDeliveryBoyAdapter adapter;
//    private List<com.example.paktrainfoodapp.ui.main.Restaurant.RestaurantDeliveryBoyModel> deliveryBoyList = new ArrayList<>();
//
//    private FirebaseFirestore db;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_resturent__delivery, container, false);
//
//        recyclerView = view.findViewById(R.id.recyclerDeliveryBoys);
////        layoutNoData = view.findViewById(R.id.layoutNoOrders);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        db = FirebaseFirestore.getInstance();
//
//        adapter = new RestaurantDeliveryBoyAdapter(deliveryBoyList);
//        recyclerView.setAdapter(adapter);
//
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        loadRestaurantCity();
//    }
//
//    // ================= CITY FETCH =================
//    private void loadRestaurantCity() {
//
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        db.collection("Users")
//                .document("Restaurant")
//                .collection("VerifiedRegister")
//                .document(userId)
//                .get()
//                .addOnSuccessListener(doc -> {
//
//                    if (!isAdded()) return;
//
//                    String city = doc.getString("city");
//
//                    if (city != null && !city.isEmpty()) {
//                        loadDeliveryBoys(city);
//                    } else {
//                        Toast.makeText(getContext(),
//                                "City not set",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    // ================= LOAD DELIVERY BOYS =================
//    private void loadDeliveryBoys(String city) {
//
//        db.collection("Users")
//                .document("Delivery")
//                .collection("VerifiedRegister")
//                .whereEqualTo("city", city)
//                .get()
//                .addOnSuccessListener(snapshots -> {
//
//                    if (!isAdded()) return;
//
//                    deliveryBoyList.clear();
//
//                    for (QueryDocumentSnapshot doc : snapshots) {
//
//                        com.example.paktrainfoodapp.ui.main.Restaurant.RestaurantDeliveryBoyModel boy =
//                                doc.toObject(com.example.paktrainfoodapp.ui.main.Restaurant.RestaurantDeliveryBoyModel.class);
//
//                        boy.setUid(doc.getId());
//
//                        deliveryBoyList.add(boy);
//
//                        fetchBoyImage(boy);
//                    }
//
//                    adapter.notifyDataSetChanged();
//
//                    if (deliveryBoyList.isEmpty()) {
//                        recyclerView.setVisibility(View.GONE);
//                        layoutNoData.setVisibility(View.VISIBLE);
//                    } else {
//                        recyclerView.setVisibility(View.VISIBLE);
//                        layoutNoData.setVisibility(View.GONE);
//                    }
//                });
//    }
//
//    // ================= FETCH IMAGE =================
//    private void fetchBoyImage(com.example.paktrainfoodapp.ui.main.Restaurant.RestaurantDeliveryBoyModel boy) {
//
//        db.collection("Users")
//                .document("Delivery")
//                .collection("Register")
//                .document(boy.getUid())
//                .get()
//                .addOnSuccessListener(imageDoc -> {
//
//                    if (!isAdded() || !imageDoc.exists()) return;
//
//                    String base64 = imageDoc.getString("imageBase64");
//
//                    boy.setImageBase64(base64);
//
//                    adapter.notifyDataSetChanged();
//                });
//    }
//}