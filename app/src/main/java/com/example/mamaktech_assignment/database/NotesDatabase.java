package com.example.mamaktech_assignment.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.mamaktech_assignment.dao.NoteDao;
import com.example.mamaktech_assignment.entities.Note;
import com.example.mamaktech_assignment.utils.JsonConverter;

@Database(entities = Note.class, version = 2, exportSchema = false)
@TypeConverters({JsonConverter.class})
public abstract class NotesDatabase extends RoomDatabase {

    private static NotesDatabase notesDatabase;

    public static synchronized NotesDatabase getDatabase(Context context) {
        if(notesDatabase == null) {
            notesDatabase = Room.databaseBuilder(
                    context,
                    NotesDatabase.class,
                    "notes_db"
            ).fallbackToDestructiveMigration()
                    .build();
        }
        return notesDatabase;
    }

    public abstract NoteDao noteDao();
}
