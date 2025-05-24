package com.example.sa3id.managers;

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
        // Use the specific Firebase URL from Constants
        this.databaseReference = FirebaseDatabase.getInstance("https://sa3idsite-default-rtdb.europe-west1.firebasedatabase.app").getReference();
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

    public void getExamsForUserSubjects(Set<String> userSubjects, final ExamCallback callback) {
        if (userSubjects == null || userSubjects.isEmpty()) {
            Log.w(TAG, "No user subjects provided, cannot load exams");
            callback.onExamsLoaded(new ArrayList<>());
            return;
        }
        
        DatabaseReference examsRef = databaseReference.child(EXAMS_REF);
        Log.d(TAG, "Querying exams for user subjects: " + userSubjects + " at path: " + examsRef.toString());
        
        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exam> userExams = new ArrayList<>();
                
                Log.d(TAG, "Data exists: " + dataSnapshot.exists() + 
                      ", has " + dataSnapshot.getChildrenCount() + " subject entries");
                
                int subjectsFound = 0;
                int subjectsWithoutData = 0;
                
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subject = subjectSnapshot.getKey();
                    Log.d(TAG, "Found subject in Firebase: " + subject + ", is in user subjects: " + 
                          userSubjects.contains(subject));
                    
                    if (userSubjects.contains(subject)) {
                        subjectsFound++;
                        if (subjectSnapshot.getChildrenCount() == 0) {
                            subjectsWithoutData++;
                            Log.d(TAG, "No exams found for subject: " + subject);
                            continue;
                        }
                        
                        for (DataSnapshot dateSnapshot : subjectSnapshot.getChildren()) {
                            String date = dateSnapshot.getKey();
                            Log.d(TAG, "  Processing date: " + date + " with " + 
                                  dateSnapshot.getChildrenCount() + " exams");
                            
                            for (DataSnapshot examSnapshot : dateSnapshot.getChildren()) {
                                String examId = examSnapshot.getKey();
                                Exam exam = examSnapshot.getValue(Exam.class);
                                if (exam != null) {
                                    exam.setExamId(examId);
                                    exam.setDate(date);
                                    exam.setSubject(subject);
                                    userExams.add(exam);
                                    Log.d(TAG, "  Added exam: " + exam.getExamName() + " (ID: " + examId + ")");
                                } else {
                                    Log.w(TAG, "  Failed to parse exam with ID: " + examId);
                                }
                            }
                        }
                    }
                }
                
                Log.d(TAG, String.format("Loaded %d exams for %d/%d subjects (%d had no data)", 
                        userExams.size(), subjectsFound, userSubjects.size(), subjectsWithoutData));
                callback.onExamsLoaded(userExams);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading exams for user subjects: " + databaseError.getMessage());
                callback.onError(databaseError.getMessage());
            }
        });
    }
} 