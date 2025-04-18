package com.example.mamaktech_assignment;

import android.net.Uri;

import java.util.UUID;

public class Note {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_CHECKLIST = 2;

    private String id;
    private int type;
    private String text;
    private Uri imageUri;
    private String checkList;
    private boolean isChecked;

    public Note(String text) {
        this.type = TYPE_TEXT;
        this.text = text;
        this.id = UUID.randomUUID().toString();
    }

    public Note(Uri imageUri) {
        this.type = TYPE_IMAGE;
        this.imageUri = imageUri;
        this.id = UUID.randomUUID().toString();
    }

    public Note(String checkList, boolean isChecked) {
        this.type = TYPE_CHECKLIST;
        this.checkList = checkList;
        this.isChecked = isChecked;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
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
