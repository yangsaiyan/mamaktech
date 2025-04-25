package com.example.mamaktech_assignment.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CreateNoteDto implements Serializable {
    @SerializedName("title")
    private String title;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("date_time")
    private String dateTime;

    @SerializedName("is_pinned")
    private boolean isPinned;

    @SerializedName("note_content_list")
    private String noteContentList;

    @SerializedName("userId")
    private int userId;

}