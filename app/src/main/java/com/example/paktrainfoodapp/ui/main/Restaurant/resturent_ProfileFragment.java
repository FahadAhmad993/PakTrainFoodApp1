package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class resturent_ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView txtName, txtEmail;
    private Button btnLogout;
    private static final String TAG = "ProfileFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resturent__profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImage = view.findViewById(R.id.img_profile);
        txtName = view.findViewById(R.id.txt_name);
        txtEmail = view.findViewById(R.id.txt_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        loadUserData();

        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        // 1. Firebase sign out
        FirebaseAuth.getInstance().signOut();

        // 2. Data clear
        PrefManager prefManager = new PrefManager(requireContext());
        prefManager.setLogin(false);
        prefManager.clear();

        // 3. Navigation with safe Context
        Intent intent = new Intent(requireContext(), Splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 4. Activity finish safely
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void loadUserData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded() || getContext() == null) return;

                    if (snapshot.exists()) {
                        txtName.setText(snapshot.getString("restaurantName"));
                        txtEmail.setText(snapshot.getString("email"));

                        String imageUrl = snapshot.getString("licenseImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext()).load(imageUrl).into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error: ", e); // Logcat mein error yahan dikhega
                });
    }
}



