package com.example.sa3id.userActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.sa3id.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChooseBagrutsActivity extends AppCompatActivity {

    private RadioGroup rgMathUnits, rgArabicUnits, rgHebrewUnits, rgEnglishUnits, rgSector, rgReligion;
    private LinearLayout religionSection;
    private ChipGroup chipGroupMajors;
    private Button btnSaveSelections;
    private SwitchMaterial switchAdvancedMode;
    private CardView cardViewMandatory, cardViewMajors, cardViewAdvanced;
    private CardView examListContainer, sectorContainer;
    private LinearLayout mainContent;
    private boolean isNewCurriculum = false;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;
    private String selectedSector = "";

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
        hideAllContentUntilSectorSelected();
        
        // Show combined sector and curriculum dialog
        showSectorAndCurriculumDialog();
        
        // Initialize default religion selection (Islam)
        List<String> defaultReligionExams = new ArrayList<>();
        defaultReligionExams.add("47111"); // 20% داخلي مراقب
        defaultReligionExams.add("47183"); // 30% تقييم بديل
        defaultReligionExams.add("47115"); // 50% الإمتحان
        selectedExams.put("דת האסלאם", defaultReligionExams);

        // Setup religion selection
        rgReligion.setOnCheckedChangeListener((group, checkedId) -> {
            // Remove any existing religious studies
            selectedExams.remove("דת האסלאם");
            selectedExams.remove("דת נוצרית");

            List<String> religionExams = new ArrayList<>();
            if (checkedId == R.id.rbIslam) {
                if (isNewCurriculum) {
                    religionExams.add("47111"); // 20% داخلي مراقب
                    religionExams.add("47183"); // 30% تقييم بديل
                    religionExams.add("47115"); // 50% الإمتحان
                    religionExams.add("47157"); // 100% exam without tasks
                    religionExams.add("47158"); // Alternative full exam
                } else {
                    religionExams.add("47181"); // 70% إمتحان
                    religionExams.add("47183"); // 30% وظيفة
                }
                selectedExams.put("דת האסלאם", religionExams);
            } else if (checkedId == R.id.rbChristian) {
                religionExams.add("73111"); // 20% داخلي
                religionExams.add("73183"); // 30% وظيفة
                religionExams.add("73115"); // 50% امتحان
                selectedExams.put("דת נוצרית", religionExams);
            }

            if (switchAdvancedMode.isChecked()) {
                setupExamsList();
            }
        });

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
        // Sector selection
        sectorContainer = findViewById(R.id.cardViewSector);
        rgSector = findViewById(R.id.rgSector);
        mainContent = findViewById(R.id.mainContent);

        // Religion selection (for Arab sector)
        religionSection = findViewById(R.id.religionSection);
        rgReligion = findViewById(R.id.rgReligion);

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
    }

    private void hideAllContentUntilSectorSelected() {
        mainContent.setVisibility(View.GONE);
    }

    private void showSectorAndCurriculumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sector_curriculum, null);
        builder.setView(dialogView);
        
        final RadioGroup rgDialogSector = dialogView.findViewById(R.id.rgDialogSector);
        final RadioGroup rgDialogCurriculum = dialogView.findViewById(R.id.rgDialogCurriculum);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmSelection);
        
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        
        btnConfirm.setOnClickListener(v -> {
            // Get sector selection
            int sectorId = rgDialogSector.getCheckedRadioButtonId();
            if (sectorId == R.id.rbDialogArab) {
                selectedSector = "arab";
                religionSection.setVisibility(View.VISIBLE);
                rgSector.check(R.id.rbArab);
                setupArabDefaults();
            } else if (sectorId == R.id.rbDialogDruze) {
                selectedSector = "druze";
                religionSection.setVisibility(View.GONE);
                rgSector.check(R.id.rbDruze);
                setupDruzeDefaults();
            }
            
            // Get curriculum selection
            int curriculumId = rgDialogCurriculum.getCheckedRadioButtonId();
            isNewCurriculum = (curriculumId == R.id.rbDialogNewCurriculum);
            
            // Apply selections and show main content
            addDefaultSubjectsAndExams();
            mainContent.setVisibility(View.VISIBLE);
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void setupArabDefaults() {
        // Set default Hebrew selection for Arabs
        rgHebrewUnits.check(R.id.rbHebrew3);
        selectedUnits.put("hebrew", 3);
        autoSelectExamsForSubject("עברית");

        // Set default Arabic selection for Arabs
        rgArabicUnits.check(R.id.rbArabic3);
        selectedUnits.put("arabic", 3);
        autoSelectExamsForSubject("ערבית");

        // Set default Math selection
        rgMathUnits.check(R.id.rbMath3);
        selectedUnits.put("math", 3);
        autoSelectExamsForSubject("מתמטיקה");

        // Set default English selection
        rgEnglishUnits.check(R.id.rbEnglish3);
        selectedUnits.put("english", 3);
        autoSelectExamsForSubject("אנגלית");

        // Set default religion selection (Islam)
        if (rgReligion != null) {
            rgReligion.check(R.id.rbIslam);
            autoSelectExamsForSubject("דת האסלאם");
        }
    }

    private void setupDruzeDefaults() {
        // Set default Hebrew selection for Druze
        rgHebrewUnits.check(R.id.rbHebrew3);
        selectedUnits.put("hebrew", 3);
        autoSelectExamsForSubject("עברית");

        // Set default Arabic selection for Druze
        rgArabicUnits.check(R.id.rbArabic3);
        selectedUnits.put("arabic", 3);
        autoSelectExamsForSubject("ערבית");

        // Set default Math selection
        rgMathUnits.check(R.id.rbMath3);
        selectedUnits.put("math", 3);
        autoSelectExamsForSubject("מתמטיקה");

        // Set default English selection
        rgEnglishUnits.check(R.id.rbEnglish3);
        selectedUnits.put("english", 3);
        autoSelectExamsForSubject("אנגלית");
    }

    private void addDefaultSubjectsAndExams() {
        // Add אזרחות (Citizenship)
        autoSelectExamsForSubject("אזרחות");

        // Add היסטוריה (History) based on sector
        autoSelectExamsForSubject("היסטוריה");

        // Make sure Math, English, Arabic and Hebrew are set
        autoSelectExamsForSubject("מתמטיקה");
        autoSelectExamsForSubject("אנגלית");
        autoSelectExamsForSubject("עברית");
        autoSelectExamsForSubject("ערבית");

        // Add religion exams for Arab sector
        if (selectedSector.equals("arab")) {
            int religionId = rgReligion.getCheckedRadioButtonId();
            if (religionId == R.id.rbIslam) {
                autoSelectExamsForSubject("דת האסלאם");
            } else if (religionId == R.id.rbChristian) {
                autoSelectExamsForSubject("דת נוצרית");
            }
        }
    }

    private void setupRadioGroups() {
        // Math units
        rgMathUnits.setOnCheckedChangeListener((group, checkedId) -> {
            int units = 3; // Default
            if (checkedId == R.id.rbMath4) {
                units = 4;
            } else if (checkedId == R.id.rbMath5) {
                units = 5;
            }
            selectedUnits.put("math", units);
            autoSelectExamsForSubject("מתמטיקה");
        });
        rgMathUnits.check(R.id.rbMath3); // Default selection

        // Arabic units
        rgArabicUnits.setOnCheckedChangeListener((group, checkedId) -> {
            int units = checkedId == R.id.rbArabic5 ? 5 : 3;
            selectedUnits.put("arabic", units);
            autoSelectExamsForSubject("ערבית");
        });
        rgArabicUnits.check(R.id.rbArabic3); // Default selection

        // Hebrew units
        rgHebrewUnits.setOnCheckedChangeListener((group, checkedId) -> {
            int units = checkedId == R.id.rbHebrew5 ? 5 : 3;
            selectedUnits.put("hebrew", units);
            autoSelectExamsForSubject("עברית");
        });
        rgHebrewUnits.check(R.id.rbHebrew3); // Default selection

        // English units
        rgEnglishUnits.setOnCheckedChangeListener((group, checkedId) -> {
            int units = 3; // Default
            if (checkedId == R.id.rbEnglish4) {
                units = 4;
            } else if (checkedId == R.id.rbEnglish5) {
                units = 5;
            }
            selectedUnits.put("english", units);
            autoSelectExamsForSubject("אנגלית");
        });
        rgEnglishUnits.check(R.id.rbEnglish3); // Default selection
    }

    private void setupMajorsChips() {
        // List of available majors (excluding mandatory subjects)
        List<String> majors = new ArrayList<>();

        // Common subjects for both sectors
        majors.add("מדעי המחשב");
        majors.add("אמנות שימושית");
        majors.add("תיירות");
        majors.add("מערכות חשמל");
        majors.add("מכניקה הנדסית");
        majors.add("אלקטרוניקה ומחשבים");
        majors.add("רוסית");
        majors.add("גרמנית");
        majors.add("מדעי המדינה");
        majors.add("סוציולוגיה");
        //majors.add("שיטות מחקר");
        majors.add("כלכלה");
        majors.add("מדעי הסביבה");
        majors.add("גאוגרפיה");
        majors.add("מוזיקה");
        majors.add("חקלאות");
        majors.add("ביולוגיה");
        majors.add("חינוך גופן");
        majors.add("כימיה");
        majors.add("פיזיקה");

        // Remove chips
        chipGroupMajors.removeAllViews();

        // Add chips for each major
        for (String major : majors) {
            // Skip subjects that are sector-specific and shouldn't be shown
            if (selectedSector.equals("druze") &&
                    (major.equals("דת האסלאם") || major.equals("דת נוצרית"))) {
                continue;
            }
            if (selectedSector.equals("arab") && major.equals("מורשת הדרוזים")) {
                continue;
            }

            // Skip for Jews
            if (major.contains("ליהודים")) {
                continue;
            }

            Chip chip = new Chip(this);
            chip.setText(major);
            chip.setCheckable(true);
            chip.setTextAppearance(this, R.style.ChipTextStyle);
            chip.setTypeface(getResources().getFont(R.font.rpt_bold));

            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
                chip.setTextColor(getResources().getColorStateList(R.color.black, null));
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedMajors.add(major);
                    autoSelectExamsForSubject(major);
                } else {
                    selectedMajors.remove(major);
                    selectedExams.remove(major);
                }
                if (switchAdvancedMode.isChecked()) {
                    setupExamsList();
                }
            });

            chipGroupMajors.addView(chip);
        }
    }

    private void autoSelectExamsForSubject(String subject) {
        List<String> defaultExams = new ArrayList<>();

        switch (subject) {
            case "מתמטיקה":
                if (selectedUnits.get("math") == 5) {
                    defaultExams.add("35571"); // 65% الإمتحان الأول
                    defaultExams.add("35572"); // 35% الإمتحان الثاني
                } else if (selectedUnits.get("math") == 4) {
                    defaultExams.add("35471"); // 65% الإمتحان الأول
                    defaultExams.add("35472"); // 35% الإمتحان الثاني
                } else { // 3 units
                    defaultExams.add("35371"); // 35% الإمتحان الأول
                    defaultExams.add("35372"); // 35% الإمتحان الثاني
                    defaultExams.add("35373"); // 30% الإمتحان الثالث
                }
                break;

            case "עברית":
                if (selectedUnits.get("hebrew") == 3) {
                    defaultExams.add("14371"); // 50% الإمتحان
                    defaultExams.add("14386"); // 20% iTest
                    defaultExams.add("14383"); // 30% تقييم بديل (داخلي)
                } else if (selectedUnits.get("hebrew") == 5) {
                    defaultExams.add("14371"); // 30% الإمتحان
                    defaultExams.add("14386"); // 12% iTest
                    defaultExams.add("14383"); // 18% تقييم بديل (داخلي)
                    defaultExams.add("14281"); // 28% الإمتحان
                    defaultExams.add("14283"); // 12% الوظيفة
                }
                break;

            case "אנגלית":
                if (selectedUnits.get("english") == 3) {
                    defaultExams.add("16381"); // 27% A
                    defaultExams.add("16383"); // 26% תלקיט
                    defaultExams.add("16382"); // 27% C
                    defaultExams.add("16385"); // 20% شفهي
                } else if (selectedUnits.get("english") == 4) {
                    defaultExams.add("16471"); // 27% E
                    defaultExams.add("16483"); // 26% תלקיט
                    defaultExams.add("16382"); // 27% C
                    defaultExams.add("16487"); // 20% iTest
                    defaultExams.add("16486"); // Alternative iTest
                } else if (selectedUnits.get("english") == 5) {
                    defaultExams.add("16471"); // 27% E
                    defaultExams.add("16583"); // 26% F
                    defaultExams.add("16582"); // 27% G
                    defaultExams.add("16587"); // 20% iTest
                    defaultExams.add("16586"); // Alternative iTest
                }
                break;

            case "ערבית":
                if (selectedUnits.get("arabic") == 3) {
                    defaultExams.add("20181"); // 20% الأدب
                    defaultExams.add("20381"); // 50% القواعد
                    defaultExams.add("20383"); // 30% الداخلي
                } else if (selectedUnits.get("arabic") == 5) {
                    defaultExams.add("20181"); // 12% الأدب
                    defaultExams.add("20381"); // 30% القواعد
                    defaultExams.add("20383"); // 18% الداخلي
                    defaultExams.add("20271"); // 28% الإمتحان
                    defaultExams.add("20281"); // Alternative exam
                    defaultExams.add("20273"); // 12% الوظيفة
                    defaultExams.add("20283"); // Alternative assignment
                }
                break;

            case "היסטוריה":
                if (isNewCurriculum) {
                    defaultExams.add("23211"); // 17% المهمة الأولى
                    defaultExams.add("23221"); // Alternative task 1
                    defaultExams.add("23212"); // 18% المهمة الثانية
                    defaultExams.add("23222"); // Alternative task 2
                    defaultExams.add("23283"); // 30% تقييم بديل
                    defaultExams.add("23215"); // 35% الإمتحان
                    defaultExams.add("23225"); // Alternative exam
                    defaultExams.add("23257"); // 100% exam without tasks
                    defaultExams.add("23258"); // Alternative full exam
                } else {
                    defaultExams.add("23281"); // 70% الإمتحان
                    defaultExams.add("23283"); // 30% تقييم بديل
                }
                break;

            case "אזרחות":
                if (isNewCurriculum) {
                    defaultExams.add("34211"); // 17% المهمة الأولى
                    defaultExams.add("34212"); // 18% المهمة الثانية
                    defaultExams.add("34273"); // 30% تقييم بديل
                    defaultExams.add("34225"); // 35% الإمتحان
                    defaultExams.add("34257"); // 100% exam without tasks
                    defaultExams.add("34258"); // Alternative full exam
                } else {
                    defaultExams.add("34281"); // 80% الإمتحان
                    defaultExams.add("34283"); // 20% الوظيفة
                }
                break;

            case "פיזיקה":
                defaultExams.add("36361"); // 30% ميكانيكا
                defaultExams.add("36371"); // 25% كهرباء
                defaultExams.add("36376"); // 15% مختبر
                defaultExams.add("36283"); // 30% داخلي
                break;

            case "כימיה":
                defaultExams.add("37381"); // 55% الإمتحان
                defaultExams.add("37387"); // Alternative exam
                defaultExams.add("37388"); // 15% بروجكت
                defaultExams.add("37283"); // 30% وظيفة
                break;

            case "ביולוגיה":
                defaultExams.add("43381"); // 55% الإمتحان
                defaultExams.add("43387"); // Alternative exam
                defaultExams.add("43386"); // 15% مختبر
                defaultExams.add("43283"); // 30% بيوحيكر
                break;

            case "דת האסלאם":
                if (isNewCurriculum) {
                    defaultExams.add("47111"); // 20% داخلي مراقب
                    defaultExams.add("47183"); // 30% تقييم بديل
                    defaultExams.add("47115"); // 50% الإمتحان
                    defaultExams.add("47157"); // 100% exam without tasks
                    defaultExams.add("47158"); // Alternative full exam
                } else {
                    defaultExams.add("47181"); // 70% إمتحان
                    defaultExams.add("47183"); // 30% وظيفة
                }
                break;

            case "אלקטרוניקה ומחשבים":
                defaultExams.add("815381"); // 70% امتحان
                defaultExams.add("815283"); // 30% داخلي
                break;
        }

        if (!defaultExams.isEmpty()) {
            selectedExams.put(subject, defaultExams);
            if (switchAdvancedMode.isChecked()) {
                setupExamsList();
            }
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

    }

    private void setupExamsList() {
        // Initialize selectedExams if null
        if (selectedExams == null) {
            selectedExams = new HashMap<>();
        }

        // Get exam data from Firebase
        DatabaseReference examsRef = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference().child("exams");
        Toast.makeText(this, "Loading exams...", Toast.LENGTH_SHORT).show();

        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(ChooseBagrutsActivity.this, "No exams found in database", Toast.LENGTH_SHORT).show();
                    return;
                }

                examListContainer.removeAllViews();
                LinearLayout mainContainer = new LinearLayout(ChooseBagrutsActivity.this);
                mainContainer.setOrientation(LinearLayout.VERTICAL);
                mainContainer.setPadding(16, 16, 16, 16);

                int examCount = 0;
                DataSnapshot otherSnapshot = null;

                // First process all subjects except "אחר"
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subject = subjectSnapshot.getKey();
                    
                    if ("אחר".equals(subject)) {
                        otherSnapshot = subjectSnapshot;
                        continue;
                    }

                    examCount += addSubjectToContainer(mainContainer, subject, subjectSnapshot);
                }

                // Add "אחר" at the end if it exists
                if (otherSnapshot != null) {
                    examCount += addSubjectToContainer(mainContainer, "אחר", otherSnapshot);
                }

                examListContainer.addView(mainContainer);

                if (examCount == 0) {
                    TextView noExamsText = new TextView(ChooseBagrutsActivity.this);
                    noExamsText.setText("No exams available. Please check your sector selection.");
                    noExamsText.setTextSize(16);
                    noExamsText.setTypeface(getResources().getFont(R.font.rpt_bold));
                    noExamsText.setTextColor(getResources().getColor(R.color.white, null));
                    noExamsText.setPadding(16, 16, 16, 16);
                    examListContainer.addView(noExamsText);
                } else {
                    Toast.makeText(ChooseBagrutsActivity.this,
                            "Loaded " + examCount + " exams", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChooseBagrutsActivity.this,
                        "Error loading exams: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int addSubjectToContainer(LinearLayout container, String subject, DataSnapshot subjectSnapshot) {
        if (!isSubjectRelevantToSector(subject)) {
            return 0;
        }

        int examCount = 0;

        // Create subject header
        TextView subjectTitle = new TextView(this);
        subjectTitle.setText(subject);
        subjectTitle.setTextSize(18);
        subjectTitle.setTypeface(getResources().getFont(R.font.rpt_bold));
        subjectTitle.setTextColor(getResources().getColor(R.color.white, null));
        subjectTitle.setPadding(0, 24, 0, 8);
        container.addView(subjectTitle);

        // Container for exams
        LinearLayout examContainer = new LinearLayout(this);
        examContainer.setOrientation(LinearLayout.VERTICAL);
        examContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        for (DataSnapshot dateSnapshot : subjectSnapshot.getChildren()) {
            for (DataSnapshot examSnapshot : dateSnapshot.getChildren()) {
                String examId = examSnapshot.getKey();
                String examName = examSnapshot.child("examName").getValue(String.class);

                if (examName == null) {
                    continue;
                }

                // Skip Jewish-specific exams in "אחר"
                if (subject.equals("אחר") && (
                        examName.contains("תנ\"ך") ||
                                examName.contains("ספרות") ||
                                examName.contains("מחשבת ישראל") ||
                                examName.contains("תלמוד"))) {
                    continue;
                }

                CheckBox examCheckBox = new CheckBox(this);
                examCheckBox.setText(examName + " (" + examId + ")");
                examCheckBox.setTypeface(getResources().getFont(R.font.rpt_bold));
                examCheckBox.setTextColor(getResources().getColor(R.color.white));

                // Set checkbox state based on selectedExams
                boolean checked = selectedExams.containsKey(subject) &&
                        selectedExams.get(subject).contains(examId);
                examCheckBox.setChecked(checked);

                examCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!selectedExams.containsKey(subject)) {
                            selectedExams.put(subject, new ArrayList<>());
                        }
                        if (!selectedExams.get(subject).contains(examId)) {
                            selectedExams.get(subject).add(examId);
                        }
                    } else {
                        if (selectedExams.containsKey(subject)) {
                            selectedExams.get(subject).remove(examId);
                        }
                    }
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 8, 0, 8);
                examCheckBox.setLayoutParams(params);
                examContainer.addView(examCheckBox);
                examCount++;
            }
        }

        container.addView(examContainer);

        // Add divider
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dividerParams.setMargins(0, 16, 0, 16);
        divider.setLayoutParams(dividerParams);
        container.addView(divider);

        return examCount;
    }

    private boolean isSubjectRelevantToSector(String subject) {
        if (selectedSector.equals("arab")) {
            return !subject.contains("דרוזים") && !subject.contains("יהודים");
        } else if (selectedSector.equals("druze")) {
            return !subject.contains("ערבים") &&
                    !subject.contains("האסלאם") &&
                    !subject.contains("נוצרית") && !subject.contains("יהודים");
        }
        return true;
    }

    private void setupButtons() {
        btnSaveSelections.setOnClickListener(v -> saveUserSelections());
    }

    private void saveUserSelections() {
        // Create a map with all the user selections
        Map<String, Object> userBagruts = new HashMap<>();
        userBagruts.put("sector", selectedSector);
        userBagruts.put("units", selectedUnits);
        userBagruts.put("majors", selectedMajors);
        userBagruts.put("exams", selectedExams);
        userBagruts.put("advancedMode", switchAdvancedMode.isChecked());

        // Save to Firestore
        firestore.collection("Users").document(userId)
                .update("bagruts", userBagruts)
                .addOnSuccessListener(aVoid -> {
                    // Also save to Realtime Database for exam-specific data
                    DatabaseReference userRef = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK)
                            .getReference()
                            .child("users")
                            .child(userId);

                    Map<String, Object> examData = new HashMap<>();
                    examData.put("selectedExams", selectedExams);
                    examData.put("lastUpdated", ServerValue.TIMESTAMP);

                    userRef.updateChildren(examData)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "تم حفظ اختياراتك بنجاح", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "فشل في حفظ الامتحانات: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في حفظ الاختيارات: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserSelections() {
        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("bagruts")) {
                        Map<String, Object> bagrutsData = (Map<String, Object>) documentSnapshot.get("bagruts");
                        if (bagrutsData != null) {
                            // Load sector
                            String sector = (String) bagrutsData.get("sector");
                            if (sector != null) {
                                selectedSector = sector;
                                if (sector.equals("arab")) {
                                    rgSector.check(R.id.rbArab);

                                    // Load religion selection
                                    Map<String, List<String>> exams = (Map<String, List<String>>) bagrutsData.get("exams");
                                    if (exams != null) {
                                        if (exams.containsKey("דת האסלאם")) {
                                            rgReligion.check(R.id.rbIslam);
                                        } else if (exams.containsKey("דת נוצרית")) {
                                            rgReligion.check(R.id.rbChristian);
                                        }
                                    }
                                } else if (sector.equals("druze")) {
                                    rgSector.check(R.id.rbDruze);
                                }
                            }

                            // Load units
                            Map<String, Long> units = (Map<String, Long>) bagrutsData.get("units");
                            if (units != null) {
                                for (String subject : units.keySet()) {
                                    int unitValue = units.get(subject).intValue();
                                    selectedUnits.put(subject, unitValue);
                                    updateUnitSelection(subject, unitValue);
                                }
                            }

                            // Load majors and exams
                            List<String> majors = (List<String>) bagrutsData.get("majors");
                            if (majors != null) {
                                selectedMajors.addAll(majors);
                                updateMajorSelections();
                            }

                            // Load advanced mode setting
                            Boolean advancedMode = (Boolean) bagrutsData.get("advancedMode");
                            if (advancedMode != null) {
                                switchAdvancedMode.setChecked(advancedMode);
                            }

                            // Load exams from Realtime Database
                            loadExamsFromRealtimeDB();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في تحميل البيانات: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadExamsFromRealtimeDB() {
        DatabaseReference userRef = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK)
                .getReference()
                .child("users")
                .child(userId)
                .child("selectedExams");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    selectedExams = (Map<String, List<String>>) snapshot.getValue();
                    if (switchAdvancedMode.isChecked()) {
                        setupExamsList();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChooseBagrutsActivity.this,
                        "Error loading exams: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUnitSelection(String subject, int units) {
        switch (subject) {
            case "math":
                if (units == 3) rgMathUnits.check(R.id.rbMath3);
                else if (units == 4) rgMathUnits.check(R.id.rbMath4);
                else if (units == 5) rgMathUnits.check(R.id.rbMath5);
                break;
            case "arabic":
                if (units == 3) rgArabicUnits.check(R.id.rbArabic3);
                else if (units == 5) rgArabicUnits.check(R.id.rbArabic5);
                break;
            case "hebrew":
                if (units == 3) rgHebrewUnits.check(R.id.rbHebrew3);
                else if (units == 5) rgHebrewUnits.check(R.id.rbHebrew5);
                break;
            case "english":
                if (units == 3) rgEnglishUnits.check(R.id.rbEnglish3);
                else if (units == 4) rgEnglishUnits.check(R.id.rbEnglish4);
                else if (units == 5) rgEnglishUnits.check(R.id.rbEnglish5);
                break;
        }
        autoSelectExamsForSubject(subject);
    }

    private void updateMajorSelections() {
        for (int i = 0; i < chipGroupMajors.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupMajors.getChildAt(i);
            if (selectedMajors.contains(chip.getText().toString())) {
                chip.setChecked(true);
            }
        }
    }

    private void saveSelectedSubjects() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to save your subjects", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUser.getUid())
                .child("selectedSubjects");

        // Convert selected exams to subjects list
        Set<String> selectedSubjects = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : selectedExams.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                selectedSubjects.add(entry.getKey());
            }
        }

        userRef.setValue(new ArrayList<>(selectedSubjects))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Subjects saved successfully", Toast.LENGTH_SHORT).show();
                    // Start ExamsActivity to show the selected exams
                    startActivity(new Intent(this, ExamsActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save subjects: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSelectedSubjects();
    }
}