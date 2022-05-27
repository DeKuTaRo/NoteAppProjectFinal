package com.example.noteappproject.Models;

public class User {
    private String fullName;
    private String email;
    private boolean isActivated;
    private String avatarPath;

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
        this.isActivated = false;
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

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }
}
