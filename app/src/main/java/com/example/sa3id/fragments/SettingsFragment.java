package com.example.sa3id.fragments;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.sa3id.R;
import com.example.sa3id.managers.ExamManager;
import com.example.sa3id.managers.NotificationManager;
import com.example.sa3id.models.Exam;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class SettingsFragment extends Fragment {
    private SwitchMaterial notificationSwitch;
    private TextView timeText;
    private Slider daysSlider;
    private TextView daysText;
    private LinearLayout notificationSettingsContainer;
    private SharedPreferences prefs;
    private DatabaseReference userRef;
    private NotificationManager notificationManager;
    private String userId;
    private Set<String> userSubjects = new HashSet<>();
    private String userExtraTimeType = "0";

    // Constants for SharedPreferences
    private static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String PREF_NOTIFICATION_HOUR = "notification_hour";
    private static final String PREF_NOTIFICATION_MINUTE = "notification_minute";
    private static final String PREF_DAYS_BEFORE = "days_before";

    public static final String ACTION_NOTIFICATION_SETTINGS_CHANGED = "com.example.sa3id.ACTION_NOTIFICATION_SETTINGS_CHANGED";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase reference
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://sa3idsite-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("users").child(userId);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        notificationManager = new NotificationManager(requireContext());

        loadUserSettings();

        initializeViews(view);
        loadSettings();
        setupListeners();
        setupChildFragments();
    }

    private void loadUserSettings() {
        // Load user subjects and extra time from Firebase
        userRef.child("selectedSubjects").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                userSubjects.clear();

                try {
                    Object value = dataSnapshot.getValue();
                    if (value instanceof List) {
                        List<?> subjectsList = (List<?>) value;
                        for (Object subject : subjectsList) {
                            if (subject != null) {
                                userSubjects.add(subject.toString());
                            }
                        }
                    } else if (value instanceof Map) {
                        // Handle case where it might be stored as a map
                        Map<?, ?> subjectsMap = (Map<?, ?>) value;
                        for (Object subject : subjectsMap.values()) {
                            if (subject != null) {
                                userSubjects.add(subject.toString());
                            }
                        }
                    }

                } catch (Exception e) {
                }
            } else {
                // Try using specific Firebase URL if the default one fails
                try {
                    DatabaseReference altRef = FirebaseDatabase.getInstance("https://sa3idsite-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("users").child(userId).child("selectedSubjects");


                    altRef.get().addOnSuccessListener(altSnapshot -> {
                        if (altSnapshot.exists()) {
                            userSubjects.clear();
                            try {
                                for (DataSnapshot subject : altSnapshot.getChildren()) {
                                    String subjectName = subject.getValue(String.class);
                                    if (subjectName != null) {
                                        userSubjects.add(subjectName);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(null, "Error in alternate subject loading: " + e.getMessage());
                            }
                        } else {
                        }
                    });
                } catch (Exception e) {
                }
            }
        });

        userRef.child("extraTimeType").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                userExtraTimeType = dataSnapshot.getValue(String.class);
            } else {

                // Try using specific Firebase URL
                DatabaseReference altRef = FirebaseDatabase.getInstance("https://sa3idsite-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("users").child(userId).child("extraTimeType");

                altRef.get().addOnSuccessListener(altSnapshot -> {
                    if (altSnapshot.exists()) {
                        userExtraTimeType = altSnapshot.getValue(String.class);
                    }
                });
            }
        });

        // For testing, add test subject "תיירות" if subjects are empty
        if (userSubjects.isEmpty()) {
            userSubjects.add("תיירות");
        }
    }

    private void initializeViews(View view) {
        notificationSwitch = view.findViewById(R.id.switchEnableNotifications);
        timeText = view.findViewById(R.id.btnNotificationTime);
        daysSlider = view.findViewById(R.id.sliderDaysBeforeExam);
        daysText = view.findViewById(R.id.txtDaysBeforeExam);
        notificationSettingsContainer = view.findViewById(R.id.notificationSettingsContainer);

        // Setup notification time selector
        view.findViewById(R.id.notificationTimeContainer).setOnClickListener(v -> showTimePickerDialog());
    }

    private void setupChildFragments() {
        // Add theme selection fragment
        if (getChildFragmentManager().findFragmentById(R.id.themeContainer) == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.themeContainer, new ThemeSelectionFragment()).commit();
        }

        // Add extra time selection fragment
        if (getChildFragmentManager().findFragmentById(R.id.extraTimeContainer) == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.extraTimeContainer, new ExtraTimeSelectionFragment()).commit();
        }
    }

    private void loadSettings() {
        // Load notification settings
        boolean notificationsEnabled = prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
        int hour = prefs.getInt(PREF_NOTIFICATION_HOUR, 10);
        int minute = prefs.getInt(PREF_NOTIFICATION_MINUTE, 0);
        int daysBefore = prefs.getInt(PREF_DAYS_BEFORE, 1);

        // Apply notification settings
        notificationSwitch.setChecked(notificationsEnabled);
        timeText.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        daysSlider.setValue(daysBefore);
        updateDaysText(daysBefore);
        notificationSettingsContainer.setVisibility(notificationsEnabled ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notificationSettingsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            prefs.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, isChecked).apply();
            saveSettingsToFirebase();
            notifySettingsChanged();
        });

        daysSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                int days = (int) value;
                updateDaysText(days);
                prefs.edit().putInt(PREF_DAYS_BEFORE, days).apply();
                saveSettingsToFirebase();
                notifySettingsChanged();
            }
        });
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = prefs.getInt(PREF_NOTIFICATION_HOUR, 10);
        int currentMinute = prefs.getInt(PREF_NOTIFICATION_MINUTE, 0);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            timeText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            prefs.edit().putInt(PREF_NOTIFICATION_HOUR, hourOfDay).putInt(PREF_NOTIFICATION_MINUTE, minute).apply();
            saveSettingsToFirebase();


            // Notify of settings change
            notifySettingsChanged();
        }, currentHour, currentMinute, true);

        timePickerDialog.show();
    }

    private void updateDaysText(int days) {
        String format = getString(R.string.notify_days_before);
        daysText.setText(String.format(format, days));
    }

    private void notifySettingsChanged() {
        // Send broadcast to notify of settings change
        Intent intent = new Intent(ACTION_NOTIFICATION_SETTINGS_CHANGED);
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);

        // If we have subject information, reschedule notifications
        if (!userSubjects.isEmpty()) {
            loadAndRescheduleExams();
        }
    }

    private void loadAndRescheduleExams() {


        if (userSubjects.isEmpty()) {
            Toast.makeText(requireContext(), "No subjects selected. Please choose subjects first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create ExamManager to load exams
        ExamManager examManager = new ExamManager();
        examManager.getExamsForUserSubjects(userSubjects, new ExamManager.ExamCallback() {
            @Override
            public void onExamsLoaded(List<Exam> exams) {

                if (exams.isEmpty()) {
                    Toast.makeText(requireContext(), "No exams were found for your subjects", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Log some sample exams for debugging
                int samplesToShow = Math.min(exams.size(), 3);
                for (int i = 0; i < samplesToShow; i++) {
                    Exam exam = exams.get(i);
                }

                notificationManager.rescheduleAllNotifications(exams, userExtraTimeType);
                Toast.makeText(requireContext(), "Rescheduled notifications for " + exams.size() + " exams", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to load exams: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void saveSettingsToFirebase() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("notificationsEnabled", notificationSwitch.isChecked());
        settings.put("notificationHour", prefs.getInt(PREF_NOTIFICATION_HOUR, 10));
        settings.put("notificationMinute", prefs.getInt(PREF_NOTIFICATION_MINUTE, 0));
        settings.put("daysBefore", (int) daysSlider.getValue());

        userRef.child("settings").updateChildren(settings);
    }
}