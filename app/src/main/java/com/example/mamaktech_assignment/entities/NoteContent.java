package com.example.mamaktech_assignment.entities;

import java.io.Serializable;

public class NoteContent implements Serializable{

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_CHECK = 1;
    public static final int TYPE_IMAGE = 2;
    private int type;
    private String text;
    private boolean checkBool;
    private String checkText;
    private String imagePath;
    private String textFormatting;

    public NoteContent() {
    }

    public NoteContent(int type , String text, String textFormatting){
        if(type == TYPE_TEXT){
            this.text = text;
            this.textFormatting = textFormatting;
        } else if(type == TYPE_IMAGE){
            this.imagePath = text;
        }
        this.type = type;
    }

    public NoteContent(boolean checkBool, String checkText){
        this.checkBool = checkBool;
        this.checkText = checkText;
        this.type = TYPE_CHECK;
    }

    public int typeCheck() {return type;}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCheckBool() {
        return checkBool;
    }

    public void setCheckBool(boolean checkBool) {
        this.checkBool = checkBool;
    }

    public String getCheckText() {
        return checkText;
    }

    public void setCheckText(String checkText) {
        this.checkText = checkText;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getTextFormatting() {
        return textFormatting;
    }

    public void setTextFormatting(String textFormatting) {
        this.textFormatting = textFormatting;
    }
}
