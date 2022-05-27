package com.example.noteappproject.Models;

public class NoteLabel {
    private String labelName;
    private boolean isCheck;

    public NoteLabel(String labelName) {
        this.labelName = labelName;
        this.isCheck = false;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
