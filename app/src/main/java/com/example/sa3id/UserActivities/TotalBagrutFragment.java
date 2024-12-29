package com.example.sa3id.UserActivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.sa3id.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TotalBagrutFragment extends Fragment {

    private LinearLayout subjectsContainer;
    private Button calculateTotalButton;
    private TextView totalResultTextView;
    private List<SubjectGradeView> subjectGradeViews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_total_bagrut, container, false);

        subjectsContainer = view.findViewById(R.id.subjectsContainer);
        calculateTotalButton = view.findViewById(R.id.calculateTotalButton);
        totalResultTextView = view.findViewById(R.id.totalResultTextView);
        subjectGradeViews = new ArrayList<>();

        setupSubjectInputs();

        calculateTotalButton.setOnClickListener(v -> calculateTotalBagrut());

        return view;
    }

    private void setupSubjectInputs() {
        String[] subjects = getResources().getStringArray(R.array.subjects);
        for (String subject : subjects) {
            SubjectGradeView subjectGradeView = new SubjectGradeView(requireContext());
            subjectGradeView.setSubject(subject);
            subjectsContainer.addView(subjectGradeView);
            subjectGradeViews.add(subjectGradeView);
        }
    }

    private void calculateTotalBagrut() {
        double totalGrade = 0;
        int totalUnits = 0;

        for (SubjectGradeView subjectView : subjectGradeViews) {
            if (subjectView.hasValidGrade()) {
                double grade = subjectView.getGrade();
                int units = subjectView.getUnits();
                totalGrade += grade * units;
                totalUnits += units;
            }
        }

        if (totalUnits > 0) {
            double average = totalGrade / totalUnits;
            totalResultTextView.setText(String.format(Locale.getDefault(),
                    "معدل البجروت: %.2f", average));
        } else {
            totalResultTextView.setText("الرجاء إدخال العلامات");
        }
    }
}