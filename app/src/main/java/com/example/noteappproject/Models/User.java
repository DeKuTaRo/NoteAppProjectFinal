package com.example.noteappproject.Models;

public class User {
    private String fullName;
    private String email;
    private String avatarPath;

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
        this.avatarPath = "";
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}
