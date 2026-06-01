package com.example.paktrainfoodapp.ui.main.Delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.Splash;
import com.example.paktrainfoodapp.utils.PrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeliveryProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView txtName, txtEmail;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PrefManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImage = view.findViewById(R.id.img_delivery_profile);
        txtName = view.findViewById(R.id.txt_delivery_name);
        txtEmail = view.findViewById(R.id.txt_delivery_email);
        btnLogout = view.findViewById(R.id.btn_delivery_logout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefManager = new PrefManager(requireContext());

        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        } else {
            txtName.setText("Guest");
            txtEmail.setText("");
        }

        btnLogout.setOnClickListener(v -> {
            if (mAuth != null) {
                mAuth.signOut();
            }

            // 🔄 Shared Preferences se login state reset karein
            prefManager.setLogin(false);

            if (getActivity() != null) {
                getActivity().finish();
            }

            startActivity(new Intent(getContext(), Splash.class));
        });
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();

        // 🔄 Path aapki naye sequential verification architecture ke mutabiq "VerifiedRegister" par set kar diya hai
        db.collection("Users")
                .document("Delivery")
                .collection("VerifiedRegister")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (isAdded() && snapshot.exists()) {

                        // Firestore se clean fields fetch karein
                        String deliveryBoyName = snapshot.getString("name");
                        String email = snapshot.getString("email");

                        // 🖼️ ImgBB sequential upload se jo URL "cnicFrontImageUrl" ya "licenseImageUrl" banna tha, woh yahan call hoga
                        String imageUrl = snapshot.getString("cnicFrontImageUrl");

                        txtName.setText(deliveryBoyName != null ? deliveryBoyName : "No Name");
                        txtEmail.setText(email != null ? email : "No Email");


                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_background) // Default temporary image
                                    .error(R.drawable.ic_launcher_background)       // Failure image
                                    .into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        txtName.setText("Error loading data");
                        txtEmail.setText("");
                        Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}



