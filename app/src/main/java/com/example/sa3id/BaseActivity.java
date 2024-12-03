package com.example.sa3id;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.example.sa3id.R;
import com.example.sa3id.UserActivities.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import android.widget.FrameLayout;


public abstract class BaseActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ImageView ivUserIcon;
    FrameLayout bottomSheet;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        initBottomSheet();
        initViews();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!handleChildBackPress()) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else if (bottomSheet != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    } else if (!(BaseActivity.this instanceof MainActivity)) {
                        Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        finishAffinity(); //exit
                    }
                }
            }
        });




    }


    protected boolean handleChildBackPress() {
        return false;
    }

    protected abstract int getLayoutResourceId();

    private void initViews() {
        ivUserIcon = findViewById(R.id.user_icon);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        navigationView.bringToFront();

        // Custom menu button
        ImageView menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(navigationView))
                    drawerLayout.closeDrawer(navigationView);
                else drawerLayout.openDrawer(navigationView);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                drawerLayout.closeDrawer(navigationView);
                handleNavigation(itemId);
                return true;
            }
        });


ivUserIcon.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        toggleBottomSheet();
    }
});

    }

    protected void handleNavigation(int itemId) {
        Context context = BaseActivity.this;

        if (itemId == R.id.nav_annoucements && !(this instanceof Announcements)) {
            startActivity(new Intent(context, Announcements.class));
            finish();
        }else if (itemId == R.id.nav_home && !(this instanceof MainActivity)) {
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }
        else if (itemId == R.id.nav_upload_materials && !(this instanceof UploadMaterials)) {
            startActivity(new Intent(context, UploadMaterials.class));
            finish();
        }
        else if (itemId == R.id.nav_our_books && !(this instanceof OurBooks)) {
            startActivity(new Intent(context, OurBooks.class));
            finish();
        } else if (itemId == R.id.nav_materials && !(this instanceof MaterialsChooseActivity)) {
            startActivity(new Intent(context, MaterialsChooseActivity.class));
            finish();
        } else if (itemId == R.id.nav_exams_bank && !(this instanceof ExamsBank)) {
            startActivity(new Intent(context, ExamsBank.class));
            finish();
        } else if (itemId == R.id.nav_calender && !(this instanceof Calender)) {
            startActivity(new Intent(context, Calender.class));
            finish();
        } else if (itemId == R.id.nav_settings && !(this instanceof UserSettings)) {
            startActivity(new Intent(context, UserSettings.class));
            finish();
        } else if (itemId == R.id.nav_contact_us && !(this instanceof ContactUs)) {
            startActivity(new Intent(context, ContactUs.class));
            finish();
        } else if (itemId == R.id.nav_about_us && !(this instanceof AboutPage)) {
            startActivity(new Intent(context, AboutPage.class));
            finish();
        } else if (itemId == R.id.nav_grades_calculator && !(this instanceof GradesCalculator)) {
            startActivity(new Intent(context, GradesCalculator.class));
            finish();
        } else if (itemId == R.id.nav_donate && !(this instanceof Donate)) {
            startActivity(new Intent(context, Donate.class));
            finish();
        } else if (itemId == R.id.nav_whatsapp_groups && !(this instanceof WhatsappGroups)) {
            startActivity(new Intent(context, WhatsappGroups.class));
            finish();
        }
    }



    private void initBottomSheet() {
        try  {
            bottomSheet = findViewById(R.id.bottomSheetProfile);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheetBehavior.setPeekHeight(0);
        }
       catch (Exception e) {
            bottomSheet =null;
            Log.e("BottomSheet", "Error initializing bottom sheet", e);
       }

    }

    public void toggleBottomSheet() {
        try {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }
        catch (Exception e) {
            Log.e("BottomSheet", "Error initializing bottom sheet", e);
        }


    }


}
