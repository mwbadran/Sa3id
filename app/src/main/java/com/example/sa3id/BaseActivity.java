package com.example.sa3id;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.sa3id.AdminActivities.ControlPanel;
import com.example.sa3id.R;
import com.example.sa3id.UserActivities.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public abstract class BaseActivity extends AppCompatActivity {
    private TextView tvEmail, tvUsername ;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView ivUserIcon, ivbottomSheetUserIcon;
    private FirebaseUser firebaseUser;
    private FrameLayout bottomSheet;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private Button btnSignIn, btnSignUp, btnLogout, btnAdminPanel;
    private FirebaseAuth mAuth;
    private LinearLayout onlyForLoggedIn, onlyForLoggedOut, onlyForAdmins;
    private boolean isLoggedIn;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        isLoggedIn = false;
        isAdmin = false;
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

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
                        finishAffinity();
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

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        navigationView.bringToFront();

        ImageView menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(navigationView))
                drawerLayout.closeDrawer(navigationView);
            else drawerLayout.openDrawer(navigationView);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            drawerLayout.closeDrawer(navigationView);
            handleNavigation(itemId);
            return true;
        });

        ivUserIcon.setOnClickListener(view -> toggleBottomSheet());
    }

    protected void handleNavigation(int itemId) {
        Context context = BaseActivity.this;

        if (itemId == R.id.nav_annoucements && !(this instanceof Announcements)) {
            startActivity(new Intent(context, Announcements.class));
            finish();
        } else if (itemId == R.id.nav_home && !(this instanceof MainActivity)) {
            startActivity(new Intent(context, MainActivity.class));
            finish();
        } else if (itemId == R.id.nav_upload_materials && !(this instanceof UploadMaterials) && isAdmin) {
            startActivity(new Intent(context, UploadMaterials.class));
            finish();
        } else if (itemId == R.id.nav_our_books && !(this instanceof OurBooks)) {
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
        try {
            bottomSheet = findViewById(R.id.bottomSheetProfile);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheetBehavior.setPeekHeight(0);
            tvUsername = bottomSheet.findViewById(R.id.tvUsername);
            ivbottomSheetUserIcon = bottomSheet.findViewById(R.id.profile_pic);
            tvEmail = bottomSheet.findViewById(R.id.tvEmail);
            btnSignIn = bottomSheet.findViewById(R.id.btnSignin);
            btnSignUp = bottomSheet.findViewById(R.id.btnSignup);
            btnLogout = bottomSheet.findViewById(R.id.btnLogout);
            btnAdminPanel = bottomSheet.findViewById(R.id.btnAdminPanel);
            onlyForLoggedIn = bottomSheet.findViewById(R.id.onlyForLoggedIn);
            onlyForLoggedOut = bottomSheet.findViewById(R.id.onlyForLoggedOut);
            onlyForAdmins = bottomSheet.findViewById(R.id.onlyForAdmins);

            btnSignIn.setOnClickListener(view -> {
                startActivity(new Intent(getApplicationContext(), SignIn.class));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            });

            btnSignUp.setOnClickListener(view -> {
                startActivity(new Intent(getApplicationContext(), SignUp.class));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            });

            btnLogout.setOnClickListener(view -> {
                mAuth.signOut();
                setDefaultCredentials();
                Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            });

            btnAdminPanel.setOnClickListener(view ->
                    startActivity(new Intent(getApplicationContext(), ControlPanel.class))
            );

            firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null) {
                // Always fetch and update credentials
                fetchUserDetails(firebaseUser.getUid());
                checkAdminStatus(firebaseUser.getUid());
            } else {
                setDefaultCredentials();
            }

            fetchUserDetails(firebaseUser.getUid());

        } catch (Exception e) {
            bottomSheet = null;
            Log.e("BottomSheet", "Error initializing bottom sheet", e);
        }
    }

    private void fetchUserDetails(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User(
                                documentSnapshot.getString("username"),
                                documentSnapshot.getString("email"),
                                documentSnapshot.getString("profilePic"));


                        //Toast.makeText(this, user.getUsername(), Toast.LENGTH_SHORT).show();
                        setCredentials(user.getUsername(), user.getEmail(), user.getProfilePic());

                        if (Boolean.TRUE.equals(documentSnapshot.getBoolean("admin"))) {
                            Toast.makeText(getApplicationContext(), "Logged in as admin", Toast.LENGTH_SHORT).show();
                            isAdmin = true;
                            onlyForAdmins.setVisibility(View.VISIBLE);
                        } else {
                            isAdmin = false;
                            onlyForAdmins.setVisibility(View.GONE);
                        }
                    } else {
                        //Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        setDefaultCredentials();
                    }
                })
                .addOnFailureListener(e -> {
                    setDefaultCredentials();
                    Log.e("Firestore", "Failed to fetch user data", e);
                });
    }


    private void checkAdminStatus(String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("admin");

        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Boolean adminStatus = task.getResult().getValue(Boolean.class);
                isAdmin = adminStatus != null && adminStatus;
                onlyForAdmins.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                btnAdminPanel.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void toggleBottomSheet() {
        try {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        } catch (Exception e) {
            Log.e("BottomSheet", "Error! bottom sheet not initialized", e);
        }
    }

    public void setDefaultCredentials() {
        try {
            isLoggedIn = false;
            isAdmin = false;
            tvUsername.setText("زائر");
            tvEmail.setText("لم يتم تسجيل الدخول");
            onlyForLoggedIn.setVisibility(View.GONE);
            onlyForLoggedOut.setVisibility(View.VISIBLE);
            onlyForAdmins.setVisibility(View.GONE);
            btnAdminPanel.setVisibility(View.GONE);

            ivUserIcon.setImageResource(R.drawable.profile_pic);
            ivbottomSheetUserIcon.setImageResource(R.drawable.profile_pic);
        } catch (Exception e) {
            Log.e("BottomSheet", "Error! bottom sheet not initialized", e);
        }
    }

    public void setCredentials(String username, String email, String profilePicUrl) {
        try {
            isLoggedIn = true;
            tvUsername.setText(username != null ? username : "User");
            tvEmail.setText(email != null ? email : "No email");

            Glide.with(BaseActivity.this)
                    .load(Uri.parse(profilePicUrl))
                    .placeholder(R.drawable.profile_pic)
                    .error(R.drawable.profile_pic)
                    .into(ivUserIcon);

            Glide.with(BaseActivity.this)
                    .load(Uri.parse(profilePicUrl))
                    .placeholder(R.drawable.profile_pic)
                    .error(R.drawable.profile_pic)
                    .into(ivbottomSheetUserIcon);

            onlyForLoggedIn.setVisibility(View.VISIBLE);
            onlyForLoggedOut.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e("BottomSheet", "Error! bottom sheet not initialized", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}