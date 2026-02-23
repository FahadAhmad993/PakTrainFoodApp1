package com.example.paktrainfoodapp.ui.main.Delivery;

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

public class DeliveryRegisterFragment extends Fragment {

    private TextInputEditText etRestaurantName, etAddress, etLicenseNo,
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
        return inflater.inflate(R.layout.fragment_delivery_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PrefManager(requireContext());
        db = FirebaseFirestore.getInstance();

        etRestaurantName = view.findViewById(R.id.et_deliverboy_name);
        spinnerCity = view.findViewById(R.id.spinner_deliveryboy_city);
        etAddress = view.findViewById(R.id.et_deliveryboy_address);
        imgLicensePreview = view.findViewById(R.id.img_cnicfront_preview_delivery);
        btnUploadLicense = view.findViewById(R.id.btn_cnic_front);
        etOwnerCnic = view.findViewById(R.id.et_owner_cnic);
        imgCnicPreview = view.findViewById(R.id.img_cnicback_preview_delivery);
        btnUploadCnic = view.findViewById(R.id.btn_cnic_back);
        etEmail = view.findViewById(R.id.et_delivery_email);
        etPhone = view.findViewById(R.id.et_delivery_phone);
        btnRegister = view.findViewById(R.id.btn_delivery_register);

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
        String DeliveryName = getText(etRestaurantName);
        String city = spinnerCity.getSelectedItem().toString();
        String address = getText(etAddress);
        String ownerCnic = getText(etOwnerCnic);
        String email = getText(etEmail);
        String phone = getText(etPhone);

        if (DeliveryName.isEmpty() || address.isEmpty() || ownerCnic.isEmpty() || email.isEmpty() || phone.isEmpty()) {
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

        saveToFirestore(DeliveryName, city, address, ownerCnic, email, phone);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void saveToFirestore(String DeliveryName, String city, String address,
                                  String ownerCnic, String email, String phone) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> delivery = new HashMap<>();
        delivery.put("uid", uid);
        delivery.put("name", DeliveryName);
        delivery.put("city", city);
        delivery.put("address", address);
        delivery.put("ownerCnic", ownerCnic);
        delivery.put("email", email);
        delivery.put("phone", phone);
        delivery.put("role", "Delivery");
        delivery.put("isVerified", true);
        delivery.put("ownerCnicBase64front", licenseBase64);
        delivery.put("ownerCnicBase64back", cnicBase64);

        // ✅ Updated Firestore Path (NEW STRUCTURE)
        db.collection("Users")
                .document("Delivery")
                .collection("VerifiedRegister")
                .document(uid)
                .set(delivery)
                .addOnSuccessListener(unused -> {
                    prefManager.setRegistered(true, email);
                    prefManager.setIsDeliveryVerified(true);
                    prefManager.setUserCity(city);
                    Toast.makeText(getContext(), "Delivery Boy Registered & Verified", Toast.LENGTH_SHORT).show();

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



