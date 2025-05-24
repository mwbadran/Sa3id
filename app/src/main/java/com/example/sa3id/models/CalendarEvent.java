package com.example.sa3id.models;

public class CalendarEvent {
    private String eventId;
    private String title;
    private String date;
    private String type; // "event" or "holiday"
    private String addedBy; // User ID who added the event

    public CalendarEvent() {
    }

    public CalendarEvent(String eventId, String title, String date, String type, String addedBy) {
        this.eventId = eventId;
        this.title = title;
        this.date = date;
        this.type = type;
        this.addedBy = addedBy;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
} 