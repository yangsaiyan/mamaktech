package com.example.mamaktech_assignment.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mamaktech_assignment.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, id DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :title || '%' ORDER BY is_pinned DESC, id DESC")
    List<Note> getQueriedNotes(String title);

    @Query("UPDATE notes SET is_pinned = 1 WHERE id = :noteId")
    void pinNote(int noteId);

    @Query("UPDATE notes SET is_pinned = 0 WHERE id = :noteId")
    void unpinNote(int noteId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
