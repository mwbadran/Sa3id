package com.example.sa3id.UserActivities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;

import java.util.HashMap;
import java.util.Map;

public class GradesCalculator extends BaseActivity {

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> subjectsAdapter;
    String[] subjectsArray = {
            "اللغة العربية",
            "اللغة الإنجليزية",
            "الرياضيات",
            "الفيزياء",
            "علم الحاسوب",
            "الكيمياء",
            "الأحياء",
            "التاريخ",
            "الجغرافيا",
            "المدنيات",
            "الدين الإسلامي",
            "العلوم الصحية",
            "ميكاترونيكا",
            "الكترونيكا",
            "اللغة العبرية"
    };

    // Views to be populated dynamically
    private LinearLayout dynamicViewsContainer, radioButtonsContainer;
    private TextView resultTextView;
    RadioGroup radioGroup;
    private Map<String, double[]> bagrutGradeWeights;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bagrutGradeWeights = new HashMap<>();
        initViews();


    }

    private void initViews() {
        btnCalculate = findViewById(R.id.calculateButton);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        dynamicViewsContainer = findViewById(R.id.dynamicViewsContainer);
        radioButtonsContainer = findViewById(R.id.radioButtonsContainer);
        resultTextView = findViewById(R.id.resultTextView);

        subjectsAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, subjectsArray);
        autoCompleteTextView.setAdapter(subjectsAdapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedSubject = (String) adapterView.getItemAtPosition(i);
                setupSubjectViews(selectedSubject);
            }
        });

        setupResultCalculation();
    }

    private void setupSubjectViews(String subject) {
        radioButtonsContainer.removeAllViews();
        dynamicViewsContainer.removeAllViews();

        switch (subject) {
            case "الرياضيات":
                setupMathViews();
                break;
            case "اللغة العربية":
                setupArabicViews();
                break;
            case "اللغة الإنجليزية":
                setupEnglishViews();
                break;
            case "الفيزياء":
                setupPhysicsViews();
                break;
            case "علم الحاسوب":
                setupComputerScienceViews();
                break;
            case "الكيمياء":
                setupChemistryViews();
                break;
            case "الأحياء":
                setupBiologyViews();
                break;
            case "التاريخ":
                setupHistoryViews();
                break;
            case "الجغرافيا":
                setupGeographyViews();
                break;
            case "المدنيات":
                setupCivicsViews();
                break;
            case "الدين الإسلامي":
                setupIslamicStudiesViews();
                break;
            case "العلوم الصحية":
                setupHealthSciencesViews();
                break;
            case "ميكاترونيكا":
                setupMechanicsViews();
                break;
            case "الكترونيكا":
                setupElectronicsViews();
                break;
            case "اللغة العبرية":
                setupHebrewViews();
                break;
            default:
                break;
        }
    }



    // helper Functions for making views:
    private Typeface loadTypeface() {
        return Typeface.createFromAsset(getAssets(), "rpt_bold.ttf");
    }

    private RadioButton createRadioButton(String text) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(text);
        radioButton.setTypeface(loadTypeface());
        return radioButton;
    }

    private EditText createEditText(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setTypeface(loadTypeface());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return editText;
    }


    private void addBagrutView(String labelText, double bagrutWeight, double magenWeight) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create label
        TextView label = new TextView(this);
        label.setText(labelText);
        label.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));  // Small width for label
        label.setTypeface(loadTypeface());
        label.setGravity(View.TEXT_ALIGNMENT_TEXT_START);
        label.setTextColor(getResources().getColor(android.R.color.white));

        EditText bagrutGrade = createEditText("علامة البجروت");
        EditText magenGrade = createEditText("علامة المجين");

        linearLayout.addView(label);
        linearLayout.addView(bagrutGrade);
        linearLayout.addView(magenGrade);

        dynamicViewsContainer.addView(linearLayout);
        bagrutGradeWeights.put(labelText, new double[]{bagrutWeight, magenWeight});
    }


    private void setupResultCalculation() {
        btnCalculate.setOnClickListener(v -> {
            double totalGrade = 0;
            int count = 0;

            for (int i = 0; i < dynamicViewsContainer.getChildCount(); i++) {
                LinearLayout modelLayout = (LinearLayout) dynamicViewsContainer.getChildAt(i);

                // Get the Bagrut and Magen grade inputs
                EditText bagrutInput = (EditText) modelLayout.getChildAt(1);
                EditText magenInput = (EditText) modelLayout.getChildAt(2);

                String bagrutStr = bagrutInput.getText().toString();
                String magenStr = magenInput.getText().toString();

                // Check if both grades are entered
                if (!bagrutStr.isEmpty() && !magenStr.isEmpty()) {
                    double bagrutGrade = Double.parseDouble(bagrutStr);
                    double magenGrade = Double.parseDouble(magenStr);

                    // Check if both grades are between 55 and 100
                    if (bagrutGrade >= 55 && bagrutGrade <= 100 && magenGrade >= 55 && magenGrade <= 100) {
                        // If valid, calculate the grade
                        double currentGrade = calculateBagrutGrade(modelLayout, bagrutGrade, magenGrade);
                        totalGrade += currentGrade;
                        count++;
                    } else {
                        // If any grade is invalid, show a Toast and stop the calculation
                        Toast.makeText(GradesCalculator.this, "يرجى إدخال درجات بين 55 و 100 لكل من البجروت والمجين", Toast.LENGTH_SHORT).show();
                        return; // Stop further processing and exit the method
                    }
                } else {
                    // If grades are missing
                    Toast.makeText(GradesCalculator.this, "يرجى إدخال العلامات لجميع النماذج", Toast.LENGTH_SHORT).show();
                    return; // Stop further processing and exit the method
                }
            }

            // Calculate the average if valid grades were entered
            if (count > 0) {
                double averageGrade = totalGrade / count;
                resultTextView.setText("العلامة النهائية:" + averageGrade);
            }
        });
    }


    private double calculateBagrutGrade(LinearLayout modelLayout, double bagrut, double magen) {
        String model = ((TextView) modelLayout.getChildAt(0)).getText().toString();
        double[] weights = bagrutGradeWeights.get(model);

        if (weights == null) {
            return 0;
        }

        return (bagrut * weights[0]) + (magen * weights[1]);
    }

    //subjects functions:
    private void setupMathViews() {
        radioGroup = new RadioGroup(this);
        RadioButton rb3 = createRadioButton("3 وحدات");
        RadioButton rb4 = createRadioButton("4 وحدات");
        RadioButton rb5 = createRadioButton("5 وحدات");

        radioGroup.addView(rb3);
        radioGroup.addView(rb4);
        radioGroup.addView(rb5);
        radioButtonsContainer.addView(radioGroup);

        dynamicViewsContainer.removeAllViews();


        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            dynamicViewsContainer.removeAllViews();

            if (checkedId == rb3.getId()) {
                addBagrutView("العلامة في النموذج الأول (801)", 0.25, 0.35);
                addBagrutView("العلامة في النموذج الثاني (802)", 0.5, 0.35);
                addBagrutView("العلامة في النموذج الثالث (803)", 0.5, 0.4);
            } else if (checkedId == rb4.getId()) {
                addBagrutView("العلامة في النموذج الأول (804)", 0.5, 0.65);
                addBagrutView("العلامة في النموذج الثاني (805)", 0.5, 0.35);
            } else if (checkedId == rb5.getId()) {

                addBagrutView("العلامة في النموذج الأول (806)", 0.5, 0.6);
                addBagrutView("العلامة في النموذج الثاني (807)", 0.5, 0.4);
            }

        });
    }

    private void setupArabicViews() {
        radioGroup = new RadioGroup(this);
        RadioButton rb3 = createRadioButton("3 وحدات");
        RadioButton rb5 = createRadioButton("5 وحدات");

        radioGroup.addView(rb3);
        radioGroup.addView(rb5);
        radioButtonsContainer.addView(radioGroup);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            dynamicViewsContainer.removeAllViews();

            if (checkedId == rb3.getId()) {
                addBagrutView("الأدب", 0.2, 0.3);
                addBagrutView("القواعد", 0.5, 0.3);
                addBagrutView("البحث", 0.3, 0);
            } else if (checkedId == rb5.getId()) {
                addBagrutView("اول وحدة", 0.12, 0);
                addBagrutView("الثانية والثالثة", 0.3, 0);
                addBagrutView("الرابعة", 0.28, 0);
                addBagrutView("الخامسة", 0.12, 0);
                addBagrutView("البحث", 0.18, 0);
            }
        });
    }


    private void setupEnglishViews() {
        radioGroup = new RadioGroup(this);
        RadioButton rb3 = createRadioButton("3 وحدات");
        RadioButton rb4 = createRadioButton("4 وحدات");
        RadioButton rb5 = createRadioButton("5 وحدات");

        radioGroup.addView(rb3);
        radioGroup.addView(rb4);
        radioGroup.addView(rb5);
        radioButtonsContainer.addView(radioGroup);

        dynamicViewsContainer.removeAllViews();

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            dynamicViewsContainer.removeAllViews();

            if (checkedId == rb3.getId()) {
                // 3 Units English setup
                addBagrutView("نموذج A", 0.7, 0.3);  // Example with 70% Bagrut and 30% Magen
                addBagrutView("نموذج B (علامة داخلية log)", 0.0, 1.0); // Example with internal grade only
                addBagrutView("نموذج C", 0.7, 0.3);
                addBagrutView("الشفوي", 0.2, 0.0); // Oral grade only (20%)
            } else if (checkedId == rb4.getId()) {
                // 4 Units English setup
                addBagrutView("نموذج C", 0.7, 0.3);
                addBagrutView("نموذج D (log علامة داخلية)", 0.0, 1.0); // Internal grade only
                addBagrutView("نموذج E", 0.7, 0.3);
                addBagrutView("الشفوي", 0.2, 0.0); // Oral grade only (20%)
            } else if (checkedId == rb5.getId()) {
                // 5 Units English setup
                addBagrutView("نموذج E", 0.7, 0.3);
                addBagrutView("نموذج F (LOG, علامة داخلية)", 0.0, 1.0); // Internal grade only
                addBagrutView("نموذج G", 0.7, 0.3);
                addBagrutView("الشفوي", 0.2, 0.0); // Oral grade only (20%)
            }
        });
    }




    private void setupComputerScienceViews() {
    }

    private void setupHistoryViews() {
        addBagrutView("تاريخ", 0.7, 0.3);
        addBagrutView("بحث", 0.3, 0);
    }

    private void setupCivicsViews() {
        addBagrutView("مدنيات", 0.8, 0.2);
    }

    private void setupPhysicsViews() {
        addBagrutView("فيزياء", 0.55, 0.15);
        addBagrutView("معمل", 0.15, 0);
        addBagrutView("بحث داخلي", 0.3, 0);
    }

    private void setupChemistryViews() {
        addBagrutView("كيمياء", 0.55, 0.15);
        addBagrutView("معمل", 0.15, 0);
        addBagrutView("بحث داخلي", 0.3, 0);
    }

    private void setupBiologyViews() {
        addBagrutView("بيولوجيا", 0.55, 0.15);
        addBagrutView("معمل", 0.15, 0);
        addBagrutView("بحث داخلي", 0.3, 0);
    }



    private void setupGeographyViews() {
    }


    private void setupIslamicStudiesViews() {
        addBagrutView("دين", 0.7, 0.3);
    }


    private void setupHealthSciencesViews() {
    }

    private void setupMechanicsViews() {
    }

    private void setupElectronicsViews() {
    }

    private void setupHebrewViews() {
        radioGroup = new RadioGroup(this);
        RadioButton rb3 = createRadioButton("3 وحدات");
        RadioButton rb5 = createRadioButton("5 وحدات");

        radioGroup.addView(rb3);
        radioGroup.addView(rb5);
        radioButtonsContainer.addView(radioGroup);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            dynamicViewsContainer.removeAllViews();

            if (checkedId == rb3.getId()) {
                addBagrutView("3 وحدات", 0.7, 0.3);
                addBagrutView("بَעל-فَم", 0.3, 0);
            } else if (checkedId == rb5.getId()) {
                addBagrutView("اول 3 وحدات", 0.42, 0.28);
                addBagrutView("الوحدتين الرابعة والخامسة", 0.28, 0.18);
                addBagrutView("بَעל-فَم", 0.18, 0);
                addBagrutView("وظيفة داخلية", 0.12, 0);
            }
        });
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_grades_calculator;
    }
}
