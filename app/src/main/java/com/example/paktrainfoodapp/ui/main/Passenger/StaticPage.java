package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;

public class StaticPage extends Fragment {

    private TextView txtPageTitle;
    private TextView txtPageContent;

    public StaticPage() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_static_page, container, false);

        txtPageTitle = view.findViewById(R.id.txtPageTitle);
        txtPageContent = view.findViewById(R.id.txtPageContent);

        if (getArguments() != null) {

            String title = getArguments().getString("title");
            String description = getArguments().getString("description");

            txtPageTitle.setText(title);
            txtPageContent.setText(description);
        }

        return view;
    }
}