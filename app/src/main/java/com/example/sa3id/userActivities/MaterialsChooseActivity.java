package com.example.sa3id.userActivities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.Constants;
import com.example.sa3id.R;

public class MaterialsChooseActivity extends BaseActivity {

    private LinearLayout electronicsButton, electricityPhysicsButton, mechanicsPhysicsButton, mathButton,
            hebrewLanguageButton, historyButton, civicsButton, islamicReligionButton, biologyButton, healthScienceButton, arabicButton, englishButton;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MaterialsChooseActivity.this;
        initViews();
    }

    private void initViews() {
        arabicButton = findViewById(R.id.arabic_button);
        englishButton = findViewById(R.id.english_button);
        electronicsButton = findViewById(R.id.electronics_button);
        electricityPhysicsButton = findViewById(R.id.electricity_physics_button);
        mechanicsPhysicsButton = findViewById(R.id.mechanics_physics_button);
        mathButton = findViewById(R.id.math_button);
        hebrewLanguageButton = findViewById(R.id.hebrew_button);
        historyButton = findViewById(R.id.history_button);
        civicsButton = findViewById(R.id.civics_button);
        islamicReligionButton = findViewById(R.id.religion_button);
        biologyButton = findViewById(R.id.biology_button);
        healthScienceButton = findViewById(R.id.health_button);


        arabicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveArabic);
                startActivity(intent);
            }
        });

        englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveEnglish);
                startActivity(intent);
            }
        });

        electronicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
        intent.putExtra("rootFolderId", Constants.driveBiology); // Replace TargetActivity with the actual target Activity
                intent.putExtra("rootFolderId", Constants.driveElectronics);
                startActivity(intent);
            }
        });

        electricityPhysicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveElectricity);
                startActivity(intent);
            }
        });

        mechanicsPhysicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveMechanics);
                startActivity(intent);
            }
        });

        mathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveMath);
                startActivity(intent);
            }
        });

        hebrewLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveHebrew);
                startActivity(intent);
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveHistory);
                startActivity(intent);
            }
        });

        civicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveMadanyat);
                startActivity(intent);
            }
        });

        islamicReligionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveIslam);
                startActivity(intent);
            }
        });

        biologyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveBiology);
                startActivity(intent);
            }
        });

        healthScienceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialsActivity.class);
                intent.putExtra("rootFolderId", Constants.driveHealthScience);
                startActivity(intent);
            }
        });



    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_materials_choose;
    }

}