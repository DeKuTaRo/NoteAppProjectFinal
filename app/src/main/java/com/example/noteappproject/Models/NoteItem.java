package com.example.noteappproject.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
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

    @ColumnInfo(name = "videoPath")
    String videoPath;

    @ColumnInfo(name = "webLink")
    String webLink;

    @ColumnInfo(name = "pinned")
    boolean pinned = false;

    @ColumnInfo(name = "passwordNote")
    String passwordNote = "";

    @ColumnInfo(name = "isDeleted")
    boolean isDeleted = false;

    @ColumnInfo(name = "created_at")
    long created_at;

    @Ignore
    String deleted_at;

    @Ignore
    long pin_at;

    public NoteItem() {

    }

    public NoteItem(String label, String subtitle, String text_content, String date, String color, String imagePath, String videoPath, String webLink) {
        this.label = label;
        this.subtitle = subtitle;
        this.text_content = text_content;
        this.date = date;
        this.color = color;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
        this.webLink = webLink;
    }


    public NoteItem(String label, String subtitle, String text_content, String date, String color, String imagePath, String videoPath, String webLink, String passwordNote) {
        this.label = label;
        this.subtitle = subtitle;
        this.text_content = text_content;
        this.date = date;
        this.color = color;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
        this.webLink = webLink;
        this.passwordNote = passwordNote;
    }

    public NoteItem(String label, String subtitle, String text_content, String date, String color, String imagePath, String videoPath, String webLink, String passwordNote, long created_at) {
        this.label = label;
        this.subtitle = subtitle;
        this.text_content = text_content;
        this.date = date;
        this.color = color;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
        this.webLink = webLink;
        this.pinned = pinned;
        this.passwordNote = passwordNote;
        this.created_at = created_at;
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

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }

    public long getPin_at() {
        return pin_at;
    }

    public void setPin_at(long pin_at) {
        this.pin_at = pin_at;
    }

    @Override
    public String toString() {
        return "NoteItem{" +
                "label='" + label + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", text_content='" + text_content + '\'' +
                ", date='" + date + '\'' +
                ", color='" + color + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", videoPath='" + videoPath + '\'' +
                ", webLink='" + webLink + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}
