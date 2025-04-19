package com.example.mamaktech_assignment.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "notes")
public class Note implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "note_content_list")
    private List<NoteContent> noteContentList = new ArrayList<>();

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date_time")
    private String dateTime;

    @ColumnInfo(name = "subtitle")
    private String subtitle;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void insertTextOrCheck(int type, String text) {
        noteContentList.add(new NoteContent(type, text));
    }

    public void insertCheck(boolean bool, String text) {
        noteContentList.add(new NoteContent(bool, text));
    }

    public List<NoteContent> getNoteContentList() {
        return noteContentList;
    }

    public void setNoteContentList(List<NoteContent> noteContentList) {
        this.noteContentList = noteContentList;
    }


    public NoteContent getNoteContent(NoteContent nc) {

        return nc;
    }

    public void setNoteContent(NoteContent nc) {

    }

    @NonNull
    @Override
    public String toString() {
        return title + " : " + dateTime;
    }
}
