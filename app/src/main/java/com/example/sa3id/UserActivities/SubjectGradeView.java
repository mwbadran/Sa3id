package com.example.sa3id.UserActivities;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sa3id.R;

public class SubjectGradeView extends LinearLayout {

    private TextView subjectNameView;
    private EditText gradeInput;
    private EditText unitsInput;

    public SubjectGradeView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.view_subject_grade, this, true);

        subjectNameView = findViewById(R.id.subjectNameView);
        gradeInput = findViewById(R.id.gradeInput);
        unitsInput = findViewById(R.id.unitsInput);
    }

    public String getSubjectName() {
        return subjectNameView.getText().toString();
    }

    public void setSubject(String subject) {
        subjectNameView.setText(subject);
    }

    public boolean hasValidGrade() {
        return !TextUtils.isEmpty(gradeInput.getText()) && !TextUtils.isEmpty(unitsInput.getText());
    }

    public double getGrade() {
        try {
            return Double.parseDouble(gradeInput.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getUnits() {
        try {
            return Integer.parseInt(unitsInput.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}