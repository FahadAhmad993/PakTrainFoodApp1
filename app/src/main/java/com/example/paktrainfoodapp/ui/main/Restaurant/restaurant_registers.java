package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.MainActivity;
import com.example.paktrainfoodapp.utils.PrefManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class restaurant_registers extends Fragment {

    private TextInputEditText etRestaurantName, etOwnerName, etAddress, etLicenseNo,
            etOwnerCnic, etEmail, etPhone;
    private Spinner spinnerCity;
    private ImageView imgLicensePreview, imgCnicPreview;
    private Button btnUploadLicense, btnUploadCnic, btnRegister;
    private Uri licenseUri, cnicUri;
    private String licenseBase64, cnicBase64;

    private FirebaseFirestore db;
    private PrefManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_registers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PrefManager(requireContext());
        db = FirebaseFirestore.getInstance();

        etRestaurantName = view.findViewById(R.id.et_restaurant_name);
        etOwnerName = view.findViewById(R.id.et_owner_name);
        spinnerCity = view.findViewById(R.id.spinner_city);
        etAddress = view.findViewById(R.id.et_address);
        etLicenseNo = view.findViewById(R.id.et_license_no);
        imgLicensePreview = view.findViewById(R.id.img_license_preview);
        btnUploadLicense = view.findViewById(R.id.btn_upload_license);
        etOwnerCnic = view.findViewById(R.id.et_owner_cnic);
        imgCnicPreview = view.findViewById(R.id.img_cnic_preview);
        btnUploadCnic = view.findViewById(R.id.btn_upload_cnic);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        btnRegister = view.findViewById(R.id.btn_register);

        // Spinner setup
        String[] cities = {"Karachi", "Lahore", "Islamabad", "Multan", "Faisalabad", "Rawalpindi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);

        btnUploadLicense.setOnClickListener(v -> pickLicenseLauncher.launch("image/*"));
        btnUploadCnic.setOnClickListener(v -> pickCnicLauncher.launch("image/*"));
        btnRegister.setOnClickListener(v -> doRegister());
    }

    private final ActivityResultLauncher<String> pickLicenseLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    licenseUri = uri;
                    licenseBase64 = uriToBase64(uri);
                    Glide.with(this).load(uri).into(imgLicensePreview);
                }
            });

    private final ActivityResultLauncher<String> pickCnicLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    cnicUri = uri;
                    cnicBase64 = uriToBase64(uri);
                    Glide.with(this).load(uri).into(imgCnicPreview);
                }
            });

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Image conversion error", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void doRegister() {
        String restName = getText(etRestaurantName);
        String ownerName = getText(etOwnerName);
        String city = spinnerCity.getSelectedItem().toString();
        String address = getText(etAddress);
        String licenseNo = getText(etLicenseNo);
        String ownerCnic = getText(etOwnerCnic);
        String email = getText(etEmail);
        String phone = getText(etPhone);

        if (restName.isEmpty() || ownerName.isEmpty() || address.isEmpty() ||
                licenseNo.isEmpty() || ownerCnic.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Invalid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (licenseBase64 == null || cnicBase64 == null) {
            Toast.makeText(getContext(), "Upload both images", Toast.LENGTH_SHORT).show();
            return;
        }

        saveToFirestore(restName, ownerName, city, address, licenseNo, ownerCnic, email, phone);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void saveToFirestore(String restName, String ownerName, String city, String address,
                                 String licenseNo, String ownerCnic, String email, String phone) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("uid", uid);
        restaurant.put("restaurantName", restName);
        restaurant.put("ownerName", ownerName);
        restaurant.put("city", city);
        restaurant.put("address", address);
        restaurant.put("licenseNo", licenseNo);
        restaurant.put("ownerCnic", ownerCnic);
        restaurant.put("email", email);
        restaurant.put("phone", phone);
        restaurant.put("role", "RESTAURANT");
        restaurant.put("isVerified", true);
        restaurant.put("licenseBase64", licenseBase64);
        restaurant.put("ownerCnicBase64", cnicBase64);

        // ✅ Updated Firestore Path (NEW STRUCTURE)
        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(uid)
                .set(restaurant)
                .addOnSuccessListener(unused -> {
                    prefManager.setRegistered(true, email);
                    prefManager.setIsRestaurantVerified(true);
                    prefManager.setUserCity(city);
                    Toast.makeText(getContext(), "Restaurant Registered & Verified", Toast.LENGTH_SHORT).show();

                    // ⚙️ Delay before moving to MainActivity
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded()) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    }, 300);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}



