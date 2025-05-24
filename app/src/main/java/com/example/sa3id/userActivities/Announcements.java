package com.example.sa3id.userActivities;

import static com.example.sa3id.adapters.AnnouncementAdapter.openAnnouncementAsActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.sa3id.models.Announcement;
import com.example.sa3id.adapters.AnnouncementAdapter;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;

import java.util.ArrayList;

public class Announcements extends BaseActivity {

    ListView announcementsListView;
    ArrayList<Announcement> announcementsList;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_annoucements);

        // Retrieve announcements from SharedPreferences
        announcementsList = getAnnouncementsFromSP();

        // Initialize ListView and adapter
        announcementsListView = findViewById(R.id.announcementsListView);
        AnnouncementAdapter adapter = new AnnouncementAdapter(this, 0, 0, announcementsList);
        announcementsListView.setAdapter(adapter);
        announcementsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openAnnouncementAsActivity(Announcements.this, announcementsList.get(i).getTitle(),announcementsList.get(i).getDescription(),announcementsList.get(i).getImageResource());
            }
        });

    }



    private ArrayList<Announcement> getAnnouncementsFromSP() {
        sharedPreferences = getSharedPreferences("AnnouncementsSP", MODE_PRIVATE);

        int count = sharedPreferences.getInt("announcementCount", 0);
        ArrayList<Announcement> announcements = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String title = sharedPreferences.getString("announcement_title_" + i, "");
            String description = sharedPreferences.getString("announcement_description_" + i, "");
            int imageResource = sharedPreferences.getInt("announcement_image_" + i, R.drawable.nizar);

            // Reconstruct the Announcement object
            announcements.add(new Announcement(title, description, imageResource));
        }

        return announcements;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_annoucements;
    }


}