package com.example.sa3id.userActivities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.example.sa3id.adapters.ExamAdapter;
import com.example.sa3id.fragments.SettingsFragment;
import com.example.sa3id.managers.ExamManager;
import com.example.sa3id.managers.NotificationManager;
import com.example.sa3id.models.Exam;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

public class ExamsActivity extends BaseActivity {
    private static final String TAG = "ExamsActivity";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ExamAdapter examAdapter;
    private ExamManager examManager;
    private NotificationManager notificationManager;
    private String userExtraTimeType = "0"; // no extra time
    private Set<String> userSubjects = new HashSet<>();
    private BroadcastReceiver settingsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewExams);
        progressBar = findViewById(R.id.progressBar);

        // Initialize managers
        examManager = new ExamManager();
        notificationManager = new NotificationManager(this);
        
        // Setup test notification button
        findViewById(R.id.btnTestNotification).setOnClickListener(v -> {
            // Request notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission();
            }
            
            // Check for exact alarm permission first
            if (!notificationManager.canScheduleExactAlarms()) {
                notificationManager.showExactAlarmPermissionDialog();
            } else {
                notificationManager.sendTestNotification();
            }
        });

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        examAdapter = new ExamAdapter(userExtraTimeType);
        recyclerView.setAdapter(examAdapter);

        // Get user's settings and subjects
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadUserSettings(currentUser.getUid());
        } else {
            CustomAlertDialog dialog= new CustomAlertDialog(this);
            dialog.show("يرجى تسجيل الدخول لعرض الامتحانات", R.drawable.baseline_error_24);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                    startActivity(new Intent(ExamsActivity.this, MainActivity.class));
                }
            });

        }
        
        // Setup broadcast receiver for settings changes
        setupSettingsReceiver();
    }

    private void loadUserSettings(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK)
            .getReference()
            .child("users")
            .child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Load extra time setting
                        if (dataSnapshot.hasChild("extraTimeType")) {
                            userExtraTimeType = dataSnapshot.child("extraTimeType").getValue(String.class);
                            examAdapter.setExtraTimeType(userExtraTimeType);
                        }

                        // Load selected subjects
                        if (dataSnapshot.hasChild("selectedSubjects")) {
                            for (DataSnapshot subjectSnapshot : dataSnapshot.child("selectedSubjects").getChildren()) {
                                String subject = subjectSnapshot.getValue(String.class);
                                if (subject != null) {
                                    userSubjects.add(subject);
                                }
                            }
                        }

                        // Load exams for user's subjects
                        loadExams();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        new CustomAlertDialog(ExamsActivity.this)
                            .show("لم يتم العثور على إعدادات المستخدم", R.drawable.baseline_error_24);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressBar.setVisibility(View.GONE);
                    new CustomAlertDialog(ExamsActivity.this)
                        .show("خطأ في تحميل إعدادات المستخدم: " + databaseError.getMessage(), 
                            R.drawable.baseline_error_24);
                }
            });
    }

    private void loadExams() {
        if (userSubjects.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            new CustomAlertDialog(this)
                .show("يرجى اختيار المواضيع أولاً", R.drawable.baseline_error_24);
            startActivity(new Intent(this, ChooseBagrutsActivity.class));
            finish();
            return;
        }

        examManager.getExamsForUserSubjects(userSubjects, new ExamManager.ExamCallback() {
            @Override
            public void onExamsLoaded(List<Exam> exams) {
                progressBar.setVisibility(View.GONE);
                examAdapter.setExams(exams);
                
                // Check permission for exact alarms on Android 12+
                if (!notificationManager.canScheduleExactAlarms()) {
                    notificationManager.showExactAlarmPermissionDialog();
                } else {
                    // Schedule notifications for all exams
                    scheduleExamNotifications(exams);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                new CustomAlertDialog(ExamsActivity.this)
                    .show("خطأ في تحميل الامتحانات: " + error, R.drawable.baseline_error_24);
            }
        });
    }

    private void scheduleExamNotifications(List<Exam> exams) {
        for (Exam exam : exams) {
            notificationManager.scheduleExamNotification(exam, userExtraTimeType);
        }
    }

    private void setupSettingsReceiver() {
        settingsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SettingsFragment.ACTION_NOTIFICATION_SETTINGS_CHANGED.equals(intent.getAction())) {
                    Log.d(TAG, "Received notification settings changed broadcast");
                    // Reschedule notifications for current exams
                    if (examAdapter != null && examAdapter.getItemCount() > 0) {
                        Log.d(TAG, "Rescheduling notifications for " + examAdapter.getItemCount() + " exams");
                        notificationManager.rescheduleAllNotifications(examAdapter.getExams(), userExtraTimeType);
                    }
                }
            }
        };
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Register broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            settingsReceiver, 
            new IntentFilter(SettingsFragment.ACTION_NOTIFICATION_SETTINGS_CHANGED)
        );
        
        // Check again when returning to app (user might have enabled permission in settings)
        if (examAdapter.getItemCount() > 0 && notificationManager.canScheduleExactAlarms()) {
            scheduleExamNotifications(examAdapter.getExams());
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(settingsReceiver);
    }

    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new CustomAlertDialog(this)
                    .show("تم منح إذن الإشعارات", R.drawable.baseline_check_circle_24);
            } else {
                new CustomAlertDialog(this)
                    .show("تم رفض إذن الإشعارات", R.drawable.baseline_error_24);
            }
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_exams;
    }
} 