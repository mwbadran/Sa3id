package com.example.sa3id.models;

public class Exam {
    private String examId;
    private String examName;
    private String startHour;
    private String endHour;
    private String duration;
    private String endHour25;
    private String endHour33;
    private String endHour50;
    private String date;
    private String subject;

    public Exam() {}

    public Exam(String examId, String examName, String startHour, String endHour, 
                String duration, String endHour25, String endHour33, String endHour50, 
                String date, String subject) {
        this.examId = examId;
        this.examName = examName;
        this.startHour = startHour;
        this.endHour = endHour;
        this.duration = duration;
        this.endHour25 = endHour25;
        this.endHour33 = endHour33;
        this.endHour50 = endHour50;
        this.date = date;
        this.subject = subject;
    }

    // Getters and setters
    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getStartHour() { return startHour; }
    public void setStartHour(String startHour) { this.startHour = startHour; }

    public String getEndHour() { return endHour; }
    public void setEndHour(String endHour) { this.endHour = endHour; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getEndHour25() { return endHour25; }
    public void setEndHour25(String endHour25) { this.endHour25 = endHour25; }

    public String getEndHour33() { return endHour33; }
    public void setEndHour33(String endHour33) { this.endHour33 = endHour33; }

    public String getEndHour50() { return endHour50; }
    public void setEndHour50(String endHour50) { this.endHour50 = endHour50; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getEndTimeByExtraTime(String extraTimeType) {
        switch (extraTimeType) {
            case "25":
                return endHour25;
            case "33":
                return endHour33;
            case "50":
                return endHour50;
            default:
                return endHour;
        }
    }
} 