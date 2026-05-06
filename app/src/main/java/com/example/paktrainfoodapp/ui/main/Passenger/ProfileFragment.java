package com.example.paktrainfoodapp.ui.main.Passenger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.Splash;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView txtName, txtEmail;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passanger_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        profileImage = view.findViewById(R.id.img_profile);
        txtName = view.findViewById(R.id.txt_name);
        txtEmail = view.findViewById(R.id.txt_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        } else {
            txtName.setText("Guest");
            txtEmail.setText("");
        }

        btnLogout.setOnClickListener(v -> {
            if (mAuth != null) mAuth.signOut();

            if (getActivity() != null) {
                getActivity().finish();
            }

            startActivity(new Intent(getContext(), Splash.class));
        });
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();

        // ✅ Updated Firestore path according to new structure
        db.collection("Users")
                .document("Passenger")
                .collection("Register")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        txtName.setText(snapshot.getString("name"));
                        txtEmail.setText(snapshot.getString("email"));

                        // Get Base64 string (image)
                        String base64Image = snapshot.getString("imageBase64");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            Bitmap bitmap = base64ToBitmap(base64Image);
                            if (bitmap != null) {
                                profileImage.setImageBitmap(bitmap);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    txtName.setText("Error loading data");
                    txtEmail.setText("");
                });
    }

    // Convert Base64 string to Bitmap
    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}








