package com.example.mamaktech_assignment.utils;

import android.net.Uri;

import androidx.room.TypeConverter;

import com.example.mamaktech_assignment.entities.NoteContent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
public class JsonConverter {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriTypeAdapter())
            .create();

    @TypeConverter
    public String fromNoteContentList(List<NoteContent> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public List<NoteContent> toNoteContentList(String data) {
        if (data == null) return null;
        Gson gson = new Gson();
        Type listType = new TypeToken<List<NoteContent>>() {}.getType();
        return gson.fromJson(data, listType);
    }

}
