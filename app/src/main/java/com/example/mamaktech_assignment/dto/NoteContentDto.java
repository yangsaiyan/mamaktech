package com.example.mamaktech_assignment.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NoteContentDto implements Serializable {
    @SerializedName("type")
    private int type;

    @SerializedName("content")
    private String content;

    @SerializedName("formatting")
    private String formatting;

    @SerializedName("checked")
    private boolean checked;

}
