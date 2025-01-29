package com.example.sa3id;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.fragment.app.FragmentManager;

import com.example.sa3id.AdminActivities.ControlPanel;
import com.example.sa3id.R;
import com.example.sa3id.UserActivities.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
    private TextView tvEmail, tvUsername;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView ivUserIcon;
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
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        return false;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    protected abstract int getLayoutResourceId();

    private void initViews() {
        ivUserIcon = findViewById(R.id.user_icon);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        navigationView.bringToFront();

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
        } else if (itemId == R.id.nav_home && !(this instanceof MainActivity)) {
            startActivity(new Intent(context, MainActivity.class));
            finish();
        } else if (itemId == R.id.nav_upload_materials && !(this instanceof UploadMaterials)) {
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
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void initBottomSheet() {
        try {
            bottomSheet = findViewById(R.id.bottomSheetProfile);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            bottomSheetBehavior.setPeekHeight(0);
            tvUsername = bottomSheet.findViewById(R.id.tvUsername);
            tvEmail = bottomSheet.findViewById(R.id.tvEmail);
            btnSignIn = bottomSheet.findViewById(R.id.btnSignin);
            btnSignUp = bottomSheet.findViewById(R.id.btnSignup);
            btnLogout = bottomSheet.findViewById(R.id.btnLogout);
            onlyForLoggedIn = bottomSheet.findViewById(R.id.onlyForLoggedIn);
            onlyForLoggedOut = bottomSheet.findViewById(R.id.onlyForLoggedOut);
            onlyForAdmins = bottomSheet.findViewById(R.id.onlyForAdmins);
            btnAdminPanel = bottomSheet.findViewById(R.id.btnAdminPanel);

            btnAdminPanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), ControlPanel.class));
                }
            });


            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    View overlay = findViewById(R.id.background_overlay);
                    if (overlay != null) {
                        overlay.setAlpha(slideOffset * 0.8f);
                    }
                }
            });

            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                }
            });
            btnSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), SignUp.class));
                }
            });
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAuth.signOut();
                    setDefaultCredentials();
                    Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show();
                }
            });

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser == null) {
                setDefaultCredentials();
                return;
            }

            fetchUserDetails(firebaseUser.getUid());

        } catch (Exception e) {
            bottomSheet = null;
            Log.e("BottomSheet", "Error initializing bottom sheet", e);
        }
    }

    public void toggleBottomSheet() {
        try {
            final View overlay = findViewById(R.id.background_overlay);

            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                overlay.setVisibility(View.VISIBLE);
                fadeInOverlay(overlay);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                fadeOutOverlay(overlay);
            }
        } catch (Exception e) {
            Log.e("BottomSheet", "Error! bottom sheet not initialized", e);
        }
    }

    private void fadeInOverlay(final View overlay) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(overlay, "alpha", 0f, 0.8f);
        fadeIn.setDuration(300);
        fadeIn.start();
    }

    private void fadeOutOverlay(final View overlay) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(overlay, "alpha", 0.8f, 0f);
        fadeOut.setDuration(300);
        fadeOut.start();

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                overlay.setVisibility(View.GONE);
            }
        });
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
        } catch (Exception e) {
            Log.e("BottomSheet", "Error! bottom sheet not initialized", e);
        }
    }

    public void setCredentials(String username, String email) {
        try {
            isLoggedIn = true;
            tvUsername.setText(username);
            tvEmail.setText(email);
            onlyForLoggedIn.setVisibility(View.VISIBLE);
            onlyForLoggedOut.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e("BottomSheet", "Error! bottom sheet not initialized", e);
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
                                documentSnapshot.getString("email"));

                        //Toast.makeText(this, user.getUsername(), Toast.LENGTH_SHORT).show();
                        setCredentials(user.getUsername(), user.getEmail());

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


}