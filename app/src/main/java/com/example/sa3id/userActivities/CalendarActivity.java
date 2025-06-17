package com.example.sa3id.userActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.dialogs.CustomAlertDialog;
import com.example.sa3id.R;
import com.example.sa3id.adapters.CalendarEventAdapter;
import com.example.sa3id.managers.CalendarEventManager;
import com.example.sa3id.managers.ExamManager;
import com.example.sa3id.models.CalendarEvent;
import com.example.sa3id.models.Exam;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarActivity extends BaseActivity {
    private static final String TAG = "ExamsCalendar";

    // UI components
    private CalendarView calendarView;
    private RecyclerView recyclerViewDateExams;
    private ProgressBar progressBar;
    private LinearLayout adminControlsLayout;
    private EditText editTextEventTitle;
    private Button buttonAddEvent;
    private Button buttonAddHoliday;
    private TextView textViewExamsHeader;

    // Adapters and managers
    private CalendarEventAdapter eventAdapter;
    private ExamManager examManager;
    private CalendarEventManager calendarEventManager;

    // Data
    private String userExtraTimeType = "0";
    private Set<String> userSubjects = new HashSet<>();
    private List<Exam> allUserExams = new ArrayList<>();
    private Map<String, List<Object>> dateEventsMap = new HashMap<>();
    private String selectedDate;
    private boolean isAdmin = false;
    // Flag to track if we're still loading data
    private boolean isLoadingData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();

        // init managers
        examManager = new ExamManager();
        calendarEventManager = new CalendarEventManager();

        // init adapters
        eventAdapter = new CalendarEventAdapter(userExtraTimeType);
        recyclerViewDateExams.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDateExams.setAdapter(eventAdapter);

        checkCurrentUser();
        setupCalendar();
        setupAdminControls();
        showInitialState();

        // Load user settings and exams
        loadUserSettings();
    }

    private void showInitialState() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewDateExams.setVisibility(View.GONE);
        loadUserSettings();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        recyclerViewDateExams = findViewById(R.id.recyclerViewDateExams);
        progressBar = findViewById(R.id.progressBar);
        adminControlsLayout = findViewById(R.id.adminControlsLayout);
        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonAddHoliday = findViewById(R.id.buttonAddHoliday);
        textViewExamsHeader = findViewById(R.id.textViewExamsHeader);
    }

    private void checkCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            CustomAlertDialog dialog = new CustomAlertDialog(this);
            dialog.show("يرجى تسجيل الدخول أولاً", R.drawable.baseline_error_24);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            });
            return;
        }

        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    isAdmin = Boolean.TRUE.equals(documentSnapshot.getBoolean("admin"));
                    adminControlsLayout.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                }
            });
    }

    private void setupCalendar() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(new Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String monthStr = String.format(Locale.getDefault(), "%02d", month + 1);
            String dayStr = String.format(Locale.getDefault(), "%02d", dayOfMonth);
            selectedDate = dayStr + "-" + monthStr + "-" + year;
            showEventsForDate(selectedDate);
        });
    }

    private void setupAdminControls() {
        buttonAddEvent.setOnClickListener(v -> addCalendarItem("event"));
        buttonAddHoliday.setOnClickListener(v -> addCalendarItem("holiday"));
    }

    private void addCalendarItem(String type) {
        String title = editTextEventTitle.getText().toString().trim();
        if (title.isEmpty()) {
            CustomAlertDialog dialog = new CustomAlertDialog(this);
            dialog.show("الرجاء إدخال عنوان المناسبة", R.drawable.baseline_error_24);
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        CalendarEvent event = new CalendarEvent(null, title, selectedDate, type, user.getUid());
        calendarEventManager.addEvent(event, new CalendarEventManager.CalendarEventCallback() {
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {}

            @Override
            public void onEventAdded(boolean success, String eventId) {
                if (success) {
                    CustomAlertDialog dialog = new CustomAlertDialog(CalendarActivity.this);
                    dialog.show("تمت إضافة المناسبة بنجاح", R.drawable.baseline_check_circle_24);
                    editTextEventTitle.setText("");
                    loadAllCalendarEvents();
                } else {
                    CustomAlertDialog dialog = new CustomAlertDialog(CalendarActivity.this);
                    dialog.show("فشل في إضافة المناسبة", R.drawable.baseline_error_24);
                }
            }

            @Override
            public void onEventDeleted(boolean success) {}

            @Override
            public void onError(String error) {
                CustomAlertDialog dialog = new CustomAlertDialog(CalendarActivity.this);
                dialog.show("حدث خطأ: " + error, R.drawable.baseline_error_24);
            }
        });
    }

    private void loadUserSettings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK)
            .getReference()
            .child("users")
            .child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Load extra time setting
                    userExtraTimeType = dataSnapshot.child("extraTimeType").getValue(String.class);
                    if (userExtraTimeType == null) userExtraTimeType = "0";

                    // Load selected subjects
                    DataSnapshot subjectsSnapshot = dataSnapshot.child("selectedSubjects");
                    userSubjects.clear();
                    for (DataSnapshot subjectSnapshot : subjectsSnapshot.getChildren()) {
                        String subject = subjectSnapshot.getValue(String.class);
                        if (subject != null) {
                            userSubjects.add(subject);
                        }
                    }
                }
                eventAdapter.setExtraTimeType(userExtraTimeType);
                loadExams();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                CustomAlertDialog dialog = new CustomAlertDialog(CalendarActivity.this);
                dialog.show("فشل في تحميل إعدادات المستخدم", R.drawable.baseline_error_24);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadExams() {
        if (userSubjects.isEmpty()) {
            loadAllCalendarEvents();
            return;
        }

        DatabaseReference examsRef = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK)
            .getReference()
            .child("exams");
            
        allUserExams.clear();

        for (String subject : userSubjects) {
            examsRef.child(subject).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot subjectSnapshot) {
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

                            Exam exam = new Exam(examId, examName, startHour, endHour, duration,
                                    endHour25, endHour33, endHour50, date, subject);
                            allUserExams.add(exam);
                        }
                    }
                    organizeExamsByDate();
                    loadAllCalendarEvents();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    CustomAlertDialog dialog = new CustomAlertDialog(CalendarActivity.this);
                    dialog.show("فشل في تحميل الامتحانات", R.drawable.baseline_error_24);
                }
            });
        }
    }

    private void organizeExamsByDate() {
        dateEventsMap.clear();
        for (Exam exam : allUserExams) {
            String date = exam.getDate();
            if (!dateEventsMap.containsKey(date)) {
                dateEventsMap.put(date, new ArrayList<>());
            }
            dateEventsMap.get(date).add(exam);
        }
    }

    private void loadAllCalendarEvents() {
        calendarEventManager.getAllEvents(new CalendarEventManager.CalendarEventCallback() {
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {
                for (CalendarEvent event : events) {
                    String date = event.getDate();
                    if (!dateEventsMap.containsKey(date)) {
                        dateEventsMap.put(date, new ArrayList<>());
                    }
                    dateEventsMap.get(date).add(event);
                }
                
                progressBar.setVisibility(View.GONE);
                recyclerViewDateExams.setVisibility(View.VISIBLE);
                
                if (selectedDate != null) {
                    showEventsForDate(selectedDate);
                }
            }

            @Override
            public void onEventAdded(boolean success, String eventId) {}

            @Override
            public void onEventDeleted(boolean success) {}

            @Override
            public void onError(String error) {
                CustomAlertDialog dialog = new CustomAlertDialog(CalendarActivity.this);
                dialog.show("فشل في تحميل المناسبات: " + error, R.drawable.baseline_error_24);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showEventsForDate(String date) {
        List<Object> dateEvents = dateEventsMap.getOrDefault(date, new ArrayList<>());
        eventAdapter.setItems(dateEvents);
        
        if (dateEvents.isEmpty()) {
            textViewExamsHeader.setText("لا توجد مناسبات أو امتحانات في هذا اليوم");
        } else {
            int examsCount = 0;
            int eventsCount = 0;
            for (Object item : dateEvents) {
                if (item instanceof Exam) examsCount++;
                else if (item instanceof CalendarEvent) eventsCount++;
            }
            textViewExamsHeader.setText(String.format("امتحانات: %d | مناسبات: %d", examsCount, eventsCount));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventAdapter != null) {
            eventAdapter.clearItems();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_calendar;
    }
}