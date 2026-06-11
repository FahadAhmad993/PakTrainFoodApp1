package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.app.ProgressDialog;
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

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.MainActivity;
import com.example.paktrainfoodapp.utils.PrefManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class restaurant_registers extends Fragment {

    // 🔥 APNI IMGBB API KEY YAHAN PASTE KAREIN
    private static final String IMGBB_API_KEY = "48a067c9d290ecbca102dca44184ae22";

    private TextInputEditText etRestaurantName, etOwnerName, etAddress, etLicenseNo, etOwnerCnic, etEmail, etPhone;
    private Spinner spinnerCity;
    private ImageView imgLicensePreview, imgCnicPreview;
    private Button btnUploadLicense, btnUploadCnic, btnRegister, btnBack;
    private String licenseBase64, cnicBase64;

    // ImgBB online image URLs store karne k liye variables
    private String licenseUrl = "";
    private String cnicUrl = "";

    private FirebaseFirestore db;
    private PrefManager prefManager;
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_registers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PrefManager(requireContext());
        db = FirebaseFirestore.getInstance();
        requestQueue = Volley.newRequestQueue(requireContext());

        // Background loading k liye custom Progress Dialog setup
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading images & registering restaurant... Please wait.");
        progressDialog.setCancelable(false);

        // Binding Views
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

        // Spinner Setup
        String[] cities = {"Karachi", "Lahore", "Islamabad", "Multan", "Faisalabad", "Rawalpindi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);

        // Click Listeners
        btnUploadLicense.setOnClickListener(v -> pickLicenseLauncher.launch("image/*"));
        btnUploadCnic.setOnClickListener(v -> pickCnicLauncher.launch("image/*"));
        btnRegister.setOnClickListener(v -> checkValidationAndStartSequence());

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> showExitConfirmationDialog());
        }

        // 📱 Device Hardware Back Button Listener
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Registration?")
                .setMessage("\"Your verification is not complete yet. Going back will sign you out. Do you still want to go back?\"")
                .setPositiveButton("Yes, Exit", (dialog, which) -> performSignOutAndGoBack())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void performSignOutAndGoBack() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        prefManager.setLogin(false);
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }

    private final ActivityResultLauncher<String> pickLicenseLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    licenseBase64 = uriToBase64(uri);
                    Glide.with(this).load(uri).into(imgLicensePreview);
                }
            });

    private final ActivityResultLauncher<String> pickCnicLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    cnicBase64 = uriToBase64(uri);
                    Glide.with(this).load(uri).into(imgCnicPreview);
                }
            });

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 🔄 40% compressions taake network par data tezi se transfer ho
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Image conversion error", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void checkValidationAndStartSequence() {
        String restName = getText(etRestaurantName);
        String ownerName = getText(etOwnerName);
        String address = getText(etAddress);
        String licenseNo = getText(etLicenseNo);
        String ownerCnic = getText(etOwnerCnic);
        String email = getText(etEmail);
        String phone = getText(etPhone);

        if (restName.isEmpty() || ownerName.isEmpty() || address.isEmpty() || licenseNo.isEmpty() || ownerCnic.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (licenseBase64 == null || cnicBase64 == null) {
            Toast.makeText(getContext(), "Please upload both License and CNIC images", Toast.LENGTH_SHORT).show();
            return;
        }

        // Loader start karein aur upload sequence chalaein
        progressDialog.show();
        uploadLicenseToImgBB();
    }

    // 🚀 STEP 1: License Image Upload (Volley POST)
    private void uploadLicenseToImgBB() {
        String url = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        licenseUrl = jsonObject.getJSONObject("data").getString("url");

                        // Pehli image kamyab rahi, ab doosri shuru karein
                        uploadCnicToImgBB();
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "License URL parsing failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "License upload failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image", licenseBase64);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    // 🚀 STEP 2: CNIC Image Upload (Volley POST)
    private void uploadCnicToImgBB() {
        String url = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        cnicUrl = jsonObject.getJSONObject("data").getString("url");

                        // Dono images upload ho chuki hain, ab final data Firestore me daalein
                        saveToFirestore();
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "CNIC URL parsing failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "CNIC upload failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image", cnicBase64);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    // 🚀 STEP 3: Save clean data along with online image URLs to Firestore
    private void saveToFirestore() {
        String restName = getText(etRestaurantName);
        String ownerName = getText(etOwnerName);
        String city = spinnerCity.getSelectedItem().toString();
        String address = getText(etAddress);
        String licenseNo = getText(etLicenseNo);
        String ownerCnic = getText(etOwnerCnic);
        String email = getText(etEmail);
        String phone = getText(etPhone);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            performSignOutAndGoBack();
            return;
        }

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
        restaurant.put("isVerified", false);

        // Online image URLs from ImgBB
        restaurant.put("licenseImageUrl", licenseUrl);
        restaurant.put("ownerCnicImageUrl", cnicUrl);

        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(uid)
                .set(restaurant)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    prefManager.setRegistered(true, email);
                    prefManager.setIsRestaurantVerified(false);
                    prefManager.setUserCity(city);

                    Toast.makeText(getContext(), "Application Submitted! Waiting for Admin Approval.", Toast.LENGTH_LONG).show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded()) {
                            performSignOutAndGoBack();
                        }
                    }, 1500);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ⚡ Wapas add kiya gaya missing function jo error de raha tha
    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}