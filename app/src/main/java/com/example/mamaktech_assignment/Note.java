package com.example.mamaktech_assignment;

import android.net.Uri;

public class Note {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_CHECKLIST = 2;

    private int type;
    private String text;
    private Uri imageUri;
    private String checkList;
    private boolean isChecked;

    public Note(String text) {
        this.type = TYPE_TEXT;
        this.text = text;
    }

    public Note(Uri imageUri) {
        this.type = TYPE_IMAGE;
        this.imageUri = imageUri;
    }

    public Note(String checkList, boolean isChecked) {
        this.type = TYPE_CHECKLIST;
        this.checkList = checkList;
        this.isChecked = isChecked;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getCheckList() {
        return checkList;
    }

    public void setCheckList(String checkList) {
        this.checkList = checkList;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
