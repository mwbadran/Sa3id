package com.example.sa3id.adminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.cardview.widget.CardView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.example.sa3id.userActivities.AdminCalendarActivity;

public class ControlPanel extends BaseActivity {

    private CardView cardFeedbackRespond;
    private CardView cardMaterialsCheck;
    private CardView cardManageUsers;
    private CardView cardMakeAnnouncements;
    private CardView cardManageBagrutExams; // new
    private CardView cardManageCalendar; // new calendar card

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
    }

    private void initViews() {
        cardFeedbackRespond = findViewById(R.id.card_feedback_respond);
        cardMaterialsCheck = findViewById(R.id.card_materials_check);
        cardManageUsers = findViewById(R.id.card_manage_users);
        cardMakeAnnouncements = findViewById(R.id.card_make_announcements);
        cardManageBagrutExams = findViewById(R.id.card_manage_bagrut_exams); // new
        cardManageCalendar = findViewById(R.id.card_manage_calendar); // Initialize new card

        cardFeedbackRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ControlPanel.this, FeedbackRespondActivity.class));
            }
        });

        cardMaterialsCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ControlPanel.this, MaterialsCheckActivity.class));
            }
        });

        cardManageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(ControlPanel.this, ManageUsersActivity.class));
            }
        });

        cardMakeAnnouncements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(ControlPanel.this, MakeAnnouncementsActivity.class));
            }
        });

        cardManageBagrutExams.setOnClickListener(new View.OnClickListener() { // new
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(ControlPanel.this, ManageBagrutExamsActivity.class));
            }
        });
        
        // Add click listener for calendar management
        cardManageCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ControlPanel.this, AdminCalendarActivity.class));
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_control_panel;
    }
}