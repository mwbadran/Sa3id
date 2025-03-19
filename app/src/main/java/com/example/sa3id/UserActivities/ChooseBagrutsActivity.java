package com.example.sa3id.UserActivities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.sa3id.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseBagrutsActivity extends AppCompatActivity {

    private RadioGroup rgMathUnits, rgArabicUnits, rgHebrewUnits, rgEnglishUnits;
    private ChipGroup chipGroupMajors;
    private Button btnSaveSelections, btnShowAdvancedMode;
    private SwitchMaterial switchAdvancedMode;
    private CardView cardViewMandatory, cardViewMajors, cardViewAdvanced;
    private CardView examListContainer;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;

    private Map<String, Integer> selectedUnits = new HashMap<>();
    private List<String> selectedMajors = new ArrayList<>();
    private Map<String, List<String>> selectedExams = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_bagruts);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // If user is not logged in, redirect to sign in
            startActivity(new Intent(this, SignIn.class));
            finish();
            return;
        }

        initViews();
        setupRadioGroups();
        setupMajorsChips();
        setupAdvancedMode();
        setupButtons();

        // Check if this is an edit operation
        Intent intent = getIntent();
        if (intent.getBooleanExtra("isEdit", false)) {
            loadUserSelections();
        }
    }

    private void initViews() {
        // Mandatory subjects radio groups
        rgMathUnits = findViewById(R.id.rgMathUnits);
        rgArabicUnits = findViewById(R.id.rgArabicUnits);
        rgHebrewUnits = findViewById(R.id.rgHebrewUnits);
        rgEnglishUnits = findViewById(R.id.rgEnglishUnits);

        // Major selection
        chipGroupMajors = findViewById(R.id.chipGroupMajors);

        // Advanced mode
        switchAdvancedMode = findViewById(R.id.switchAdvancedMode);
        cardViewMandatory = findViewById(R.id.cardViewMandatory);
        cardViewMajors = findViewById(R.id.cardViewMajors);
        cardViewAdvanced = findViewById(R.id.cardViewAdvanced);
        examListContainer = findViewById(R.id.cardViewExamsList);

        // Buttons
        btnSaveSelections = findViewById(R.id.btnSaveSelections);
        btnShowAdvancedMode = findViewById(R.id.btnShowAdvancedMode);
    }

    private void setupRadioGroups() {
        // Set default selections and listeners

        // Math units
        rgMathUnits.setOnCheckedChangeListener((group, checkedId) -> {
            int units = 3; // Default
            if (checkedId == R.id.rbMath4) {
                units = 4;
            } else if (checkedId == R.id.rbMath5) {
                units = 5;
            }
            selectedUnits.put("math", units);
        });
        rgMathUnits.check(R.id.rbMath3); // Default selection
        selectedUnits.put("math", 3);

        // Arabic units
        rgArabicUnits.setOnCheckedChangeListener((group, checkedId) -> {
            int units = checkedId == R.id.rbArabic5 ? 5 : 3;
            selectedUnits.put("arabic", units);
        });
        rgArabicUnits.check(R.id.rbArabic3); // Default selection
        selectedUnits.put("arabic", 3);

        // Hebrew units
        rgHebrewUnits.setOnCheckedChangeListener((group, checkedId) -> {
            int units = checkedId == R.id.rbHebrew5 ? 5 : 3;
            selectedUnits.put("hebrew", units);
        });
        rgHebrewUnits.check(R.id.rbHebrew3); // Default selection
        selectedUnits.put("hebrew", 3);

        // English units
        rgEnglishUnits.setOnCheckedChangeListener((group, checkedId) -> {
            int units = 3; // Default
            if (checkedId == R.id.rbEnglish4) {
                units = 4;
            } else if (checkedId == R.id.rbEnglish5) {
                units = 5;
            }
            selectedUnits.put("english", units);
        });
        rgEnglishUnits.check(R.id.rbEnglish3); // Default selection
        selectedUnits.put("english", 3);
    }

    private void setupMajorsChips() {
        // List of available majors
        String[] majors = new String[]{
                "فيزياء", "كيمياء", "بيولوجيا", "علوم الحاسوب", "تاريخ", "جغرافيا",
                "اقتصاد", "علم الاجتماع", "فن", "موسيقى", "علم النفس"
        };

        for (String major : majors) {
            Chip chip = new Chip(this);
            chip.setText(major);
            chip.setCheckable(true);
            chip.setTextAppearance(R.style.ChipTextStyle);
            chip.setTypeface(getResources().getFont(R.font.rpt_bold));

            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
                chip.setTextColor(getResources().getColorStateList(R.color.black, null));
            }

            //chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color));
            //chip.setChipBackgroundColor(getResources().getColorStateList(R.color.black));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedMajors.add(major);
                } else {
                    selectedMajors.remove(major);
                }
            });

            chipGroupMajors.addView(chip);
        }
    }

    private void setupAdvancedMode() {
        cardViewAdvanced.setVisibility(View.GONE);

        switchAdvancedMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cardViewAdvanced.setVisibility(View.VISIBLE);
                setupExamsList();
            } else {
                cardViewAdvanced.setVisibility(View.GONE);
            }
        });

        btnShowAdvancedMode.setOnClickListener(v -> {
            switchAdvancedMode.setChecked(true);
        });
    }

    private void setupExamsList() {
        // Get exam data
        Map<String, List<Map<String, String>>> examsData = getExamsData();

        // Clear any existing views
        examListContainer.removeAllViews();

        // Create a vertical LinearLayout to hold all subject sections
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(16, 16, 16, 16);

        for (String subject : examsData.keySet()) {
            // Create a section for each subject
            TextView subjectTitle = new TextView(this);
            subjectTitle.setText(subject);
            subjectTitle.setTextSize(18);
            subjectTitle.setTypeface(getResources().getFont(R.font.rpt_bold));
            subjectTitle.setTextColor(getResources().getColor(R.color.white));
            subjectTitle.setPadding(0, 24, 0, 8);
            mainContainer.addView(subjectTitle);

            // Add a RecyclerView or scroll view for exams
            LinearLayout examContainer = new LinearLayout(this);
            examContainer.setOrientation(LinearLayout.VERTICAL);
            examContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // Add spacing between checkboxes
            for (Map<String, String> exam : examsData.get(subject)) {
                CheckBox examCheckBox = new CheckBox(this);
                examCheckBox.setText(exam.get("name") + " (" + exam.get("code") + ")");
                examCheckBox.setTypeface(getResources().getFont(R.font.rpt_bold));
                examCheckBox.setTextColor(getResources().getColor(R.color.white));
                // Add spacing between checkboxes
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 8, 0, 8);
                examCheckBox.setLayoutParams(params);

                String examId = exam.get("code");
                examCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (!selectedExams.containsKey(subject)) {
                        selectedExams.put(subject, new ArrayList<>());
                    }

                    if (isChecked) {
                        selectedExams.get(subject).add(examId);
                    } else {
                        selectedExams.get(subject).remove(examId);
                    }
                });

                examContainer.addView(examCheckBox);
            }

            mainContainer.addView(examContainer);

            // Add a divider after each subject except the last one
            if (!subject.equals(examsData.keySet().toArray()[examsData.size() - 1])) {
                View divider = new View(this);
                //divider.setBackgroundColor(getResources().getColor(R.color.divider_color));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                dividerParams.setMargins(0, 16, 0, 16);
                divider.setLayoutParams(dividerParams);
                mainContainer.addView(divider);
            }
        }

        examListContainer.addView(mainContainer);
    }

    private Map<String, List<Map<String, String>>> getExamsData() {
        // This would be replaced with your actual Excel data
        Map<String, List<Map<String, String>>> examsData = new HashMap<>();

        // Math exams
        List<Map<String, String>> mathExams = new ArrayList<>();
        Map<String, String> exam1 = new HashMap<>();
        exam1.put("code", "035801");
        exam1.put("name", "رياضيات 3 وحدات جزء أ");
        mathExams.add(exam1);

        Map<String, String> exam2 = new HashMap<>();
        exam2.put("code", "035802");
        exam2.put("name", "رياضيات 3 وحدات جزء ب");
        mathExams.add(exam2);

        Map<String, String> exam3 = new HashMap<>();
        exam3.put("code", "035804");
        exam3.put("name", "رياضيات 4 وحدات");
        mathExams.add(exam3);

        Map<String, String> exam4 = new HashMap<>();
        exam4.put("code", "035806");
        exam4.put("name", "رياضيات 5 وحدات جزء أ");
        mathExams.add(exam4);

        Map<String, String> exam5 = new HashMap<>();
        exam5.put("code", "035807");
        exam5.put("name", "رياضيات 5 وحدات جزء ب");
        mathExams.add(exam5);

        examsData.put("رياضيات", mathExams);

        // Add similar lists for other subjects
        // English
        List<Map<String, String>> englishExams = new ArrayList<>();
        Map<String, String> engExam1 = new HashMap<>();
        engExam1.put("code", "016381");
        engExam1.put("name", "انجليزي 3 وحدات");
        englishExams.add(engExam1);

        Map<String, String> engExam2 = new HashMap<>();
        engExam2.put("code", "016384");
        engExam2.put("name", "انجليزي 4 وحدات");
        englishExams.add(engExam2);

        Map<String, String> engExam3 = new HashMap<>();
        engExam3.put("code", "016385");
        engExam3.put("name", "انجليزي 5 وحدات");
        englishExams.add(engExam3);

        examsData.put("انجليزي", englishExams);

        // Arabic exams
        List<Map<String, String>> arabicExams = new ArrayList<>();
        Map<String, String> arExam1 = new HashMap<>();
        arExam1.put("code", "027301");
        arExam1.put("name", "عربي 3 وحدات");
        arabicExams.add(arExam1);

        Map<String, String> arExam2 = new HashMap<>();
        arExam2.put("code", "027501");
        arExam2.put("name", "عربي 5 وحدات جزء أ");
        arabicExams.add(arExam2);

        Map<String, String> arExam3 = new HashMap<>();
        arExam3.put("code", "027502");
        arExam3.put("name", "عربي 5 وحدات جزء ب");
        arabicExams.add(arExam3);

        examsData.put("عربي", arabicExams);

        // Hebrew exams
        List<Map<String, String>> hebrewExams = new ArrayList<>();
        Map<String, String> hebExam1 = new HashMap<>();
        hebExam1.put("code", "015301");
        // Hebrew exams
        hebrewExams = new ArrayList<>();
        hebExam1 = new HashMap<>();
        hebExam1.put("code", "015301");
        hebExam1.put("name", "عبري 3 وحدات");
        hebrewExams.add(hebExam1);

        Map<String, String> hebExam2 = new HashMap<>();
        hebExam2.put("code", "015501");
        hebExam2.put("name", "عبري 5 وحدات جزء أ");
        hebrewExams.add(hebExam2);

        Map<String, String> hebExam3 = new HashMap<>();
        hebExam3.put("code", "015502");
        hebExam3.put("name", "عبري 5 وحدات جزء ب");
        hebrewExams.add(hebExam3);

        examsData.put("عبري", hebrewExams);

        return examsData;
    }

    private void setupButtons() {
        btnSaveSelections.setOnClickListener(v -> saveUserSelections());
    }

    private void saveUserSelections() {
        // Create a map with all the user selections
        Map<String, Object> userBagruts = new HashMap<>();
        userBagruts.put("units", selectedUnits);
        userBagruts.put("majors", selectedMajors);

        if (switchAdvancedMode.isChecked()) {
            userBagruts.put("exams", selectedExams);
            userBagruts.put("advancedMode", true);
        } else {
            // Generate the exams based on selections
            generateExamsFromSelections();
            userBagruts.put("exams", selectedExams);
            userBagruts.put("advancedMode", false);
        }

        // Save to Firestore
        firestore.collection("Users").document(userId)
                .update("bagruts", userBagruts)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "تم حفظ اختياراتك بنجاح", Toast.LENGTH_SHORT).show();

                    // Redirect to main activity or appropriate screen
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في حفظ الاختيارات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void generateExamsFromSelections() {

        selectedExams = new HashMap<>();

        // Math exams based on selected units
        List<String> mathExams = new ArrayList<>();
        int mathUnits = selectedUnits.get("math");
        if (mathUnits == 3) {
            mathExams.add("035801");
            mathExams.add("035802");
        } else if (mathUnits == 4) {
            mathExams.add("035804");
        } else if (mathUnits == 5) {
            mathExams.add("035806");
            mathExams.add("035807");
        }
        selectedExams.put("رياضيات", mathExams);

        // English exams based on selected units
        List<String> englishExams = new ArrayList<>();
        int englishUnits = selectedUnits.get("english");
        if (englishUnits == 3) {
            englishExams.add("016381");
        } else if (englishUnits == 4) {
            englishExams.add("016384");
        } else if (englishUnits == 5) {
            englishExams.add("016385");
        }
        selectedExams.put("انجليزي", englishExams);

        // Arabic exams based on selected units
        List<String> arabicExams = new ArrayList<>();
        int arabicUnits = selectedUnits.get("arabic");
        if (arabicUnits == 3) {
            arabicExams.add("027301");
        } else if (arabicUnits == 5) {
            arabicExams.add("027501");
            arabicExams.add("027502");
        }
        selectedExams.put("عربي", arabicExams);

        // Hebrew exams based on selected units
        List<String> hebrewExams = new ArrayList<>();
        int hebrewUnits = selectedUnits.get("hebrew");
        if (hebrewUnits == 3) {
            hebrewExams.add("015301");
        } else if (hebrewUnits == 5) {
            hebrewExams.add("015501");
            hebrewExams.add("015502");
        }
        selectedExams.put("عبري", hebrewExams);

        // Add majors exams - all 5 units
        for (String major : selectedMajors) {
            List<String> majorExams = new ArrayList<>();

            // This is just an example - you would replace with actual codes
            if (major.equals("فيزياء")) {
                majorExams.add("036541");
                majorExams.add("036542");
            } else if (major.equals("كيمياء")) {
                majorExams.add("037581");
            } else if (major.equals("بيولوجيا")) {
                majorExams.add("043581");
            } else if (major.equals("علوم الحاسوب")) {
                majorExams.add("048581");
                majorExams.add("048582");
            }
            // Add similar mappings for other majors

            selectedExams.put(major, majorExams);
        }
    }

    private void loadUserSelections() {
        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("bagruts")) {
                        Map<String, Object> bagrutsData = (Map<String, Object>) documentSnapshot.get("bagruts");

                        if (bagrutsData != null) {
                            // Load units
                            Map<String, Long> units = (Map<String, Long>) bagrutsData.get("units");
                            if (units != null) {
                                for (String subject : units.keySet()) {
                                    int unitValue = units.get(subject).intValue();
                                    selectedUnits.put(subject, unitValue);

                                    // Update radio buttons
                                    switch (subject) {
                                        case "math":
                                            if (unitValue == 3) {
                                                rgMathUnits.check(R.id.rbMath3);
                                            } else if (unitValue == 4) {
                                                rgMathUnits.check(R.id.rbMath4);
                                            } else if (unitValue == 5) {
                                                rgMathUnits.check(R.id.rbMath5);
                                            }
                                            break;
                                        case "arabic":
                                            if (unitValue == 3) {
                                                rgArabicUnits.check(R.id.rbArabic3);
                                            } else if (unitValue == 5) {
                                                rgArabicUnits.check(R.id.rbArabic5);
                                            }
                                            break;
                                        case "hebrew":
                                            if (unitValue == 3) {
                                                rgHebrewUnits.check(R.id.rbHebrew3);
                                            } else if (unitValue == 5) {
                                                rgHebrewUnits.check(R.id.rbHebrew5);
                                            }
                                            break;
                                        case "english":
                                            if (unitValue == 3) {
                                                rgEnglishUnits.check(R.id.rbEnglish3);
                                            } else if (unitValue == 4) {
                                                rgEnglishUnits.check(R.id.rbEnglish4);
                                            } else if (unitValue == 5) {
                                                rgEnglishUnits.check(R.id.rbEnglish5);
                                            }
                                            break;
                                    }
                                }
                            }

                            // Load majors
                            List<String> majors = (List<String>) bagrutsData.get("majors");
                            if (majors != null) {
                                selectedMajors.addAll(majors);

                                // Update UI
                                for (int i = 0; i < chipGroupMajors.getChildCount(); i++) {
                                    Chip chip = (Chip) chipGroupMajors.getChildAt(i);
                                    if (selectedMajors.contains(chip.getText().toString())) {
                                        chip.setChecked(true);
                                    }
                                }
                            }

                            // Load advanced mode settings
                            Boolean advancedMode = (Boolean) bagrutsData.get("advancedMode");
                            if (advancedMode != null && advancedMode) {
                                switchAdvancedMode.setChecked(true);

                                // Load exams
                                Map<String, List<String>> exams = (Map<String, List<String>>) bagrutsData.get("exams");
                                if (exams != null) {
                                    selectedExams = exams;
                                    // UI will be updated when advanced mode is toggled
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في تحميل البيانات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Add import for LinearLayout
    private void addImportStatement() {
        // This is a placeholder method to remind you to add the following import
        // This method isn't actually called in the code
        // import android.widget.LinearLayout;
    }
}