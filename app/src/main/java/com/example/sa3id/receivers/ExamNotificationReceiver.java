package com.example.sa3id.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.sa3id.managers.NotificationManager;

public class ExamNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String examName = intent.getStringExtra("examName");
        String examDate = intent.getStringExtra("examDate");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");

        NotificationManager notificationManager = new NotificationManager(context);
        notificationManager.showExamNotification(examName, examDate, startTime, endTime);
    }
} 