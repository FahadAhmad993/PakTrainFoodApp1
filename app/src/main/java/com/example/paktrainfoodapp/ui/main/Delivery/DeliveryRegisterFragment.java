package com.example.paktrainfoodapp.ui.main.Delivery;

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

public class DeliveryRegisterFragment extends Fragment {

    // 🔥 APNI IMGBB API KEY YAHAN PASTE KAREIN
    private static final String IMGBB_API_KEY = "48a067c9d290ecbca102dca44184ae22";

    private TextInputEditText etRestaurantName, etAddress, etOwnerCnic, etEmail, etPhone;
    private Spinner spinnerCity;
    private ImageView imgLicensePreview, imgCnicPreview;
    private Button btnUploadLicense, btnUploadCnic, btnRegister;
    private String licenseBase64, cnicBase64;

    // Links storage for ImgBB URLs
    private String cnicFrontUrl = "";
    private String cnicBackUrl = "";

    private FirebaseFirestore db;
    private PrefManager prefManager;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PrefManager(requireContext());
        db = FirebaseFirestore.getInstance();
        requestQueue = Volley.newRequestQueue(requireContext());

        // Progress Dialog Init
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading documents & Registering... Please wait.");
        progressDialog.setCancelable(false);

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

        String[] cities = {"Karachi", "Lahore", "Islamabad", "Multan", "Faisalabad", "Rawalpindi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);

        btnUploadLicense.setOnClickListener(v -> pickLicenseLauncher.launch("image/*"));
        btnUploadCnic.setOnClickListener(v -> pickCnicLauncher.launch("image/*"));

        // Register click leads to ImgBB upload sequence first
        btnRegister.setOnClickListener(v -> startRegistrationSequence());

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
                .setMessage("Your verification is not complete yet. Going back will sign you out. Do you still want to go back?")
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
            // 🔄 Quality 40% kar di taake ImgBB par instant upload ho jaye speed se
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startRegistrationSequence() {
        String deliveryName = getText(etRestaurantName);
        String address = getText(etAddress);
        String ownerCnic = getText(etOwnerCnic);
        String email = getText(etEmail);
        String phone = getText(etPhone);

        if (deliveryName.isEmpty() || address.isEmpty() || ownerCnic.isEmpty() || email.isEmpty() || phone.isEmpty()) {
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

        // Sab validation paas ho gayi, ab upload loader show karein
        progressDialog.show();
        uploadCnicFrontImage();
    }

    // 🚀 STEP A: Upload First Image to ImgBB
    private void uploadCnicFrontImage() {
        String url = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        cnicFrontUrl = jsonObject.getJSONObject("data").getString("url");

                        // Pehli image ho gayi, ab doosri shuru karein
                        uploadCnicBackImage();
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Front Image Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Front Image Upload Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image", licenseBase64); // Pass Base64 string here
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    // 🚀 STEP B: Upload Second Image to ImgBB
    private void uploadCnicBackImage() {
        String url = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        cnicBackUrl = jsonObject.getJSONObject("data").getString("url");

                        // Dono images upload ho gayiin, ab final Firestore entry karein
                        saveToFirestore();
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Back Image Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Back Image Upload Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    // 🚀 STEP C: Save Small URL Strings into Firestore Document
    private void saveToFirestore() {
        String deliveryName = getText(etRestaurantName);
        String city = spinnerCity.getSelectedItem().toString();
        String address = getText(etAddress);
        String ownerCnic = getText(etOwnerCnic);
        String email = getText(etEmail);
        String phone = getText(etPhone);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Session expired.", Toast.LENGTH_SHORT).show();
            performSignOutAndGoBack();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> delivery = new HashMap<>();
        delivery.put("uid", uid);
        delivery.put("name", deliveryName);
        delivery.put("city", city);
        delivery.put("address", address);
        delivery.put("ownerCnic", ownerCnic);
        delivery.put("email", email);
        delivery.put("phone", phone);
        delivery.put("role", "Delivery");
        delivery.put("isVerified", true);

        // 🔥 AB BASE64 TEXT NAHI, BALKAY CHOTAY ONLINE LINKS SAVE HO RAHAY HAIN!
        delivery.put("ownerCnicUrlfront", cnicFrontUrl);
        delivery.put("ownerCnicUrlback", cnicBackUrl);

        db.collection("Users")
                .document("Delivery")
                .collection("VerifiedRegister")
                .document(uid)
                .set(delivery)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    prefManager.setRegistered(true, email);
                    prefManager.setIsDeliveryVerified(true);
                    prefManager.setUserCity(city);
                    Toast.makeText(getContext(), "Delivery Boy Registered Successfully", Toast.LENGTH_SHORT).show();

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
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}





