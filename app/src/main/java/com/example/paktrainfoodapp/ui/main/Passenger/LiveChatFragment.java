package com.example.paktrainfoodapp.ui.main.Passenger;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.paktrainfoodapp.R;

public class LiveChatFragment extends Fragment {

    private LinearLayout chatContainer;
    private EditText etMessage;
    private Button btnSend;

    public LiveChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_live_chat, container, false);

        chatContainer = view.findViewById(R.id.chatContainer);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {

            String message = etMessage.getText().toString().trim();

            if (!TextUtils.isEmpty(message)) {

                TextView tv = new TextView(requireContext());

                tv.setText("You : " + message);
                tv.setTextSize(16);
                tv.setPadding(24, 24, 24, 24);
                tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                chatContainer.addView(tv);
                // Auto Reply
                TextView reply = new TextView(requireContext());

                reply.setText("Support : Thank you for contacting Pak Train Food.\nWe will respond shortly.");
                reply.setTextSize(16);
                reply.setPadding(24,24,24,24);
                reply.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                chatContainer.addView(reply);

                etMessage.setText("");
            }

        });
        return view;

    }
}