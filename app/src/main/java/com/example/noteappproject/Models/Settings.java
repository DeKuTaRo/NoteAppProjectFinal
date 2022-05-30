package com.example.noteappproject.Models;

public class Settings {
    private String fontSize, fontStyle, timeDelete;

    public Settings() {

    }

    public Settings(String fontSize) {
        this.fontSize = fontSize;
    }


    public Settings(String fontSize, String fontStyle, String timeDelete) {
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        this.timeDelete = timeDelete;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getTimeDelete() {
        return timeDelete;
    }

    public void setTimeDelete(String timeDelete) {
        this.timeDelete = timeDelete;
    }
}
