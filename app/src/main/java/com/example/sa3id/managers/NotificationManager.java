package com.example.sa3id.managers;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.sa3id.R;
import com.example.sa3id.models.Exam;
import com.example.sa3id.receivers.ExamNotificationReceiver;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationManager {
    private static final String CHANNEL_ID = "exam_notification_channel";
    private static final String CHANNEL_NAME = "Exam Notifications";
    private static final String CHANNEL_DESC = "Notifications for upcoming exams";

    // Constants for SharedPreferences
    private static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String PREF_NOTIFICATION_HOUR = "notification_hour";
    private static final String PREF_NOTIFICATION_MINUTE = "notification_minute";
    private static final String PREF_DAYS_BEFORE = "days_before";

    private final Context context;
    private final SharedPreferences prefs;

    public NotificationManager(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Create notification channel
        createNotificationChannel();

        // Check required permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }

    }

    private void createNotificationChannel() {
        // For Android 8.0+ (Oreo and above), create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, android.app.NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.enableVibration(true);

            // Get the NotificationManager service
            android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
            }
        } else {
        }
    }

    /**
     * Checks if the app can schedule exact alarms and prompts user if necessary
     */
    public boolean canScheduleExactAlarms() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12+
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        // For Android 11 and below
        return true;
    }

    /**
     * Show a dialog to direct user to the settings for exact alarm permission
     */
    public void showExactAlarmPermissionDialog() {
        if (context instanceof FragmentActivity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            new MaterialAlertDialogBuilder(context).setTitle("Permission Required").setMessage("To receive exam reminders, this app needs permission to schedule exact alarms. Please enable this in the settings.").setPositiveButton("Go to Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
            }).setNegativeButton("Cancel", (dialog, which) -> {
                Toast.makeText(context, "Exam notifications may not work correctly", Toast.LENGTH_LONG).show();
            }).setCancelable(false).show();
        }
    }

    /**
     * Schedule a notification for an exam using user's preferences
     */
    public void scheduleExamNotification(Exam exam, String extraTimeType) {
        // Check if notifications are enabled
        boolean notificationsEnabled = prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
        if (!notificationsEnabled) {
            return;
        }

        try {
            // Parse date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            String dateTimeStr = exam.getDate() + " " + exam.getStartHour();
            Date examDate = dateFormat.parse(dateTimeStr);

            if (examDate != null) {
                // Get user preferences
                int daysBefore = prefs.getInt(PREF_DAYS_BEFORE, 1);
                int hour = prefs.getInt(PREF_NOTIFICATION_HOUR, 10);
                int minute = prefs.getInt(PREF_NOTIFICATION_MINUTE, 0);

                // Set notification time based on preferences
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(examDate);
                calendar.add(Calendar.DAY_OF_YEAR, -daysBefore);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // If the notification time is in the past, set it to now + 10 seconds
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.setTimeInMillis(System.currentTimeMillis() + 10000); // 10 seconds in the future
                }


                // Create intent for notification
                Intent intent = new Intent(context, ExamNotificationReceiver.class);
                intent.putExtra("examName", exam.getExamName());
                intent.putExtra("examDate", exam.getDate());
                intent.putExtra("startTime", exam.getStartHour());
                intent.putExtra("endTime", exam.getEndTimeByExtraTime(extraTimeType));

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, exam.getExamId().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                // Schedule notification
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                                String msg = "Exam notification scheduled for " + dateFormat.format(calendar.getTime());
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            } else {
                                // Fall back to inexact alarm if we don't have permission
                                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            }
                        } catch (SecurityException e) {
                            // If we get a security exception, try inexact alarm as fallback
                            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            e.printStackTrace();
                        }
                    } else {
                        // For pre-Android 12, we can use exact alarms
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        String msg = "Exam notification scheduled for " + dateFormat.format(calendar.getTime());
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error scheduling notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Reschedule all notifications for a list of exams (used when settings change)
     */
    public void rescheduleAllNotifications(List<Exam> exams, String extraTimeType) {
        if (exams == null || exams.isEmpty()) {
            return;
        }


        // Get current notification settings
        boolean enabled = prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
        int hour = prefs.getInt(PREF_NOTIFICATION_HOUR, 10);
        int minute = prefs.getInt(PREF_NOTIFICATION_MINUTE, 0);
        int days = prefs.getInt(PREF_DAYS_BEFORE, 1);


        if (!enabled) {
            return;
        }

        // Reschedule each exam notification
        for (Exam exam : exams) {
            scheduleExamNotification(exam, extraTimeType);
        }
    }

    /**
     * Send a test notification immediately to check if notifications are working
     */
    public void sendTestNotification() {

        // Create test exam data
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Add 1 day to current date
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        String examDate = dateFormat.format(calendar.getTime());
        String startTime = timeFormat.format(calendar.getTime());

        // Add 1 hour to get end time
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        String endTime = timeFormat.format(calendar.getTime());

        // For testing permission issues
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_LONG).show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if notification channel exists
            android.app.NotificationManager notifManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notifManager != null) {
                NotificationChannel channel = notifManager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    createNotificationChannel();
                }
            }
        }

        // Show the notification immediately
        showExamNotification("Test Notification - " + timeFormat.format(new Date()), examDate, startTime, endTime);
        Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show();
    }

    public void showExamNotification(String examName, String date, String startTime, String endTime) {

        // Create notification builder with proper channel ID
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_notification).setContentTitle("Upcoming Exam Tomorrow").setContentText(examName).setStyle(new NotificationCompat.BigTextStyle().bigText(String.format("You have %s tomorrow\nDate: %s\nStart Time: %s\nEnd Time: %s", examName, date, startTime, endTime))).setPriority(NotificationCompat.PRIORITY_HIGH) // Higher priority
                .setVibrate(new long[]{0, 500, 250, 500}) // Add vibration
                .setAutoCancel(true);

        // Add sound
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);


        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = (examName + date + startTime).hashCode();

            try {
                notificationManager.notify(notificationId, builder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
} 