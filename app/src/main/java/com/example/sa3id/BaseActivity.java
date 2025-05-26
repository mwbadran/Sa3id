package com.example.sa3id;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.bumptech.glide.Glide;
import com.example.sa3id.adminActivities.ControlPanel;
import com.example.sa3id.models.User;
import com.example.sa3id.userActivities.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseActivity extends AppCompatActivity {
    private TextView tvEmail, tvUsername;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView ivUserIcon, ivbottomSheetUserIcon;
    protected FirebaseUser firebaseUser;
    private FrameLayout bottomSheet;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private Button btnSignIn, btnSignUp, btnLogout, btnAdminPanel;
    private FirebaseAuth mAuth;
    private LinearLayout onlyForLoggedIn, onlyForLoggedOut, onlyForAdmins;
    private LinearLayout llEditBagrutExams, llUserSettings;
    protected boolean isAdmin, isBottomSheetInitialized;
    Context context;
    private Toast easterEggToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        isBottomSheetInitialized = false;
        context = BaseActivity.this;
        isAdmin = false;
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        easterEggToast = Toast.makeText(this, "test", Toast.LENGTH_SHORT);

        initBottomSheet();
        initViews();

        if (firebaseUser != null) fetchUserDetails(firebaseUser.getUid());
        showOnlyFor(firebaseUser != null);

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
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        navigationView.bringToFront();

        ImageView menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(navigationView)) drawerLayout.closeDrawer(navigationView);
            else drawerLayout.openDrawer(navigationView);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId != R.id.nav_easter_egg)
                drawerLayout.closeDrawer(navigationView);
            handleNavigation(itemId);
            return true; // Don't close drawer
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

        if (itemId == R.id.nav_easter_egg) {
            handleEasterEggClick();
            return;
        }

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
        } else if (itemId == R.id.nav_exams_calendar && !(this instanceof ExamsCalendar)) {
            startActivity(new Intent(context, ExamsCalendar.class));
            finish();
        } else if (itemId == R.id.nav_exams && !(this instanceof ExamsActivity)) {
            startActivity(new Intent(context, ExamsActivity.class));
            finish();
        } else if (itemId == R.id.nav_settings && !(this instanceof UserSettings)) {
            startActivity(new Intent(context, UserSettings.class));
            finish();
        } else if (itemId == R.id.nav_contact_us && !(this instanceof FeedbackActivity)) {
            startActivity(new Intent(context, FeedbackActivity.class));
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
            isBottomSheetInitialized = true;
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
            // Bottom Sheet Menu Elements:
            llEditBagrutExams = bottomSheet.findViewById(R.id.llEditBagrutExams);
            llUserSettings = bottomSheet.findViewById(R.id.llUserSettings);


            setupBottomSheetListeners();


            firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null) {
                // Always fetch and update credentials
                fetchUserDetails(firebaseUser.getUid());
                //checkAdminStatus(firebaseUser.getUid());
            } else {
                setDefaultCredentials();
            }

        } catch (Exception e) {
            isBottomSheetInitialized = false;
            bottomSheet = null;
            Log.e("BottomSheet", "Error initializing bottom sheet", e);
        }
    }

    protected void fetchUserDetails(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = new User(documentSnapshot.getString("username"), documentSnapshot.getString("email"), documentSnapshot.getString("profilePic"));


                //Toast.makeText(this, user.getUsername(), Toast.LENGTH_SHORT).show();
                setCredentials(user.getUsername(), user.getEmail(), user.getProfilePic());

                if (Boolean.TRUE.equals(documentSnapshot.getBoolean("admin"))) {
                    //Toast.makeText(getApplicationContext(), "Logged in as admin", Toast.LENGTH_SHORT).show();
                    isAdmin = true;
                    if (isBottomSheetInitialized) onlyForAdmins.setVisibility(View.VISIBLE);
                } else {
                    isAdmin = false;
                    if (isBottomSheetInitialized) onlyForAdmins.setVisibility(View.GONE);
                }
            } else {
                setDefaultCredentials();
            }
        }).addOnFailureListener(e -> {
            setDefaultCredentials();
            Log.e("Firestore", "Failed to fetch user data", e);
        });
        //Toast.makeText(context, ""+isAdmin, Toast.LENGTH_SHORT).show();
    }

    private void setupBottomSheetListeners() {
        //Buttonsw
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
            CustomAlertDialog dialog = new CustomAlertDialog(this);
            dialog.show("تم تسجيل الخروج بنجاح", R.drawable.baseline_check_circle_24);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        btnAdminPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ControlPanel.class));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        // Menu Elements (They are LinearLayouts...)

        llEditBagrutExams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, ChooseBagrutsActivity.class));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        llUserSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, UserSettings.class));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        // Add click listener for edit profile button
        View editProfileButton = bottomSheet.findViewById(R.id.edit_profile_pic);
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> {
                if (firebaseUser == null) {
                    CustomAlertDialog dialog = new CustomAlertDialog(this);
                    dialog.show("يرجى تسجيل الدخول أولاً", R.drawable.baseline_error_24);
                    return;
                }
                Intent intent = new Intent(this, ProfileEdit.class);
                startActivity(intent);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            });
        }
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
            tvUsername.setText(username != null ? username : "User");
            tvEmail.setText(email != null ? email : "No email");

            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                Uri uri = Uri.parse(profilePicUrl);
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.profile_pic)
                        .error(R.drawable.profile_pic)
                        .into(ivUserIcon);
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.profile_pic)
                        .error(R.drawable.profile_pic)
                        .into(ivbottomSheetUserIcon);
            } else {
                // fallback: just set the placeholder directly
                ivUserIcon           .setImageResource(R.drawable.profile_pic);
                ivbottomSheetUserIcon.setImageResource(R.drawable.profile_pic);
            }
            onlyForLoggedIn.setVisibility(View.VISIBLE);
            onlyForLoggedOut.setVisibility(View.GONE);
            onlyForAdmins.setVisibility(View.GONE);

        } catch (Exception e) {
            Log.e("BottomSheet", "Error! bottom sheet not initialized", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) fetchUserDetails(firebaseUser.getUid());
        showOnlyFor(firebaseUser != null);

    }
    
    private void showOnlyFor(boolean isLoggedIn) {
        if (!isBottomSheetInitialized) return;
        onlyForAdmins.setVisibility(View.GONE);
        if (isLoggedIn) {
            onlyForLoggedIn.setVisibility(View.VISIBLE);
            onlyForLoggedOut.setVisibility(View.GONE);
        } else {
            onlyForLoggedIn.setVisibility(View.GONE);
            onlyForLoggedOut.setVisibility(View.VISIBLE);
        }
    }

    private void handleEasterEggClick() {
        SharedPreferences prefs = getSharedPreferences("easter_egg", MODE_PRIVATE);
        long lastClickTime = prefs.getLong("last_click_time", 0);
        int clickCount = prefs.getInt("click_count", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastClickTime > 3000) {
            clickCount = 0;
        }

        clickCount++;
        prefs.edit()
                .putLong("last_click_time", currentTime)
                .putInt("click_count", clickCount)
                .apply();

        if (clickCount >= 3 && clickCount < 10) {
            int remaining = 10 - clickCount;
            if (easterEggToast != null) {
                easterEggToast.cancel(); // should hide previous one
                easterEggToast = Toast.makeText(this, "باقي " + remaining + " نقرات للهدية!",Toast.LENGTH_SHORT);
                easterEggToast.show();
            }
        } else if (clickCount >= 10) {
            if (easterEggToast != null) easterEggToast.cancel();
            Toast.makeText(this, "مبروك! لقد وجدت القطة الخفية كابا علي 😼", Toast.LENGTH_LONG).show();
            prefs.edit().putInt("click_count", 0).apply();
            startActivity(new Intent(this, EasterEggActivity.class));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}