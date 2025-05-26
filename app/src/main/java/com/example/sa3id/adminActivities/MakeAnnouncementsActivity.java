package com.example.sa3id.adminActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.example.sa3id.models.Announcement;
import com.example.sa3id.userActivities.AnnouncementViewActivity;
import com.example.sa3id.userActivities.CustomAlertDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class MakeAnnouncementsActivity extends BaseActivity {
    private TextInputLayout titleLayout, descriptionLayout;
    private TextInputEditText etTitle, etDescription;
    private ImageView ivSelectedImage;
    private Button btnSelectImage, btnPreview, btnPublish;
    private int selectedImageResource = R.drawable.ic_image; // Default image
    private SharedPreferences sharedPreferences;
    private ArrayList<Announcement> announcements;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initViews();
        setupImagePicker();
        loadExistingAnnouncements();
        setupClickListeners();
    }

    private void initViews() {
        titleLayout = findViewById(R.id.titleLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnPreview = findViewById(R.id.btnPreview);
        btnPublish = findViewById(R.id.btnPublish);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivSelectedImage.setImageURI(uri);
                    // For now, we'll keep using drawable resources
                    // In a real app, you'd want to save this image to storage
                    selectedImageResource = R.drawable.ic_image;
                }
            }
        );
    }

    private void loadExistingAnnouncements() {
        sharedPreferences = getSharedPreferences("AnnouncementsSP", MODE_PRIVATE);
        announcements = new ArrayList<>();
        
        int count = sharedPreferences.getInt("announcementCount", 0);
        for (int i = 0; i < count; i++) {
            String title = sharedPreferences.getString("announcement_title_" + i, "");
            String description = sharedPreferences.getString("announcement_description_" + i, "");
            int imageResource = sharedPreferences.getInt("announcement_image_" + i, R.drawable.ic_image);
            announcements.add(new Announcement(title, description, imageResource));
        }
    }

    private void setupClickListeners() {
        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnPreview.setOnClickListener(v -> {
            if (validateInputs()) {
                previewAnnouncement();
            }
        });

        btnPublish.setOnClickListener(v -> {
            if (validateInputs()) {
                publishAnnouncement();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            titleLayout.setError("الرجاء إدخال عنوان");
            isValid = false;
        } else {
            titleLayout.setError(null);
        }

        if (description.isEmpty()) {
            descriptionLayout.setError("الرجاء إدخال وصف");
            isValid = false;
        } else {
            descriptionLayout.setError(null);
        }

        return isValid;
    }

    private void previewAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        Intent previewIntent = new Intent(this, AnnouncementViewActivity.class);
        previewIntent.putExtra("title", title);
        previewIntent.putExtra("description", description);
        previewIntent.putExtra("imageResource", String.valueOf(selectedImageResource));
        previewIntent.putExtra("isPreview", true);
        startActivity(previewIntent);
    }

    private void publishAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        // Add new announcement to the list
        announcements.add(0, new Announcement(title, description, selectedImageResource));
        
        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("announcementCount", announcements.size());
        
        for (int i = 0; i < announcements.size(); i++) {
            Announcement announcement = announcements.get(i);
            editor.putString("announcement_title_" + i, announcement.getTitle());
            editor.putString("announcement_description_" + i, announcement.getDescription());
            editor.putInt("announcement_image_" + i, announcement.getImageResource());
        }
        
        editor.apply();

        // Show success message
        new CustomAlertDialog(this)
            .show("تم نشر الإعلان بنجاح", R.drawable.baseline_check_circle_24);

        // Clear inputs
        etTitle.setText("");
        etDescription.setText("");
        ivSelectedImage.setImageResource(R.drawable.ic_image);
        selectedImageResource = R.drawable.ic_image;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_make_announcements;
    }
} 