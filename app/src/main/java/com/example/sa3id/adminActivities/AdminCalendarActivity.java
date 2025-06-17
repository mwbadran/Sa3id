package com.example.sa3id.adminActivities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.example.sa3id.adapters.AdminCalendarEventAdapter;
import com.example.sa3id.managers.CalendarEventManager;
import com.example.sa3id.models.CalendarEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminCalendarActivity extends BaseActivity implements AdminCalendarEventAdapter.EventActionListener {
    private static final String TAG = "AdminCalendarActivity";
    
    // UI Components
    private EditText editTextEventTitle;
    private EditText editTextEventDescription;
    private Button buttonSelectDate;
    private TextView textViewSelectedDate;
    private RadioButton radioButtonEvent;
    private RadioButton radioButtonHoliday;
    private Button buttonAddEvent;
    private Button buttonFilterStartDate;
    private Button buttonFilterEndDate;
    private CheckBox checkBoxEvents;
    private CheckBox checkBoxHolidays;
    private Button buttonApplyFilter;
    private RecyclerView recyclerViewEvents;
    private ProgressBar progressBar;
    private TextView textViewEventsHeader;
    
    // Data
    private CalendarEventManager eventManager;
    private AdminCalendarEventAdapter eventAdapter;
    private String selectedDate;
    private String filterStartDate;
    private String filterEndDate;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    
    // Current event being edited (null for new events)
    private CalendarEvent currentEditEvent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize date format
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());
        
        // Initialize UI components
        initViews();
        
        // Initialize event manager and adapter
        eventManager = new CalendarEventManager();
        eventAdapter = new AdminCalendarEventAdapter(this);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventAdapter);
        
        // Set up button click listeners
        setupListeners();
        
        // Load events
        loadEvents();
    }
    
    private void initViews() {
        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        radioButtonEvent = findViewById(R.id.radioButtonEvent);
        radioButtonHoliday = findViewById(R.id.radioButtonHoliday);
        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonFilterStartDate = findViewById(R.id.buttonFilterStartDate);
        buttonFilterEndDate = findViewById(R.id.buttonFilterEndDate);
        checkBoxEvents = findViewById(R.id.checkBoxEvents);
        checkBoxHolidays = findViewById(R.id.checkBoxHolidays);
        buttonApplyFilter = findViewById(R.id.buttonApplyFilter);
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        progressBar = findViewById(R.id.progressBar);
        textViewEventsHeader = findViewById(R.id.textViewEventsHeader);
        
        // Set initial date
        textViewSelectedDate.setText("التاريخ: " + selectedDate);
    }
    
    private void setupListeners() {
        // Date selection button
        buttonSelectDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AdminCalendarActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedDate = dateFormat.format(calendar.getTime());
                        textViewSelectedDate.setText("التاريخ: " + selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        // Add/edit event button
        buttonAddEvent.setOnClickListener(v -> {
            String title = editTextEventTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال عنوان المناسبة", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String type = radioButtonEvent.isChecked() ? "event" : "holiday";
            
            if (currentEditEvent == null) {
                // Add new event
                addEvent(title, type);
            } else {
                // Update existing event
                updateEvent(currentEditEvent, title, type);
            }
        });
        
        // Filter date buttons
        buttonFilterStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AdminCalendarActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, dayOfMonth);
                        filterStartDate = dateFormat.format(cal.getTime());
                        buttonFilterStartDate.setText("من: " + filterStartDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        buttonFilterEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AdminCalendarActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, dayOfMonth);
                        filterEndDate = dateFormat.format(cal.getTime());
                        buttonFilterEndDate.setText("إلى: " + filterEndDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        // Apply filter button
        buttonApplyFilter.setOnClickListener(v -> applyFilters());
    }
    
    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        textViewEventsHeader.setText("جاري تحميل المناسبات...");
        
        eventManager.getAllEvents(new CalendarEventManager.CalendarEventCallback() {
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {
                progressBar.setVisibility(View.GONE);
                eventAdapter.setEvents(events);
                textViewEventsHeader.setText(String.format("المناسبات (%d)", events.size()));
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
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminCalendarActivity.this, 
                        "خطأ في تحميل المناسبات: " + error, Toast.LENGTH_SHORT).show();
                textViewEventsHeader.setText("المناسبات (0)");
            }
        });
    }
    
    private void addEvent(String title, String type) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDate(selectedDate);
        event.setType(type);
        event.setAddedBy(currentUser.getUid());
        
        progressBar.setVisibility(View.VISIBLE);
        eventManager.addEvent(event, new CalendarEventManager.CalendarEventCallback() {
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {
                // Not used in this context
            }

            @Override
            public void onEventAdded(boolean success, String eventId) {
                progressBar.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(AdminCalendarActivity.this, 
                            "تمت إضافة المناسبة بنجاح", Toast.LENGTH_SHORT).show();
                    clearEventForm();
                    loadEvents(); // Refresh events list
                } else {
                    Toast.makeText(AdminCalendarActivity.this, 
                            "فشلت إضافة المناسبة", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEventDeleted(boolean success) {
                // Not used in this context
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminCalendarActivity.this, 
                        "خطأ: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateEvent(CalendarEvent event, String title, String type) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        event.setTitle(title);
        event.setDate(selectedDate);
        event.setType(type);
        
        // First delete the old event
        progressBar.setVisibility(View.VISIBLE);
        eventManager.deleteEvent(event.getEventId(), new CalendarEventManager.CalendarEventCallback() {
            @Override
            public void onEventsLoaded(List<CalendarEvent> events) {
                // Not used in this context
            }

            @Override
            public void onEventAdded(boolean success, String eventId) {
                // Not used in this context
            }

            @Override
            public void onEventDeleted(boolean success) {
                if (success) {
                    // Now add the updated event as a new event
                    eventManager.addEvent(event, new CalendarEventManager.CalendarEventCallback() {
                        @Override
                        public void onEventsLoaded(List<CalendarEvent> events) {
                            // Not used in this context
                        }

                        @Override
                        public void onEventAdded(boolean success, String eventId) {
                            progressBar.setVisibility(View.GONE);
                            if (success) {
                                Toast.makeText(AdminCalendarActivity.this, 
                                        "تم تحديث المناسبة بنجاح", Toast.LENGTH_SHORT).show();
                                clearEventForm();
                                loadEvents(); // Refresh events list
                            } else {
                                Toast.makeText(AdminCalendarActivity.this, 
                                        "فشل تحديث المناسبة", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onEventDeleted(boolean success) {
                            // Not used in this context
                        }

                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminCalendarActivity.this, 
                                    "خطأ: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminCalendarActivity.this, 
                            "فشل حذف المناسبة القديمة", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminCalendarActivity.this, 
                        "خطأ: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void clearEventForm() {
        editTextEventTitle.setText("");
        editTextEventDescription.setText("");
        radioButtonEvent.setChecked(true);
        currentEditEvent = null;
        buttonAddEvent.setText("إضافة المناسبة");
        
        // Reset date to today
        calendar = Calendar.getInstance();
        selectedDate = dateFormat.format(calendar.getTime());
        textViewSelectedDate.setText("التاريخ: " + selectedDate);
    }
    
    private void applyFilters() {
        String eventType = null;
        
        // Check which types are selected
        if (checkBoxEvents.isChecked() && !checkBoxHolidays.isChecked()) {
            eventType = "event";
        } else if (!checkBoxEvents.isChecked() && checkBoxHolidays.isChecked()) {
            eventType = "holiday";
        }
        
        // Apply filters to adapter
        eventAdapter.filterEvents(eventType, filterStartDate, filterEndDate);
        
        // Update header
        textViewEventsHeader.setText(String.format("المناسبات (%d)", eventAdapter.getItemCount()));
    }
    
    @Override
    public void onEditEvent(CalendarEvent event) {
        // Set form fields for editing
        currentEditEvent = event;
        editTextEventTitle.setText(event.getTitle());
        selectedDate = event.getDate();
        textViewSelectedDate.setText("التاريخ: " + selectedDate);
        
        if (event.getType().equals("holiday")) {
            radioButtonHoliday.setChecked(true);
        } else {
            radioButtonEvent.setChecked(true);
        }
        
        buttonAddEvent.setText("تحديث المناسبة");
    }
    
    @Override
    public void onDeleteEvent(CalendarEvent event) {
        new AlertDialog.Builder(this)
                .setTitle("حذف المناسبة")
                .setMessage("هل أنت متأكد من أنك تريد حذف هذه المناسبة؟")
                .setPositiveButton("نعم", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    eventManager.deleteEvent(event.getEventId(), new CalendarEventManager.CalendarEventCallback() {
                        @Override
                        public void onEventsLoaded(List<CalendarEvent> events) {
                            // Not used in this context
                        }

                        @Override
                        public void onEventAdded(boolean success, String eventId) {
                            // Not used in this context
                        }

                        @Override
                        public void onEventDeleted(boolean success) {
                            progressBar.setVisibility(View.GONE);
                            if (success) {
                                Toast.makeText(AdminCalendarActivity.this, 
                                        "تم حذف المناسبة بنجاح", Toast.LENGTH_SHORT).show();
                                loadEvents(); // Refresh events list
                            } else {
                                Toast.makeText(AdminCalendarActivity.this, 
                                        "فشل حذف المناسبة", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminCalendarActivity.this, 
                                    "خطأ: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("لا", null)
                .show();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_admin_calendar;
    }
} 