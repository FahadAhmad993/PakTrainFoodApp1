package com.example.paktrainfoodapp.ui.main.Passenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private ImageView btnEditProfile;

    private TextView txtName;
    private TextView txtEmail;
    private TextView btnLogout;

    private LinearLayout layoutHelpSupport;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String> galleryLauncher;

    public ProfileFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_passanger_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Initialize Views

        profileImage = view.findViewById(R.id.img_profile);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        txtName = view.findViewById(R.id.txt_name);
        txtEmail = view.findViewById(R.id.txt_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        layoutHelpSupport = view.findViewById(R.id.layoutHelpSupport);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Gallery Launcher

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {

                        Glide.with(requireActivity())
                                .load(uri)
                                .circleCrop()
                                .into(profileImage);

                        Toast.makeText(requireContext(),
                                "Profile Image Changed",
                                Toast.LENGTH_SHORT).show();
                    }

                });

        // Change Profile Image

        btnEditProfile.setOnClickListener(v ->
                galleryLauncher.launch("image/*"));
        // ==========================
        // Load User Data
        // ==========================

        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        } else {
            txtName.setText("Guest User");
            txtEmail.setText("");
        }

        // ==========================
        // Help & Support
        // ==========================

        layoutHelpSupport.setOnClickListener(v -> {

            Fragment parent = getParentFragment();

            if (parent instanceof Passenger_Fragment_Loader) {
                ((Passenger_Fragment_Loader) parent).openHelpSupport();
            }

        });

        // ==========================
        // Logout
        // ==========================

        btnLogout.setOnClickListener(v -> {

            mAuth.signOut();

            Intent intent = new Intent(requireActivity(), Splash.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            requireActivity().finish();

        });

    }

    // ==========================
    // Load User Data From Firebase
    // ==========================

    private void loadUserData() {

        if (mAuth.getCurrentUser() == null)
            return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users")
                .document("Passenger")
                .collection("Register")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!isAdded())
                        return;

                    if (snapshot.exists()) {

                        String name = snapshot.getString("name");
                        String email = snapshot.getString("email");
                        String imageUrl = snapshot.getString("profileImageUrl");

                        txtName.setText(name != null ? name : "No Name");
                        txtEmail.setText(email != null ? email : "No Email");
                        if (imageUrl != null && !imageUrl.isEmpty()) {

                            Glide.with(requireActivity())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.edit_info)
                                    .circleCrop()
                                    .into(profileImage);

                        } else {

                            profileImage.setImageResource(R.drawable.edit_info);

                        }

                    }

                })
                .addOnFailureListener(e ->

                        Toast.makeText(requireContext(),
                                "Failed to load profile",
                                Toast.LENGTH_SHORT).show()

                );

    }

}
/*
package com.example.paktrainfoodapp.ui.main.Passenger;

 import android.os.Build;
 import android.os.ProfilingManager;
 import android.widget.LinearLayout;
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

 import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.firestore.FirebaseFirestore;


 public class ProfileFragment extends Fragment {

     public final ProfileFragment.PrefManager PrefManager;
     private ImageView profileImage;
     private ImageView btnEditProfile; // Pencil button declaration
     private TextView txtName, txtEmail, btnLogout;
     private LinearLayout layoutHelpSupport;

     private FirebaseAuth mAuth;
     private FirebaseFirestore db;
     private com.example.paktrainfoodapp.ui.main.Passenger.PrefManager prefManager;

     // Gallery se image select karne k liye launcher
     private ActivityResultLauncher<String> galleryLauncher;

     public ProfileFragment() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
             PrefManager = new PrefManager();
     }

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

         // UI Components Initialize
         profileImage = view.findViewById(R.id.img_profile);
         btnEditProfile = view.findViewById(R.id.btn_edit_profile); // Initialized pencil icon
         txtName = view.findViewById(R.id.txt_name);
         txtEmail = view.findViewById(R.id.txt_email);
         btnLogout = view.findViewById(R.id.btn_logout);
         layoutHelpSupport = view.findViewById(R.id.layoutHelpSupport);
         mAuth = FirebaseAuth.getInstance();
         db = FirebaseFirestore.getInstance();

         // 1. Gallery Result Launcher Setup
         galleryLauncher = registerForActivityResult(
                 new ActivityResultContracts.GetContent(),
                 new ActivityResultCallback<Uri>() {
                     @Override
                     public void onActivityResult(Uri uri) {
                         if (uri != null && isAdded() && getActivity() != null) {
                             // Selected image ko usi waqt screen pr view karwane k liye
                             Glide.with(requireActivity())
                                     .load(uri)
                                     .circleCrop()
                                     .into(profileImage);

                             // Yahan click hone k bad agar bad me Firebase Storage
                             // me push krna ho to code add kiya ja skta ha.
                             Toast.makeText(getContext(), "Image Changed!", Toast.LENGTH_SHORT).show();
                         }
                     }
                 }
         );

         // 2. Pencil button click handle kiya
         if (btnEditProfile != null) {
             btnEditProfile.setOnClickListener(v -> {
                 galleryLauncher.launch("image/*"); // Gallery open karega
             });
         }

         // Baqi aapka original unchanged logic code
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
                 }
                 if (getActivity() != null) {
                     Intent intent = new Intent(getActivity(), Splash.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                     startActivity(intent);
                     getActivity().finish();
                 }
             });
             if (layoutHelpSupport != null) {

                 layoutHelpSupport.setOnClickListener(v -> {

                     Fragment parent = getParentFragment();

                     if (parent instanceof Passenger_Fragment_Loader) {
                         ((Passenger_Fragment_Loader) parent).openHelpSupport();
                     }

                 });

             }
             }
         }

     private void loadUserData() {
         if (mAuth.getCurrentUser() == null) {
             return;
         }
         String uid = mAuth.getCurrentUser().getUid();

         db.collection("Users").document("Passenger").collection("Register").document(uid)
                 .get()
                 .addOnSuccessListener(snapshot -> {
                     if (!isAdded() || getActivity() == null) {
                         return;
                     }
                     if (snapshot.exists()) {
                         String name = snapshot.getString("name");
                         String email = snapshot.getString("email");
                         String imageUrl = snapshot.getString("profileImageUrl");

                         if (txtName != null) {
                             txtName.setText(name != null ? name : "No Name");
                         }
                         if (txtEmail != null) {
                             txtEmail.setText(email != null ? email : "No Email");
                         }

                         if (imageUrl != null && !imageUrl.isEmpty() && profileImage != null) {
                             Glide.with(equals())
                                     .load(imageUrl)
                                     .placeholder(R.drawable.edit_info)
                                     .circleCrop()
                                     .into(profileImage);
                         }
                     }
                 });
     }

     private View equals() {
         return null;
     }

     private class PrefManager {
     }
 }
*/

