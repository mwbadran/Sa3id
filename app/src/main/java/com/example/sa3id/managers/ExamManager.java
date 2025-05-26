package com.example.sa3id.managers;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

import androidx.annotation.NonNull;
import android.util.Log;
import com.example.sa3id.models.Exam;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ExamManager {
    private static final String TAG = "ExamManager";
    private static final String EXAMS_REF = "exams";
    private final DatabaseReference databaseReference;
    
    // For debug
    private static final String FIREBASE_DEBUG_URL = "https://console.firebase.google.com/project/_/database/sa3id/data/";

    public interface ExamCallback {
        void onExamsLoaded(List<Exam> exams);
        void onError(String error);
    }

    public ExamManager() {
        this.databaseReference = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference();
        Log.d(TAG, "Initialized with database reference: " + databaseReference.toString());
    }

    public void getExamsForSubject(String subject, final ExamCallback callback) {
        DatabaseReference examsRef = databaseReference.child(EXAMS_REF).child(subject);
        Log.d(TAG, "Querying exams for subject: " + subject + " at path: " + examsRef.toString());
        
        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exam> exams = new ArrayList<>();
                
                Log.d(TAG, "Data exists for subject " + subject + ": " + dataSnapshot.exists() + 
                      ", has " + dataSnapshot.getChildrenCount() + " date entries");
                
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    Log.d(TAG, "Processing date: " + date + " with " + 
                          dateSnapshot.getChildrenCount() + " exams");
                    
                    for (DataSnapshot examSnapshot : dateSnapshot.getChildren()) {
                        String examId = examSnapshot.getKey();
                        Exam exam = examSnapshot.getValue(Exam.class);
                        if (exam != null) {
                            exam.setExamId(examId);
                            exam.setDate(date);
                            exam.setSubject(subject);
                            exams.add(exam);
                            Log.d(TAG, "Added exam: " + exam.getExamName() + " (ID: " + examId + ")");
                        } else {
                            Log.w(TAG, "Failed to parse exam with ID: " + examId);
                        }
                    }
                }
                
                Log.d(TAG, "Loaded " + exams.size() + " exams for subject: " + subject);
                callback.onExamsLoaded(exams);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading exams for subject " + subject + ": " + databaseError.getMessage());
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void getAllExams(final ExamCallback callback) {
        DatabaseReference examsRef = databaseReference.child(EXAMS_REF);
        Log.d(TAG, "Querying all exams at path: " + examsRef.toString());
        
        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exam> allExams = new ArrayList<>();
                
                Log.d(TAG, "Data exists: " + dataSnapshot.exists() + 
                      ", has " + dataSnapshot.getChildrenCount() + " subject entries");
                
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subject = subjectSnapshot.getKey();
                    Log.d(TAG, "Processing subject: " + subject);
                    
                    for (DataSnapshot dateSnapshot : subjectSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        Log.d(TAG, "  Processing date: " + date);
                        
                        for (DataSnapshot examSnapshot : dateSnapshot.getChildren()) {
                            String examId = examSnapshot.getKey();
                            Exam exam = examSnapshot.getValue(Exam.class);
                            if (exam != null) {
                                exam.setExamId(examId);
                                exam.setDate(date);
                                exam.setSubject(subject);
                                allExams.add(exam);
                                Log.d(TAG, "  Added exam: " + exam.getExamName() + " (ID: " + examId + ")");
                            } else {
                                Log.w(TAG, "  Failed to parse exam with ID: " + examId);
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Loaded " + allExams.size() + " exams in total");
                callback.onExamsLoaded(allExams);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading all exams: " + databaseError.getMessage());
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void getExamsForUserSubjects(Set<String> subjects, final ExamCallback callback) {
        if (subjects == null || subjects.isEmpty()) {
            callback.onExamsLoaded(new ArrayList<>());
            return;
        }

        DatabaseReference examsRef = databaseReference.child(EXAMS_REF);
        Log.d(TAG, "Fetching exams for subjects: " + subjects);

        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exam> allExams = new ArrayList<>();

                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subject = subjectSnapshot.getKey();
                    if (subjects.contains(subject)) {
                        // Iterate through dates
                        for (DataSnapshot dateSnapshot : subjectSnapshot.getChildren()) {
                            String date = dateSnapshot.getKey();
                            
                            // Iterate through exams for this date
                            for (DataSnapshot examSnapshot : dateSnapshot.getChildren()) {
                                String examId = examSnapshot.getKey();
                                
                                String examName = examSnapshot.child("examName").getValue(String.class);
                                String startHour = examSnapshot.child("startHour").getValue(String.class);
                                String endHour = examSnapshot.child("endHour").getValue(String.class);
                                String duration = examSnapshot.child("duration").getValue(String.class);
                                String endHour25 = examSnapshot.child("endHour25").getValue(String.class);
                                String endHour33 = examSnapshot.child("endHour33").getValue(String.class);
                                String endHour50 = examSnapshot.child("endHour50").getValue(String.class);

                                if (examName != null && startHour != null && endHour != null) {
                                    Exam exam = new Exam();
                                    exam.setExamId(examId);
                                    exam.setSubject(subject);
                                    exam.setExamName(examName);
                                    exam.setDate(date);
                                    exam.setStartHour(startHour);
                                    exam.setEndHour(endHour);
                                    exam.setDuration(duration);
                                    exam.setEndHour25(endHour25);
                                    exam.setEndHour33(endHour33);
                                    exam.setEndHour50(endHour50);
                                    
                                    allExams.add(exam);
                                    Log.d(TAG, "Added exam: " + examName + " for subject: " + subject + " on date: " + date);
                                }
                            }
                        }
                    }
                }

                Log.d(TAG, "Total exams loaded: " + allExams.size());
                callback.onExamsLoaded(allExams);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading exams: " + databaseError.getMessage());
                callback.onError(databaseError.getMessage());
            }
        });
    }
} 