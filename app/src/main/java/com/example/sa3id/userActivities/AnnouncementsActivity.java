package com.example.sa3id.userActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;
import static com.example.sa3id.adapters.AnnouncementAdapter.openAnnouncementAsActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.sa3id.models.Announcement;
import com.example.sa3id.adapters.AnnouncementAdapter;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class AnnouncementsActivity extends BaseActivity {

    private ListView announcementsListView;
    private ArrayList<Announcement> announcementsList;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference("announcements");

        initViews();
        loadLocalAnnouncements();
        loadFirebaseAnnouncements();
    }

    private void initViews() {
        announcementsListView = findViewById(R.id.announcementsListView);
        progressBar = findViewById(R.id.progressBar);
        announcementsList = new ArrayList<>();
    }

    private void loadLocalAnnouncements() {
        sharedPreferences = getSharedPreferences("AnnouncementsSP", MODE_PRIVATE);
        int count = sharedPreferences.getInt("announcementCount", 0);

        for (int i = 0; i < count; i++) {
            String title = sharedPreferences.getString("announcement_title_" + i, "");
            String description = sharedPreferences.getString("announcement_description_" + i, "");
            int imageResource = sharedPreferences.getInt("announcement_image_" + i, R.drawable.ic_image);

            announcementsList.add(new Announcement(title, description, imageResource));
        }

        updateListView();
    }

    private void loadFirebaseAnnouncements() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                announcementsList.removeIf(announcement -> !announcement.isLocal());

                for (DataSnapshot announcementSnapshot : snapshot.getChildren()) {
                    Announcement announcement = announcementSnapshot.getValue(Announcement.class);
                    if (announcement != null) {
                        announcementsList.add(announcement);
                    }
                }

                Collections.sort(announcementsList, (a1, a2) -> {
                    if (a1.isLocal() && !a2.isLocal()) return 1;
                    if (!a1.isLocal() && a2.isLocal()) return -1;
                    if (a1.isLocal() && a2.isLocal()) return 0;
                    return a2.getTimestamp().compareTo(a1.getTimestamp());
                });

                updateListView();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AnnouncementsActivity.this, "فشل في تحميل الإعلانات: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateListView() {
        AnnouncementAdapter adapter = new AnnouncementAdapter(this, 0, 0, announcementsList);
        announcementsListView.setAdapter(adapter);
        announcementsListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Announcement announcement = announcementsList.get(i);
            if (announcement.isLocal()) {
                openAnnouncementAsActivity(this, announcement.getTitle(), announcement.getDescription(), announcement.getImageResource());
            } else {
                openAnnouncementAsActivity(this, announcement.getTitle(), announcement.getDescription(), announcement.getImageUrl());
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_announcements;
    }
}