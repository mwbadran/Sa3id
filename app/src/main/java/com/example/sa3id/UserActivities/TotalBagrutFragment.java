package com.example.sa3id.UserActivities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;

import com.example.sa3id.BagrutSubject;
import com.example.sa3id.BagrutSubjectCategory;
import com.example.sa3id.BagrutSubjectsManager;
import com.example.sa3id.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TotalBagrutFragment extends Fragment {
    private LinearLayout mandatoryContainer;
    private LinearLayout optionalContainer;
    private LinearLayout optionalSubjectsLayout;
    private Button calculateTotalButton;
    private TextView totalResultTextView;
    private ImageView expandArrow;
    private RadioGroup sectorGroup;
    private List<SubjectGradeView> subjectGradeViews;
    private boolean isOptionalExpanded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_total_bagrut, container, false);

        initViews(view);
        setupListeners();
        setupInitialState();

        return view;
    }

    private void initViews(View view) {
        mandatoryContainer = view.findViewById(R.id.mandatoryContainer);
        optionalContainer = view.findViewById(R.id.optionalContainer);
        optionalSubjectsLayout = view.findViewById(R.id.optionalSubjectsLayout);
        calculateTotalButton = view.findViewById(R.id.calculateTotalButton);
        totalResultTextView = view.findViewById(R.id.totalResultTextView);
        expandArrow = view.findViewById(R.id.expandArrow);
        sectorGroup = view.findViewById(R.id.sectorGroup);
        subjectGradeViews = new ArrayList<>();
    }

    private void setupListeners() {
        sectorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isArabicSector = checkedId == R.id.arabicSector;
            refreshSubjects(isArabicSector);
        });

        View optionalHeader = optionalContainer.findViewById(R.id.optionalHeader);
        optionalHeader.setOnClickListener(v -> toggleOptionalSubjects());

        calculateTotalButton.setOnClickListener(v -> calculateTotalBagrut());
    }

    private void setupInitialState() {
        optionalSubjectsLayout.setVisibility(View.GONE);
        refreshSubjects(true);
    }

    private void refreshSubjects(boolean isArabicSector) {
        mandatoryContainer.removeAllViews();
        optionalSubjectsLayout.removeAllViews();
        subjectGradeViews.clear();

        List<BagrutSubject> subjects = BagrutSubjectsManager.getBagrutSubjectsForSector(isArabicSector);

        // Add new views
        for (BagrutSubject subject : subjects) {
            SubjectGradeView subjectGradeView = new SubjectGradeView(requireContext());
            subjectGradeView.setSubject(subject.getName());

            if (subject.getCategory() == BagrutSubjectCategory.MANDATORY) {
                mandatoryContainer.addView(subjectGradeView);
            } else {
                optionalSubjectsLayout.addView(subjectGradeView);
            }

            subjectGradeViews.add(subjectGradeView);
        }

        if (isOptionalExpanded) {
            toggleOptionalSubjects();
        }
    }

    private void toggleOptionalSubjects() {
        isOptionalExpanded = !isOptionalExpanded;
        optionalSubjectsLayout.setVisibility(isOptionalExpanded ? View.VISIBLE : View.GONE);

        // Animate arrow rotation
        float rotation = isOptionalExpanded ? 180f : 0f;
        ObjectAnimator.ofFloat(expandArrow, "rotation", expandArrow.getRotation(), rotation)
                .setDuration(200)
                .start();
    }

    private void calculateTotalBagrut() {
        if (!validateInputs()) {
            return;
        }
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
        }
    }

    private boolean validateInputs() {
        boolean isArabicSector = sectorGroup.getCheckedRadioButtonId() == R.id.arabicSector;
        List<BagrutSubject> mandatorySubjects = BagrutSubjectsManager.getBagrutSubjectsForSector(isArabicSector)
                .stream()
                .filter(subject -> subject.getCategory() == BagrutSubjectCategory.MANDATORY)
                .collect(Collectors.toList());


        List<String> missingSubjects = new ArrayList<>();
        for (BagrutSubject subject : mandatorySubjects) {
            boolean found = false;
            for (SubjectGradeView subjectView : subjectGradeViews) {
                if (subjectView.getSubjectName().equals(subject.getName()) && subjectView.hasValidGrade()) {
                    found = true;
                    // Check grade range
                    double grade = subjectView.getGrade();
                    if (grade < 55 || grade > 100) {
                        showValidationError(
                                String.format("العلامة في %s يجب أن تكون بين 55 و 100", subject.getName()),
                                R.drawable.shrek_mad
                        );
                        return false;
                    }
                    break;
                }
            }
            if (!found) {
                missingSubjects.add(subject.getName());
            }
        }

        if (!missingSubjects.isEmpty()) {
            String missingSubjectsStr = String.join("، ", missingSubjects);
            showValidationError(
                    "يجب إدخال علامات المواضيع الإلزامية التالية:\n" + missingSubjectsStr,
                    R.drawable.shrek_mad
            );
            return false;
        }

        // Check all other entered grades (optional subjects)
        for (SubjectGradeView subjectView : subjectGradeViews) {
            if (subjectView.hasValidGrade()) {
                double grade = subjectView.getGrade();
                if (grade < 55 || grade > 100) {
                    showValidationError(
                            String.format("العلامة في %s يجب أن تكون بين 55 و 100", subjectView.getSubjectName()),
                            R.drawable.shrek_mad
                    );
                    return false;
                }
            }
        }

        return true;
    }

    private void showValidationError(String message, @DrawableRes int imageResId) {
        BagrutValidationDialog dialog = new BagrutValidationDialog(requireContext());
        dialog.show(message, imageResId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (expandArrow != null) {
            expandArrow.clearAnimation();
        }
    }



}