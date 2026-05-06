package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.example.paktrainfoodapp.R;
import com.example.paktrainfoodapp.utils.PrefManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddEditItemDialogFragment extends DialogFragment {

    public interface Listener {
        void onSaved();
    }

    private String restId;
    private MenuItem editingItem;
    private Listener listener;

    private TextInputEditText etName, etDesc, etPrice, etTime, etCategory;
    private ImageView ivImage;
    private Button btnChooseImage, btnSave;
    private Uri imageUri;
    private String base64Image;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<String> imagePickerLauncher;

    public static AddEditItemDialogFragment newInstance(String restId, MenuItem item, Listener listener) {
        AddEditItemDialogFragment f = new AddEditItemDialogFragment();
        f.restId = restId;
        f.editingItem = item;
        f.listener = listener;
        return f;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_restaurant_add_edit_item_dialog, container, false);

        etName = v.findViewById(R.id.et_item_name);
        etDesc = v.findViewById(R.id.et_item_desc);
        etPrice = v.findViewById(R.id.et_item_price);
        etTime = v.findViewById(R.id.et_item_time);
        etCategory = v.findViewById(R.id.et_item_category);
        ivImage = v.findViewById(R.id.imgPreview);
        btnChooseImage = v.findViewById(R.id.btnChooseImage);
        btnSave = v.findViewById(R.id.btn_save_item);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        Glide.with(this).load(uri).into(ivImage);
                    }
                }
        );

        btnChooseImage.setOnClickListener(v1 -> imagePickerLauncher.launch("image/*"));

        if (editingItem != null) {
            etName.setText(editingItem.getName());
            etDesc.setText(editingItem.getDescription());
            etPrice.setText(String.valueOf(editingItem.getPrice()));
            etTime.setText(editingItem.getTime());
            etCategory.setText(editingItem.getCategory());

            if (!TextUtils.isEmpty(editingItem.getImageUrl())) {
                try {
                    byte[] decodedBytes = Base64.decode(editingItem.getImageUrl(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    ivImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        btnSave.setOnClickListener(v12 -> onSaveClicked());
        return v;
    }

    private Bitmap resizeBitmap(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();
        int maxSize = 512;

        if (width <= maxSize && height <= maxSize) return original;

        float ratio = (float) width / height;
        if (ratio > 1) {
            width = maxSize;
            height = (int) (maxSize / ratio);
        } else {
            height = maxSize;
            width = (int) (maxSize * ratio);
        }
        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            bitmap = resizeBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void onSaveClicked() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String time = etTime.getText() != null ? etTime.getText().toString().trim() : "";
        String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(getContext(), "Name and price are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingItem != null && imageUri == null) {
            base64Image = editingItem.getImageUrl();
        } else {
            if (imageUri == null) {
                Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            base64Image = convertImageToBase64(imageUri);
            if (base64Image == null) {
                Toast.makeText(getContext(), "Failed to convert image", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        fetchRestaurantAndSave(name, desc, price, time, category, base64Image);
    }

    private void fetchRestaurantAndSave(String name, String desc, double price, String time, String category, String imageBase64) {
        PrefManager prefManager = new PrefManager(requireContext());
        var ref = new Object() {
            final String[] restaurantCity = {prefManager.getUserCity()};
        };

        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(restId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String restaurantName = doc.getString("restaurantName");
                        if (ref.restaurantCity[0] == null || ref.restaurantCity[0].isEmpty()) {
                            ref.restaurantCity[0] = doc.getString("city");
                        }

                        MenuItem item = new MenuItem(name, desc, imageBase64, price, time, category,
                                restId, restaurantName, ref.restaurantCity[0]);

                        saveMenuItem(item);

                    } else {
                        Toast.makeText(getContext(), "Restaurant not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to get restaurant info: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ✅ Save under: Users → Restaurant → VerifiedRegister → {restaurantId} → MenuItems
    private void saveMenuItem(MenuItem item) {
        if (editingItem != null) {
            String id = editingItem.getId();
            item.setId(id);
            db.collection("Users")
                    .document("Restaurant")
                    .collection("VerifiedRegister")
                    .document(restId)
                    .collection("MenuItems")
                    .document(id)
                    .set(item)
                    .addOnSuccessListener(a -> {
                        if (listener != null) listener.onSaved();
                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            db.collection("Users")
                    .document("Restaurant")
                    .collection("VerifiedRegister")
                    .document(restId)
                    .collection("MenuItems")
                    .add(item)
                    .addOnSuccessListener(ref -> {
                        String id = ref.getId();
                        ref.update("id", id);
                        item.setId(id);
                        if (listener != null) listener.onSaved();
                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}




