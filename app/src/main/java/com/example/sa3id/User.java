package com.example.sa3id;

public class User {
    private String username;
    private String email;
    private final boolean isAdmin;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.isAdmin = false;
    }

    public User() {
        this.isAdmin = false;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    // Getter and Setter methods
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
}

