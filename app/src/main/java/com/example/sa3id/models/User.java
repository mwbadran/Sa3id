package com.example.sa3id.models;

import com.google.firebase.firestore.Exclude;

public class User {
    private String username;
    private String email;
    private boolean isAdmin;
    private String profilePicUrl;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.isAdmin = false;
    }

    public User(String username, String email, String profilePicUrl) {
        this.username = username;
        this.email = email;
        this.isAdmin = false;
        this.profilePicUrl = profilePicUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}

