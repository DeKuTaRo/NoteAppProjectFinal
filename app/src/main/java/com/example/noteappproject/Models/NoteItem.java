package com.example.noteappproject.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes")
public class NoteItem implements Serializable {

    @PrimaryKey(autoGenerate = true)
    int ID = 0;

    @ColumnInfo(name = "label")
    String label = "";

    @ColumnInfo(name = "subtitle")
    String subtitle = "";

    @ColumnInfo(name = "text_content")
    String text_content = "";

    @ColumnInfo(name = "date")
    String date = "";

    @ColumnInfo(name = "color")
    String color;

    @ColumnInfo(name = "imagePath")
    String imagePath;

    @ColumnInfo(name = "webLink")
    String webLink;

    @ColumnInfo(name = "pinned")
    boolean pinned = false;

    @ColumnInfo(name = "passwordNote")
    String passwordNote = "";

    public NoteItem() {

    }

    public NoteItem(int ID, String label, String subtitle, String text_content, String date, String color, String imagePath, String webLink) {
        this.ID = ID;
        this.label = label;
        this.subtitle = subtitle;
        this.text_content = text_content;
        this.date = date;
        this.color = color;
        this.imagePath = imagePath;
        this.webLink = webLink;
    }


    public NoteItem(String label, String subtitle, String text_content, String date, String color, String imagePath, String webLink, String passwordNote) {
        this.label = label;
        this.subtitle = subtitle;
        this.text_content = text_content;
        this.date = date;
        this.color = color;
        this.imagePath = imagePath;
        this.webLink = webLink;
        this.passwordNote = passwordNote;
    }

    public NoteItem(int ID, String passwordNote) {
        this.ID = ID;
        this.passwordNote = passwordNote;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getText_content() {
        return text_content;
    }

    public void setText_content(String text_content) {
        this.text_content = text_content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public String getPasswordNote() {
        return passwordNote;
    }

    public void setPasswordNote(String passwordNote) {
        this.passwordNote = passwordNote;
    }
}
