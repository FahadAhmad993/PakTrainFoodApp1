package com.example.paktrainfoodapp.ui.main.Passenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    // txt_name aur txt_email ke liye IDs XML mein honi chahiye,
    // agar nahi hain to XML mein add karein
    private TextView txtName, txtEmail, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PrefManager prefManager;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passanger_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI Components Initialize (XML IDs ke mutabik)
        profileImage = view.findViewById(R.id.img_profile);
        txtName = view.findViewById(R.id.txt_name);      // Ensure XML has android:id="@+id/txt_name"
        txtEmail = view.findViewById(R.id.txt_email);    // Ensure XML has android:id="@+id/txt_email"
        btnLogout = view.findViewById(R.id.btn_logout);  // Ye aapka TextView hai

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefManager = new PrefManager(requireContext());

        // Check if views are null to prevent crash
        if (txtName != null && txtEmail != null && btnLogout != null) {

            if (mAuth.getCurrentUser() != null) {
                loadUserData();
            } else {
                txtName.setText("Guest User");
                txtEmail.setText("");
            }

            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                if (prefManager != null) {
                    prefManager.setLogin(false);
                    prefManager.clear();
                }
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), Splash.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users").document("Passenger").collection("Register").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded() || getActivity() == null) return;
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        String email = snapshot.getString("email");
                        String imageUrl = snapshot.getString("profileImageUrl");

                        if (txtName != null) txtName.setText(name != null ? name : "No Name");
                        if (txtEmail != null) txtEmail.setText(email != null ? email : "No Email");

                        if (imageUrl != null && !imageUrl.isEmpty() && profileImage != null) {
                            Glide.with(requireActivity())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .circleCrop()
                                    .into(profileImage);
                        }
                    }
                });
    }
}


//
