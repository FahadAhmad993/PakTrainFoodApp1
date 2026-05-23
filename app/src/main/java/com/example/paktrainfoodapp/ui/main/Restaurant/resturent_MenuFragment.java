package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paktrainfoodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class resturent_MenuFragment extends Fragment implements AddEditItemDialogFragment.Listener {

    private Button btnAddItem;
    private RecyclerView rvMenu;
    private TextView tvEmptyState;

    private MenuAdapter adapter;
    private List<MenuItem> items = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentRestaurantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public resturent_MenuFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_resturent__menu, container, false);

        btnAddItem = v.findViewById(R.id.btnAddItem);
        rvMenu = v.findViewById(R.id.rvMenuItems);
        tvEmptyState = v.findViewById(R.id.tvEmptyState);

        rvMenu.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MenuAdapter(requireContext(), items, new MenuAdapter.OnItemActionListener() {
            @Override
            public void onEdit(MenuItem item) {
                openEditDialog(item);
            }

            @Override
            public void onDelete(MenuItem item) {
                confirmDelete(item);
            }
        });
        rvMenu.setAdapter(adapter);

        btnAddItem.setOnClickListener(view -> openAddDialog());

        loadMenuItems();
        return v;
    }

    private void openAddDialog() {
        AddEditItemDialogFragment dlg = AddEditItemDialogFragment.newInstance(currentRestaurantId, null, this);
        dlg.show(getParentFragmentManager(), "AddItem");
    }

    private void openEditDialog(MenuItem item) {
        AddEditItemDialogFragment dlg = AddEditItemDialogFragment.newInstance(currentRestaurantId, item, this);
        dlg.show(getParentFragmentManager(), "EditItem");
    }

    private void confirmDelete(MenuItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(MenuItem item) {
        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(currentRestaurantId)
                .collection("MenuItems")
                .document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                    loadMenuItems();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadMenuItems() {
        db.collection("Users")
                .document("Restaurant")
                .collection("VerifiedRegister")
                .document(currentRestaurantId)
                .collection("MenuItems")
                .get()
                .addOnSuccessListener(this::onMenuLoaded)
                .addOnFailureListener(e -> {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("No menu items available.");
                });
    }

    private void onMenuLoaded(QuerySnapshot snapshots) {
        items.clear();
        for (QueryDocumentSnapshot d : snapshots) {
            MenuItem item = d.toObject(MenuItem.class);
            item.setId(d.getId());
            items.add(item);
        }
        adapter.notifyDataSetChanged();
        tvEmptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSaved() {
        loadMenuItems();
    }
}




