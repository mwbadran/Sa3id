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
    private static final String EXAMS_REF = "exams";
    private final DatabaseReference databaseReference;

    public interface ExamCallback {
        void onExamsLoaded(List<Exam> exams);

        void onError(String error);
    }

    public ExamManager() {
        this.databaseReference = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference();
    }

    public void getExamsForSubject(String subject, final ExamCallback callback) {
        DatabaseReference examsRef = databaseReference.child(EXAMS_REF).child(subject);

        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exam> exams = new ArrayList<>();


                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    for (DataSnapshot examSnapshot : dateSnapshot.getChildren()) {
                        String examId = examSnapshot.getKey();
                        Exam exam = examSnapshot.getValue(Exam.class);
                        if (exam != null) {
                            exam.setExamId(examId);
                            exam.setDate(date);
                            exam.setSubject(subject);
                            exams.add(exam);
                        } else {
                        }
                    }
                }

                callback.onExamsLoaded(exams);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void getAllExams(final ExamCallback callback) {
        DatabaseReference examsRef = databaseReference.child(EXAMS_REF);

        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exam> allExams = new ArrayList<>();


                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subject = subjectSnapshot.getKey();

                    for (DataSnapshot dateSnapshot : subjectSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();

                        for (DataSnapshot examSnapshot : dateSnapshot.getChildren()) {
                            String examId = examSnapshot.getKey();
                            Exam exam = examSnapshot.getValue(Exam.class);
                            if (exam != null) {
                                exam.setExamId(examId);
                                exam.setDate(date);
                                exam.setSubject(subject);
                                allExams.add(exam);
                            }
                        }
                    }
                }

                callback.onExamsLoaded(allExams);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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

        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exam> allExams = new ArrayList<>();

                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subject = subjectSnapshot.getKey();
                    if (subjects.contains(subject)) {
                        for (DataSnapshot dateSnapshot : subjectSnapshot.getChildren()) {
                            String date = dateSnapshot.getKey();

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
                                }
                            }
                        }
                    }
                }

                callback.onExamsLoaded(allExams);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }
} 