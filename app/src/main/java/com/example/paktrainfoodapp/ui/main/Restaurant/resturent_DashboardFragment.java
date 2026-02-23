package com.example.paktrainfoodapp.ui.main.Restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.paktrainfoodapp.R;

public class resturent_DashboardFragment extends Fragment {

    private LinearLayout btnProfile, btnMenu, btnDelivery;
   ImageView iconProfile;
    private TextView textProfile;

    public resturent_DashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resturent__dashboard, container, false);

        btnProfile = view.findViewById(R.id.btnProfile);
        btnMenu = view.findViewById(R.id.btnMenu);
        btnDelivery = view.findViewById(R.id.btnDelivery);


        btnProfile.setOnClickListener(v ->{
                openFragment(new resturent_ProfileFragment());
        });

        btnMenu.setOnClickListener(v ->
                Toast.makeText(getContext(), "Opening Menu Management...", Toast.LENGTH_SHORT).show());

        btnDelivery.setOnClickListener(v ->
                Toast.makeText(getContext(), "Opening Delivery Boys Section...", Toast.LENGTH_SHORT).show());

        return view;
    }
    private void openFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(null) // optional, back button se wapas jana ho to
                .commit();
    }



}
