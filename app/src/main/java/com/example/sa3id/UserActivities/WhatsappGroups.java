package com.example.sa3id.UserActivities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.google.android.material.navigation.NavigationView;

public class WhatsappGroups extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_whatsapp_groups);

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_whatsapp_groups;
    }


}