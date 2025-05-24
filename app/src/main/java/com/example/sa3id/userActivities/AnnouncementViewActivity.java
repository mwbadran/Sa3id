package com.example.sa3id.userActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;

public class AnnouncementViewActivity extends BaseActivity {

    private Intent comeIntent;
    private String title, description;
    private int imageResource;

    private TextView tvTitle, tvDescription;
    private ImageView announcementImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        comeIntent = getIntent();
        title = comeIntent.getStringExtra("title");
        description = comeIntent.getStringExtra("description");
        imageResource = Integer.parseInt(comeIntent.getStringExtra("imageResource"));

        initViews();


        setDataToViews();
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
    }

    private void setDataToViews() {
        tvTitle.setText(title);
        tvDescription.setText(description);

        announcementImage.setImageResource(imageResource);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_announcement_view;
    }
}