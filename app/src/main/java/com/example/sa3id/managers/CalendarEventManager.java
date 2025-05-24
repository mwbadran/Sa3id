package com.example.sa3id.managers;

import androidx.annotation.NonNull;
import android.util.Log;
import com.example.sa3id.models.CalendarEvent;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarEventManager {
    private static final String TAG = "CalendarEventManager";
    private static final String EVENTS_REF = "calendarEvents";
    private final DatabaseReference databaseReference;

    public interface CalendarEventCallback {
        void onEventsLoaded(List<CalendarEvent> events);
        void onEventAdded(boolean success, String eventId);
        void onEventDeleted(boolean success);
        void onError(String error);
    }

    public CalendarEventManager() {
        this.databaseReference = FirebaseDatabase.getInstance("https://sa3idsite-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        Log.d(TAG, "Initialized with database reference: " + databaseReference.toString());
    }

    public void getAllEvents(final CalendarEventCallback callback) {
        DatabaseReference eventsRef = databaseReference.child(EVENTS_REF);
        Log.d(TAG, "Querying all calendar events at path: " + eventsRef.toString());
        
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CalendarEvent> events = new ArrayList<>();
                
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String eventId = eventSnapshot.getKey();
                    CalendarEvent event = eventSnapshot.getValue(CalendarEvent.class);
                    if (event != null) {
                        event.setEventId(eventId);
                        events.add(event);
                        Log.d(TAG, "Added event: " + event.getTitle() + " (ID: " + eventId + ")");
                    } else {
                        Log.w(TAG, "Failed to parse event with ID: " + eventId);
                    }
                }
                
                Log.d(TAG, "Loaded " + events.size() + " calendar events");
                callback.onEventsLoaded(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading calendar events: " + databaseError.getMessage());
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void getEventsForDate(String date, final CalendarEventCallback callback) {
        DatabaseReference eventsRef = databaseReference.child(EVENTS_REF);
        Log.d(TAG, "Querying calendar events for date: " + date);
        
        eventsRef.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CalendarEvent> events = new ArrayList<>();
                
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String eventId = eventSnapshot.getKey();
                    CalendarEvent event = eventSnapshot.getValue(CalendarEvent.class);
                    if (event != null) {
                        event.setEventId(eventId);
                        events.add(event);
                        Log.d(TAG, "Added event for date: " + event.getTitle() + " (ID: " + eventId + ")");
                    } else {
                        Log.w(TAG, "Failed to parse event with ID: " + eventId);
                    }
                }
                
                Log.d(TAG, "Loaded " + events.size() + " calendar events for date: " + date);
                callback.onEventsLoaded(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading calendar events for date: " + date + ", " + databaseError.getMessage());
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void addEvent(CalendarEvent event, final CalendarEventCallback callback) {
        DatabaseReference eventsRef = databaseReference.child(EVENTS_REF);
        String eventId = eventsRef.push().getKey();
        
        if (eventId != null) {
            event.setEventId(eventId);
            
            eventsRef.child(eventId).setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event added successfully: " + event.getTitle() + " (ID: " + eventId + ")");
                    callback.onEventAdded(true, eventId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding event: " + e.getMessage());
                    callback.onError(e.getMessage());
                    callback.onEventAdded(false, null);
                });
        } else {
            Log.e(TAG, "Failed to generate event ID");
            callback.onError("Failed to generate event ID");
            callback.onEventAdded(false, null);
        }
    }

    public void deleteEvent(String eventId, final CalendarEventCallback callback) {
        DatabaseReference eventRef = databaseReference.child(EVENTS_REF).child(eventId);
        
        eventRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Event deleted successfully: " + eventId);
                callback.onEventDeleted(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting event: " + e.getMessage());
                callback.onError(e.getMessage());
                callback.onEventDeleted(false);
            });
    }
} 