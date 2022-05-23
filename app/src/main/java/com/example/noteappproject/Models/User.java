package com.example.noteappproject.Models;

public class User {

    private String fullName, email, password, avatarPath;

    public User() {

    }

    public User(String fullName, String email, String password, String avatarPath) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.avatarPath = avatarPath;
    }

    public User(String fullName, String email, String avatarPath) {
        this.fullName = fullName;
        this.email = email;
        this.avatarPath = avatarPath;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}
