package com.example.sa3id.userActivities;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.example.sa3id.R;

public class CustomAlertDialog extends Dialog {
    private ImageView errorImageView;
    private TextView messageTextView;
    private Button closeButton;

    public CustomAlertDialog(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null);
        setContentView(view);

        // Initialize views
        errorImageView = view.findViewById(R.id.errorImageView);
        messageTextView = view.findViewById(R.id.messageTextView);
        closeButton = view.findViewById(R.id.closeButton);

        // Set close button listener
        closeButton.setOnClickListener(v -> dismiss());

        // Set dialog window attributes
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void show(String message, @DrawableRes int imageResId) {
        messageTextView.setText(message);

        //errorImageView.setImageResource(imageResId);
        Glide.with(getContext())
                .load(imageResId)
                .into(errorImageView);
        show();
    }
}

