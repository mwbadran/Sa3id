package com.example.sa3id.userActivities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.google.android.material.button.MaterialButton;

public class AnnouncementViewActivity extends BaseActivity {

    private Intent comeIntent;
    private String title, description;
    private int imageResource;

    private TextView tvTitle, tvDescription;
    private ImageView announcementImage;
    private MaterialButton btnWhatsAppShare, btnTelegramShare, btnGeneralShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        comeIntent = getIntent();
        title = comeIntent.getStringExtra("title");
        description = comeIntent.getStringExtra("description");
        imageResource = Integer.parseInt(comeIntent.getStringExtra("imageResource"));

        initViews();
        setDataToViews();
        setupClickListeners();
    }

    @Override
    protected boolean handleChildBackPress() {
        super.handleChildBackPress();
        finish();
        return true;
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        announcementImage = findViewById(R.id.announcementImage);
        btnWhatsAppShare = findViewById(R.id.btnWhatsAppShare);
        btnTelegramShare = findViewById(R.id.btnTelegramShare);
        btnGeneralShare = findViewById(R.id.btnGeneralShare);
    }

    private void setDataToViews() {
        tvTitle.setText(title);
        tvDescription.setText(description);
        announcementImage.setImageResource(imageResource);
    }

    private void setupClickListeners() {
        // Image click listener for expansion
        announcementImage.setOnClickListener(v -> showExpandedImage());

        // Share buttons click listeners
        btnWhatsAppShare.setOnClickListener(v -> shareToWhatsApp());
        btnTelegramShare.setOnClickListener(v -> shareToTelegram());
        btnGeneralShare.setOnClickListener(v -> shareGeneral());
    }

    private void showExpandedImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expanded_image, null);
        ImageView expandedImage = dialogView.findViewById(R.id.expandedImage);
        expandedImage.setImageResource(imageResource);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Close dialog when clicking the image
        expandedImage.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void shareToWhatsApp() {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + description);
        startActivity(whatsappIntent);
    }

    private void shareToTelegram() {
        Intent telegramIntent = new Intent(Intent.ACTION_SEND);
        telegramIntent.setType("text/plain");
        telegramIntent.setPackage("org.telegram.messenger");
        telegramIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + description);
        startActivity(telegramIntent);
    }

    private void shareGeneral() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + description);
        startActivity(Intent.createChooser(shareIntent, "مشاركة عبر"));
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_announcement_view;
    }
}