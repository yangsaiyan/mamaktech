package com.example.mamaktech_assignment.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.mamaktech_assignment.R;
import com.example.mamaktech_assignment.adapters.NotesAdapter;
import com.example.mamaktech_assignment.dao.NoteDao;
import com.example.mamaktech_assignment.database.NotesDatabase;
import com.example.mamaktech_assignment.entities.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_VIEW_NOTE = 3;

    private RecyclerView notesRecyclerView;
    private EditText Searchbar;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        Searchbar = findViewById(R.id.inputSearch);

        Searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                Log.d("AFTER_TEXT_CHANGED", "query: " + query);

                if (query.isEmpty()) {
                    getNotes();
                } else {
                    noteList.clear();
                    onSearchChange(query);
                }

                Log.d("Query Changed", String.valueOf(noteList.size()));
            }
        });
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), EditActivity.class),
                        REQUEST_CODE_ADD_NOTE);
            }
        });

        notesRecyclerView = findViewById(R.id.notesCyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes();
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void onSearchChange(String query) {
        class QueryNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext())
                        .noteDao().getQueriedNotes(query);
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                Log.d("SEARCH_RESULTS", notes.toString());
                noteList.clear();
                noteList.addAll(notes);
                notesAdapter.setNotes(noteList);
            }
        }

        new QueryNotesTask().execute();
    }

    private void getNotes() {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                try {
                    Log.d("DATABASE_DEBUG", "Getting database instance");
                    NotesDatabase db = NotesDatabase.getDatabase(getApplicationContext());
                    Log.d("DATABASE_DEBUG", "Database instance obtained");
                    Log.d("DATABASE_DEBUG", "Getting DAO");
                    NoteDao dao = db.noteDao();
                    Log.d("DATABASE_DEBUG", "DAO obtained");
                    Log.d("DATABASE_DEBUG", "Executing getAllNotes query");
                    List<Note> notes = dao.getAllNotes();
                    Log.d("DATABASE_DEBUG", "Query executed, result size: " + (notes != null ? notes.size() : "null"));
                    return notes;
                } catch (Exception e) {
                    Log.e("DATABASE_ERROR", "Error fetching notes", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (notes != null) {
                    Log.d("MY_NOTES", "Notes found: " + notes.size());
                    noteList.clear();
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else {
                    Log.d("MY_NOTES", "No notes found or error occurred");
                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            Log.d("ACTIVITY_RESULT", "Note added successfully, refreshing notes");
            getNotes();
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes();
            }
        }
    }
}