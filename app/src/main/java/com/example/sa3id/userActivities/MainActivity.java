package com.example.sa3id.userActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;
import static com.example.sa3id.adapters.AnnouncementAdapter.openAnnouncementAsActivity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.sa3id.models.Announcement;
import com.example.sa3id.adapters.AnnouncementAdapter;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends BaseActivity {

    SharedPreferences announcementsSP, userDetailsSP;
    SharedPreferences.Editor annoucementsEditor, userDetailsEditor;
    ListView announcementsListView;
    ArrayList<Announcement> announcementsList;
    AnnouncementAdapter adapter;
    ProgressBar progressBar;
    DatabaseReference databaseReference;
    NavigationView navigationView;
    GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences and editor
        announcementsSP = getSharedPreferences("AnnouncementsSP", MODE_PRIVATE);
        annoucementsEditor = announcementsSP.edit();
        userDetailsSP = getSharedPreferences("UserDetailsSP", MODE_PRIVATE);
        userDetailsEditor = userDetailsSP.edit();

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK)
            .getReference("announcements");

        initViews();
        populateGridLayout();
        loadAnnouncements();
    }

    private void initViews() {
        navigationView = findViewById(R.id.nav_view);
        gridLayout = findViewById(R.id.grid_layout);
        progressBar = findViewById(R.id.progressBar);

        announcementsListView = findViewById(R.id.announcementsListView);
        announcementsList = new ArrayList<>();
        announcementsListView.setOnItemClickListener((adapterView, view, i, position) -> {
            Announcement announcement = announcementsList.get(i);
            if (announcement.isLocal()) {
                openAnnouncementAsActivity(MainActivity.this, 
                    announcement.getTitle(),
                    announcement.getDescription(),
                    announcement.getImageResource());
            } else {
                openAnnouncementAsActivity(MainActivity.this,
                    announcement.getTitle(),
                    announcement.getDescription(),
                    announcement.getImageUrl());
            }
        });
    }

    private void loadAnnouncements() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Query Firebase for latest 2 announcements
        Query query = databaseReference.orderByChild("timestamp").limitToLast(2);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Announcement> firebaseAnnouncements = new ArrayList<>();
                
                for (DataSnapshot announcementSnapshot : snapshot.getChildren()) {
                    Announcement announcement = announcementSnapshot.getValue(Announcement.class);
                    if (announcement != null) {
                        firebaseAnnouncements.add(announcement);
                    }
                }

                // Sort Firebase announcements by timestamp (newest first)
                firebaseAnnouncements.sort((a1, a2) ->
                        a2.getTimestamp().compareTo(a1.getTimestamp()));

                // If we have less than 2 Firebase announcements, get the rest from local storage
                if (firebaseAnnouncements.size() < 2) {
                    ArrayList<Announcement> localAnnouncements = getLocalAnnouncements();
                    int remainingCount = 2 - firebaseAnnouncements.size();
                    
                    // Add local announcements until we have 2 total
                    for (int i = 0; i < Math.min(remainingCount, localAnnouncements.size()); i++) {
                        firebaseAnnouncements.add(localAnnouncements.get(i));
                    }
                }

                // Update the UI with the combined announcements
                announcementsList.clear();
                announcementsList.addAll(firebaseAnnouncements);
                adapter = new AnnouncementAdapter(MainActivity.this, 0, 0, announcementsList);
                announcementsListView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, 
                    "فشل في تحميل الإعلانات: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                // Fall back to local announcements
                loadLocalAnnouncements();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private ArrayList<Announcement> getLocalAnnouncements() {
        ArrayList<Announcement> localAnnouncements = new ArrayList<>();
        int count = announcementsSP.getInt("announcementCount", 0);

        for (int i = 0; i < count; i++) {
            String title = announcementsSP.getString("announcement_title_" + i, "");
            String description = announcementsSP.getString("announcement_description_" + i, "");
            int imageResource = announcementsSP.getInt("announcement_image_" + i, R.drawable.ic_image);
            localAnnouncements.add(new Announcement(title, description, imageResource));
        }

        return localAnnouncements;
    }

    private void loadLocalAnnouncements() {
        ArrayList<Announcement> localAnnouncements = getLocalAnnouncements();
        announcementsList.clear();
        announcementsList.addAll(localAnnouncements);
        adapter = new AnnouncementAdapter(MainActivity.this, 0, 0, announcementsList);
        announcementsListView.setAdapter(adapter);
    }

    private void populateGridLayout() {
        int columnCount = 3;
        int paddingInPixels = (int) (20 * getResources().getDisplayMetrics().density);
        int screenWidth = getResources().getDisplayMetrics().widthPixels - (2 * paddingInPixels);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int gridItemSize = (screenWidth / columnCount) - 32;
        int iconSize = (int) (gridItemSize * 0.5);

        ArrayList<GridItem> items = new ArrayList<>();
        items.add(new GridItem(R.drawable.announcements, getString(R.string.announcements), R.id.nav_annoucements));
        items.add(new GridItem(R.drawable.upcoming_exams, getString(R.string.upcoming_exams), R.id.nav_exams_calendar));
        items.add(new GridItem(R.drawable.materials, getString(R.string.materials_page), R.id.nav_materials));
        items.add(new GridItem(R.drawable.our_books, getString(R.string.our_books), R.id.nav_our_books));
        items.add(new GridItem(R.drawable.calc, getString(R.string.grades_calculator), R.id.nav_grades_calculator));
        items.add(new GridItem(R.drawable.upload_materials, getString(R.string.upload_materials), R.id.nav_upload_materials));
        items.add(new GridItem(R.drawable.whatsapp_logo, getString(R.string.whatsapp_groups), R.id.nav_whatsapp_groups));
        items.add(new GridItem(R.drawable.baseline_feedback_24, getString(R.string.feedback), R.id.nav_contact_us));
        items.add(new GridItem(R.drawable.baseline_donate_24, getString(R.string.donate), R.id.nav_donate));

        gridLayout.setColumnCount(columnCount);
        gridLayout.removeAllViews();

        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        float textSizeInSp = 14f * (screenWidth/976f);

        boolean isDarkTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) 
                == Configuration.UI_MODE_NIGHT_YES;

        for (GridItem item : items) {
            LinearLayout layout = new LinearLayout(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = gridItemSize;
            params.height = gridItemSize;
            params.setMargins(16, 16, 16, 16);
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_square));
            layout.setPadding(10, 10, 10, 10);
            layout.setElevation(6f);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(iconSize, iconSize);
            imageParams.setMargins(0, 0, 0, 8);
            imageView.setLayoutParams(imageParams);
            imageView.setImageResource(item.imageResId);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // Apply theme-appropriate tint to icons
            if (isDarkTheme) {
                // For dark theme, use a light gray tint instead of pure white
                imageView.setColorFilter(
                    ContextCompat.getColor(this, R.color.sa3id_green),
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
            } else {
                // For light theme, use the original icon colors
                imageView.setColorFilter(null);
            }

            TextView textView = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(textParams);
            textView.setText(item.label);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(textSizeInSp);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.rpt_bold));
            textView.setTextColor(ContextCompat.getColor(this, isDarkTheme ? android.R.color.white : R.color.black));
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);

            layout.addView(imageView);
            layout.addView(textView);

            layout.setOnClickListener(view -> navigationView.getMenu().performIdentifierAction(item.navId, 0));

            gridLayout.addView(layout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    private static class GridItem {
        int imageResId;
        String label;
        int navId;

        GridItem(int imageResId, String label, int navId) {
            this.imageResId = imageResId;
            this.label = label;
            this.navId = navId;
        }
    }
}
