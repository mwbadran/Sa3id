package com.example.sa3id.userActivities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ExamsCalendar extends BaseActivity {
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

        // Initialize UI components
        initViews();

        // Initialize managers
        examManager = new ExamManager();
        calendarEventManager = new CalendarEventManager();
        
        // Initialize adapter
        eventAdapter = new CalendarEventAdapter(userExtraTimeType);
        recyclerViewDateExams.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDateExams.setAdapter(eventAdapter);

        // Get current user and check if admin
        checkCurrentUser();

        // Setup calendar
        setupCalendar();
        
        // Setup admin controls
        setupAdminControls();
        
        // Show initial empty state
        showInitialState();
        
        // Load user settings and exams
        loadUserSettings();
    }
    
    private void showInitialState() {
        // Show default text when no data is loaded yet
        textViewExamsHeader.setText("جاري تحميل بيانات الامتحانات...");
        
        // Create an initial empty list
        eventAdapter.setItems(new ArrayList<>());
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "يرجى تسجيل الدخول لعرض جدول الامتحانات", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Check if user is admin
        FirebaseDatabase.getInstance().getReference()
            .child("admins")
            .child(currentUser.getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    isAdmin = dataSnapshot.exists();
                    adminControlsLayout.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    
                    // Add admin calendar button if user is admin
                    if (isAdmin) {
                        Button adminCalendarButton = new Button(ExamsCalendar.this);
                        adminCalendarButton.setText("إدارة التقويم المتقدمة");
                        adminCalendarButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black, null)));
                        adminCalendarButton.setTextColor(Color.WHITE);
                        adminCalendarButton.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        adminCalendarButton.setOnClickListener(v -> {
                            Intent intent = new Intent(ExamsCalendar.this, AdminCalendarActivity.class);
                            startActivity(intent);
                        });
                        
                        // Add to admin controls layout
                        adminControlsLayout.addView(adminCalendarButton, 0);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error checking admin status: " + databaseError.getMessage());
                }
            });
    }

    @SuppressLint("SimpleDateFormat")
    private void setupCalendar() {
        // Get today's date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());
        
        // Listen for date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDate = dateFormat.format(calendar.getTime());
            
            // Update header
            textViewExamsHeader.setText(String.format("الامتحانات والمناسبات ليوم %s", selectedDate));
            
            // Show events for selected date
            showEventsForDate(selectedDate);
        });
        
        // Initial header text
        textViewExamsHeader.setText(String.format("الامتحانات والمناسبات ليوم %s", selectedDate));
    }

    private void setupAdminControls() {
        buttonAddEvent.setOnClickListener(v -> addCalendarItem("event"));
        buttonAddHoliday.setOnClickListener(v -> addCalendarItem("holiday"));
    }

    private void addCalendarItem(String type) {
        String title = editTextEventTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "يرجى إدخال عنوان المناسبة", Toast.LENGTH_SHORT).show();
            return;
        }
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDate(selectedDate);
        event.setType(type);
        event.setAddedBy(currentUser.getUid());
        
        progressBar.setVisibility(View.VISIBLE);
        calendarEventManager.addEvent(event, new CalendarEventManager.CalendarEventCallback() {
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {
                // Not used in this context
            }

            @Override
            public void onEventAdded(boolean success, String eventId) {
                progressBar.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(ExamsCalendar.this, "تمت إضافة المناسبة بنجاح", Toast.LENGTH_SHORT).show();
                    editTextEventTitle.setText("");
                    
                    // Refresh events for selected date
                    showEventsForDate(selectedDate);
                    
                    // Refresh all events
                    loadAllCalendarEvents();
                } else {
                    Toast.makeText(ExamsCalendar.this, "فشلت إضافة المناسبة", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEventDeleted(boolean success) {
                // Not used in this context
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ExamsCalendar.this, "خطأ: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserSettings() {
        progressBar.setVisibility(View.VISIBLE);
        isLoadingData = true;
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            isLoadingData = false;
            return;
        }
        
        FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(currentUser.getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Load extra time setting
                        if (dataSnapshot.hasChild("extraTimeType")) {
                            userExtraTimeType = dataSnapshot.child("extraTimeType").getValue(String.class);
                            eventAdapter.setExtraTimeType(userExtraTimeType);
                        }

                        // Load selected subjects
                        if (dataSnapshot.hasChild("selectedSubjects")) {
                            for (DataSnapshot subjectSnapshot : dataSnapshot.child("selectedSubjects").getChildren()) {
                                String subject = subjectSnapshot.getValue(String.class);
                                if (subject != null) {
                                    userSubjects.add(subject);
                                }
                            }
                        }

                        // Load exams for user's subjects
                        loadExams();
                    } else {
                        isLoadingData = false;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ExamsCalendar.this, "لم يتم العثور على إعدادات المستخدم", Toast.LENGTH_SHORT).show();
                        // Load calendar events even if user settings not found
                        loadAllCalendarEvents();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    isLoadingData = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ExamsCalendar.this, 
                        "خطأ في تحميل إعدادات المستخدم: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    // Load calendar events even if user settings fail
                    loadAllCalendarEvents();
                }
            });
    }

    private void loadExams() {
        if (userSubjects.isEmpty()) {
            isLoadingData = false;
            progressBar.setVisibility(View.GONE);
            loadAllCalendarEvents(); // Still load events even if no exams
            Toast.makeText(this, "يرجى اختيار المواضيع الخاصة بك أولاً", Toast.LENGTH_LONG).show();
            return;
        }

        examManager.getExamsForUserSubjects(userSubjects, new ExamManager.ExamCallback() {
            @Override
            public void onExamsLoaded(List<Exam> exams) {
                allUserExams.clear();
                allUserExams.addAll(exams);
                
                // Organize exams by date
                organizeExamsByDate();
                
                // Load all calendar events
                loadAllCalendarEvents();
                
                // Show events for the selected date
                showEventsForDate(selectedDate);
                
                // We'll hide the progress bar after loading calendar events
            }

            @Override
            public void onError(String error) {
                isLoadingData = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ExamsCalendar.this, 
                    "خطأ في تحميل الامتحانات: " + error, 
                    Toast.LENGTH_SHORT).show();
                
                // Still try to load calendar events
                loadAllCalendarEvents();
            }
        });
    }

    private void loadAllCalendarEvents() {
        calendarEventManager.getAllEvents(new CalendarEventManager.CalendarEventCallback() {
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {
                isLoadingData = false;
                progressBar.setVisibility(View.GONE);
                
                // Add events to date map
                for (CalendarEvent event : events) {
                    String date = event.getDate();
                    if (!dateEventsMap.containsKey(date)) {
                        dateEventsMap.put(date, new ArrayList<>());
                    }
                    dateEventsMap.get(date).add(event);
                }
                
                // Refresh view for the selected date
                showEventsForDate(selectedDate);
            }

            @Override
            public void onEventAdded(boolean success, String eventId) {
                // Not used in this context
            }

            @Override
            public void onEventDeleted(boolean success) {
                // Not used in this context
            }

            @Override
            public void onError(String error) {
                isLoadingData = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ExamsCalendar.this, 
                    "خطأ في تحميل المناسبات: " + error, 
                    Toast.LENGTH_SHORT).show();
                
                // Still show any loaded exams
                showEventsForDate(selectedDate);
            }
        });
    }

    private void organizeExamsByDate() {
        dateEventsMap.clear();
        
        for (Exam exam : allUserExams) {
            String examDate = exam.getDate();
            if (!dateEventsMap.containsKey(examDate)) {
                dateEventsMap.put(examDate, new ArrayList<>());
            }
            dateEventsMap.get(examDate).add(exam);
        }
    }

    private void showEventsForDate(String date) {
        List<Object> items = new ArrayList<>();
        
        // Add exams and events for the selected date
        if (dateEventsMap.containsKey(date)) {
            items.addAll(dateEventsMap.get(date));
        }
        
        eventAdapter.setItems(items);
        
        if (isLoadingData) {
            textViewExamsHeader.setText("جاري تحميل بيانات الامتحانات...");
        } else if (items.isEmpty()) {
            textViewExamsHeader.setText(String.format("لا توجد امتحانات أو مناسبات ليوم %s", date));
        } else {
            textViewExamsHeader.setText(String.format("الامتحانات والمناسبات ليوم %s (%d)", date, items.size()));
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure we clear the loading flag when the activity is destroyed
        isLoadingData = false;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_exams_calendar;
    }
}