package com.example.paktrainfoodapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.ui.main.MainActivity;
import com.example.paktrainfoodapp.ui.main.Delivery.DeliveryRegisterFragment;
import com.example.paktrainfoodapp.ui.main.Restaurant.restaurant_registers;
import com.example.paktrainfoodapp.utils.PrefManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtForgotPassword, txtGoRegister;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String selectedRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtEmail = view.findViewById(R.id.edit_text_email);
        edtPassword = view.findViewById(R.id.edit_text_password);
        btnLogin = view.findViewById(R.id.button_login);
        txtForgotPassword = view.findViewById(R.id.text_forgot_password);
        txtGoRegister = view.findViewById(R.id.text_register);
        TextView title = view.findViewById(R.id.textViewTitle);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        selectedRole = getArguments() != null
                ? getArguments().getString(AuthActivity.USER_ROLE_KEY, "PASSENGER")
                : "PASSENGER";

        // Set dynamic title
        switch (selectedRole) {
            case "PASSENGER":
                title.setText("Passenger Login");
                break;
            case "RESTAURANT":
                title.setText("Restaurant Login");
                break;
            case "DELIVERY":
                title.setText("Delivery Login");
                break;
        }

        btnLogin.setOnClickListener(v -> doLogin());
        txtForgotPassword.setOnClickListener(v -> sendResetPassword());
        txtGoRegister.setOnClickListener(v -> openRegisterFragment());
    }

    // ---------------- LOGIN FUNCTION ---------------- //
    private void doLogin() {
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        PrefManager pref = new PrefManager(requireContext());

                        pref.setLogin(true);
                        pref.setUserRole(selectedRole);
                        pref.setUserEmail(email);

                        // Check registration in Firestore
                        checkUserRegistration(uid, email);
                    } else {
                        Toast.makeText(getContext(),
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- CHECK REGISTRATION ---------------- //
    private void checkUserRegistration(String uid, String email) {
        String collectionPath = "Users";
        String roleDoc;
        String subCollection = "Register";

        switch (selectedRole) {
            case "PASSENGER":
                roleDoc = "Passenger";
                break;
            case "RESTAURANT":
                roleDoc = "Restaurant";
                subCollection = "VerifiedRegister";
                break;
            case "DELIVERY":
                roleDoc = "Delivery";
                subCollection = "VerifiedRegister";
                break;
            default:
                roleDoc = "Passenger";
        }

        db.collection(collectionPath)
                .document(roleDoc)
                .collection(subCollection)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> handleUserCheck(doc, uid, email, roleDoc))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error checking user: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void handleUserCheck(DocumentSnapshot doc, String uid, String email, String roleDoc) {
        PrefManager pref = new PrefManager(requireContext());

        if (doc.exists()) {
            String name = doc.getString("name");
            pref.setRegistered(true, email);
            pref.setUserName(name);
            pref.setUserRole(roleDoc.toUpperCase());
            goToMainActivity();
        } else {
            // Open additional registration form for Restaurant or Delivery
            if (selectedRole.equals("RESTAURANT")) {
                openRestaurantRegisterForm(uid, email);
            } else if (selectedRole.equals("DELIVERY")) {
                openDeliveryRegisterForm(uid, email);
            } else {
                Toast.makeText(getContext(), "User not found. Please register first.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ---------------- PASSWORD RESET ---------------- //
    private void sendResetPassword() {
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- NAVIGATION ---------------- //
    private void openRegisterFragment() {
        Fragment fragment = new RegisterFragment();
        Bundle b = new Bundle();
        b.putString(AuthActivity.USER_ROLE_KEY, selectedRole);
        fragment.setArguments(b);
        ((AuthActivity) requireActivity()).loadFragment(fragment);
    }

    private void goToMainActivity() {
        PrefManager pref = new PrefManager(requireContext());
        pref.setLogin(true);
        pref.setRegistered(true, pref.getUserEmail());

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(AuthActivity.USER_ROLE_KEY, selectedRole);
        startActivity(intent);
        requireActivity().finish();
    }

    private void openRestaurantRegisterForm(String uid, String email) {
        restaurant_registers fragment = new restaurant_registers();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        args.putString("email", email);
        fragment.setArguments(args);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                ((AuthActivity) requireActivity()).loadFragment(fragment);
            }
        }, 300);
    }

    private void openDeliveryRegisterForm(String uid, String email) {
        DeliveryRegisterFragment fragment = new DeliveryRegisterFragment();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        args.putString("email", email);
        fragment.setArguments(args);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                ((AuthActivity) requireActivity()).loadFragment(fragment);
            }
        }, 300);
    }
}





