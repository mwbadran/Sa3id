package com.example.sa3id.models;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UploadRequest {
    private String description;
    private String subject;
    private ArrayList<String> materialsList;
    private String senderEmail;
    private String timestamp;

    public UploadRequest(String description, ArrayList<String> materialsList, String senderEmail, String timestamp) {
        this.description = description;
        this.materialsList = materialsList;
        this.senderEmail = senderEmail;
        this.timestamp = (timestamp != null) ? timestamp : getCurrentTimestamp();

    }

    public UploadRequest() {
        this.timestamp = getCurrentTimestamp();
    }

    public static String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getMaterialsList() {
        return materialsList;
    }

    public void setMaterialsList(ArrayList<String> materialsList) {
        this.materialsList = materialsList;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
