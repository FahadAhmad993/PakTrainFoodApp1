package com.example.paktrainfoodapp.ui.auth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterFragment extends Fragment {

    private CircleImageView profileImage;
    private TextInputEditText edtName, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private Button btnChooseImage, btnRegister;
    private TextView txtGoLogin;

    private Uri imageUri = null;
    private String imageBase64 = null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String userRole = "PASSENGER"; // default

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    Glide.with(this).load(uri).into(profileImage);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImage = view.findViewById(R.id.profile_image);
        edtName = view.findViewById(R.id.edit_text_name);
        edtEmail = view.findViewById(R.id.edit_text_email);
        edtPhone = view.findViewById(R.id.edit_text_phone);
        edtPassword = view.findViewById(R.id.edit_text_password);
        edtConfirmPassword = view.findViewById(R.id.edit_text_confirm_password);
        btnChooseImage = view.findViewById(R.id.button_choose_image);
        btnRegister = view.findViewById(R.id.button_register);
        txtGoLogin = view.findViewById(R.id.text_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null && getArguments().containsKey(AuthActivity.USER_ROLE_KEY)) {
            userRole = getArguments().getString(AuthActivity.USER_ROLE_KEY);
        }

        btnChooseImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnRegister.setOnClickListener(v -> doRegister());
        txtGoLogin.setOnClickListener(v -> goToLogin());
    }

    private void goToLogin() {
        Bundle bundle = new Bundle();
        bundle.putString(AuthActivity.USER_ROLE_KEY, userRole);

        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        ((AuthActivity) requireActivity()).loadFragment(fragment);
    }

    private void doRegister() {
        String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String phone = edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
        String confirmPassword = edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Invalid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(getContext(), "Please select profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            imageBase64 = convertImageToBase64(imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error converting image", Toast.LENGTH_SHORT).show();
            return;
        }

        createFirebaseUser(name, email, phone, password, imageBase64);
    }

    private String convertImageToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void createFirebaseUser(String name, String email, String phone, String password, String imageBase64) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("uid", uid);
                        user.put("name", name);
                        user.put("email", email);
                        user.put("phone", phone);
                        user.put("imageBase64", imageBase64);
                        user.put("role", userRole);

                        // Firestore structure
                        String parentCollection;
                        if (userRole.equals("PASSENGER"))
                            parentCollection = "Passenger";
                        else if (userRole.equals("RESTAURANT"))
                            parentCollection = "Restaurant";
                        else
                            parentCollection = "Delivery";

                        db.collection("Users")
                                .document(parentCollection)
                                .collection("Register")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Registered successfully as " + userRole, Toast.LENGTH_SHORT).show();
                                    goToLogin();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "Auth failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}



