package com.example.mamaktech_assignment.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.example.mamaktech_assignment.R;
import com.example.mamaktech_assignment.adapters.NotesAdapter;
import com.example.mamaktech_assignment.dao.NoteDao;
import com.example.mamaktech_assignment.database.NotesDatabase;
import com.example.mamaktech_assignment.entities.Note;
import com.example.mamaktech_assignment.utils.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_VIEW_NOTE = 3;

    private static final int REQUEST_CODE_USER_ACTION = 4;

    private RecyclerView notesRecyclerView;
    private EditText Searchbar;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private ImageButton userButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        Searchbar = findViewById(R.id.inputSearch);
        userButton = findViewById(R.id.userMenu);

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

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
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

    public void AuthPrefs(Context context) {
        prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.overflow_user_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.userAction) {
                    startActivityForResult(
                            new Intent(getApplicationContext(), UserActivity.class),
                            REQUEST_CODE_USER_ACTION);
                    return true;
                } else if (id == R.id.backup) {
                    backupNote();
                    return true;
                } else if (id == R.id.retrieve) {
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void backupNote() {
        ApiClient apiClient = new ApiClient(this);
        String authToken = getAuthToken(); // Get from SharedPreferences

        try {
            JSONObject noteData = new JSONObject();
            noteData.put("title", "My Note");
            noteData.put("content", "This is my note content");

            apiClient.createOrUpdateNote(authToken, noteData, new ApiClient.ApiResponseListener() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        String noteId = response.getString("id");
                        // Handle success
                    } catch (JSONException e) {
                        Log.e("API", "Error parsing response", e);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("API", "Error: " + errorMessage);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void retrieveNote() {

        ApiClient apiClient = new ApiClient(this);
        String authToken = getAuthToken();

        apiClient.getNote(authToken, new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String title = response.getString("title");
                    String content = response.getString("content");
                    // Update UI with note data
                } catch (JSONException e) {
                    Log.e("API", "Error parsing note", e);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("API", "Error fetching note: " + errorMessage);
            }
        });
    }

    public String getAuthToken() {
        return prefs.getString("authToken", null);
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