package com.example.sa3id.models;

public class FeedbackMsg {
    private String id;
    private String userId;
    private String name;
    private String email;
    private String subject;
    private String message;
    private String timestamp;
    private String status;
    private String response;
    private String respondedBy;
    private String responseTimestamp;

    public FeedbackMsg() {
    }

    public FeedbackMsg(String id, String userId, String name, String email, String subject, String message, String timestamp, String status, String response, String respondedBy, String responseTimestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.response = response;
        this.respondedBy = respondedBy;
        this.responseTimestamp = responseTimestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRespondedBy() {
        return respondedBy;
    }

    public void setRespondedBy(String respondedBy) {
        this.respondedBy = respondedBy;
    }

    public String getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(String responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }
}