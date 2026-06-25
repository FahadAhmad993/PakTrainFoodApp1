package com.example.paktrainfoodapp.ui.main.Delivery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private ImageView btnEditProfile;
    private TextView txtName,riderWallet, txtEmail;
    private TextView btnLogout; // Badla hua type Card List ke mutabiq

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PrefManager prefManager;

    private ActivityResultLauncher<String> galleryLauncher;

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
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        txtName = view.findViewById(R.id.txt_delivery_name);
        txtEmail = view.findViewById(R.id.txt_delivery_email);
        btnLogout = view.findViewById(R.id.btn_delivery_logout); // Matching ID text reference
        riderWallet  = view.findViewById(R.id.riderWallet);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefManager = new PrefManager(requireContext());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null && isAdded() && getActivity() != null) {
                            if (profileImage != null) {
                                Glide.with(requireActivity())
                                        .load(uri)
                                        .circleCrop()
                                        .into(profileImage);
                            }
                            Toast.makeText(getContext(), "Delivery Rider Image Changed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        }

        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        } else {
            if (txtName != null) txtName.setText("Guest");
            if (txtEmail != null) txtEmail.setText("");
        }

        //rider wallet open
        riderWallet.setOnClickListener(v -> {

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_loader,
                            new RiderWalletFragment()
                    )
                    .addToBackStack("profile")
                    .commit();
        });

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (mAuth != null) {
                    mAuth.signOut();
                }
                if (prefManager != null) {
                    prefManager.setLogin(false);
                }
                if (getActivity() != null) {
                    getActivity().finish();
                }
                startActivity(new Intent(getContext(), Splash.class));
            });
        }
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users")
                .document("Delivery")
                .collection("VerifiedRegister")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (isAdded() && snapshot.exists()) {
                        String deliveryBoyName = snapshot.getString("name");
                        String email = snapshot.getString("email");
                        String imageUrl = snapshot.getString("cnicFrontImageUrl");

                        if (txtName != null) txtName.setText(deliveryBoyName != null ? deliveryBoyName : "No Name");
                        if (txtEmail != null) txtEmail.setText(email != null ? email : "No Email");

                        if (imageUrl != null && !imageUrl.isEmpty() && profileImage != null) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.edit_info)
                                    .error(R.drawable.edit_info)
                                    .circleCrop()
                                    .into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        if (txtName != null) txtName.setText("Error loading data");
                        if (txtEmail != null) txtEmail.setText("");
                        Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}