//package com.example.paktrainfoodapp.ui.auth;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.example.paktrainfoodapp.R;
//import com.example.paktrainfoodapp.ui.main.MainActivity;
//import com.example.paktrainfoodapp.ui.main.Restaurant.restaurant_registers;
//import com.example.paktrainfoodapp.utils.PrefManager;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class LoginFragment extends Fragment {
//
//    private TextInputEditText edtEmail, edtPassword;
//    private Button btnLogin;
//    private TextView txtForgotPassword, txtGoRegister;
//    private FirebaseAuth auth;
//    private FirebaseFirestore db;
//    private String selectedRole;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_auth_login, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view,
//                              @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        edtEmail = view.findViewById(R.id.edit_text_email);
//        edtPassword = view.findViewById(R.id.edit_text_password);
//        btnLogin = view.findViewById(R.id.button_login);
//        txtForgotPassword = view.findViewById(R.id.text_forgot_password);
//        txtGoRegister = view.findViewById(R.id.text_register);
//        TextView title = view.findViewById(R.id.textViewTitle);
//
//        auth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//
//        selectedRole = getArguments() != null
//                ? getArguments().getString(AuthActivity.USER_ROLE_KEY, "PASSENGER")
//                : "PASSENGER";
//
//        // Dynamic title
//        switch (selectedRole) {
//            case "PASSENGER":
//                title.setText("Passenger Login");
//                break;
//            case "RESTAURANT":
//                title.setText("Restaurant Login");
//                break;
//            case "DELIVERY":
//                title.setText("Delivery Login");
//                break;
//        }
//
//        btnLogin.setOnClickListener(v -> doLogin());
//        txtForgotPassword.setOnClickListener(v -> sendResetPassword());
//        txtGoRegister.setOnClickListener(v -> openRegisterFragment());
//    }
//
//    // ---------------- LOGIN FUNCTION ---------------- //
//    private void doLogin() {
//        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
//        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
//
//        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
//            Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        String uid = auth.getCurrentUser().getUid();
//                        PrefManager pref = new PrefManager(requireContext());
//
//                        pref.setLogin(true);
//                        pref.setUserRole(selectedRole);
//                        pref.setUserEmail(email);
//
//                        // Correct role-based registration check
//                        checkUserRegistration(uid, email);
//                    } else {
//                        Toast.makeText(getContext(),
//                                "Login failed: " + task.getException().getMessage(),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    // ---------------- CHECK REGISTRATION IN FIRESTORE ---------------- //
//    private void checkUserRegistration(String uid, String email) {
//        String collectionPath = "Users";
//        String roleDoc;
//        String subCollection = "Register";
//
//        switch (selectedRole) {
//            case "PASSENGER":
//                roleDoc = "Passenger";
//                break;
//            case "RESTAURANT":
//                roleDoc = "Restaurant";
//                subCollection = "VerifiedRegister"; // check verified data first
//                break;
//            case "DELIVERY":
//                roleDoc = "Delivery";
//                break;
//            default:
//                roleDoc = "Passenger";
//        }
//
//        db.collection(collectionPath)
//                .document(roleDoc)
//                .collection(subCollection)
//                .document(uid)
//                .get()
//                .addOnSuccessListener(doc -> handleUserCheck(doc, uid, email, roleDoc))
//                .addOnFailureListener(e ->
//                        Toast.makeText(getContext(),
//                                "Error checking user: " + e.getMessage(),
//                                Toast.LENGTH_SHORT).show());
//    }
//
//    private void handleUserCheck(DocumentSnapshot doc, String uid, String email, String roleDoc) {
//        PrefManager pref = new PrefManager(requireContext());
//
//        if (doc.exists()) {
//            String name = doc.getString("name");
//
//            pref.setRegistered(true, email);
//            pref.setUserName(name);
//            pref.setUserRole(roleDoc.toUpperCase());
//            goToMainActivity();
//        } else {
//            // For restaurants only, open additional registration form
//            if (selectedRole.equals("RESTAURANT")) {
//                openRestaurantRegisterForm(uid, email);
//            } else {
//                Toast.makeText(getContext(), "User not found. Please register first.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    // ---------------- PASSWORD RESET ---------------- //
//    private void sendResetPassword() {
//        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
//        if (TextUtils.isEmpty(email)) {
//            Toast.makeText(getContext(), "Enter your email first", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        auth.sendPasswordResetEmail(email)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getContext(),
//                                "Error: " + task.getException().getMessage(),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    // ---------------- NAVIGATION ---------------- //
//    private void openRegisterFragment() {
//        Fragment fragment = new RegisterFragment();
//        Bundle b = new Bundle();
//        b.putString(AuthActivity.USER_ROLE_KEY, selectedRole);
//        fragment.setArguments(b);
//        ((AuthActivity) requireActivity()).loadFragment(fragment);
//    }
//
//    private void goToMainActivity() {
//        PrefManager pref = new PrefManager(requireContext());
//        pref.setLogin(true);
//        pref.setRegistered(true, pref.getUserEmail());
//
//        Intent intent = new Intent(getActivity(), MainActivity.class);
//        intent.putExtra(AuthActivity.USER_ROLE_KEY, selectedRole);
//        startActivity(intent);
//        requireActivity().finish();
//    }
//
//    private void openRestaurantRegisterForm(String uid, String email) {
//        restaurant_registers fragment = new restaurant_registers();
//        Bundle args = new Bundle();
//        args.putString("uid", uid);
//        args.putString("email", email);
//        fragment.setArguments(args);
//
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            if (isAdded()) {
//                ((AuthActivity) requireActivity()).loadFragment(fragment);
//            }
//        }, 300);
//    }
//}
//
//
//
//
