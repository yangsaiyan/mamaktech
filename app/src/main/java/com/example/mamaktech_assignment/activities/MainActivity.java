package com.example.mamaktech_assignment.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.Toast;

import android.Manifest;
import com.example.mamaktech_assignment.R;
import com.example.mamaktech_assignment.adapters.NotesAdapter;
import com.example.mamaktech_assignment.dao.NoteDao;
import com.example.mamaktech_assignment.database.NotesDatabase;
import com.example.mamaktech_assignment.entities.Note;
import com.example.mamaktech_assignment.entities.NoteContent;
import com.example.mamaktech_assignment.utils.ApiClient;
import com.example.mamaktech_assignment.utils.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NotesAdapter.NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_VIEW_NOTE = 3;
    private static final int REQUEST_CODE_USER_ACTION = 4;
    private static final int REQUEST_STORAGE_PERMISSION = 5;

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

        requestStoragePermission();
        prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
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

                if (query.isEmpty()) {
                    getNotes();
                } else {
                    noteList.clear();
                    onSearchChange(query);
                }
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
                    backupNote(noteList);
                    return true;
                } else if (id == R.id.retrieve) {
                    retrieveNote();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void backupNote(List<Note> notes) {
        String authToken = getAuthToken();

        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = 1;
        ApiClient apiClient = new ApiClient(this);

        if (authToken == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONArray notesArray = new JSONArray();
            for (Note note : notes) {
                JSONObject noteJson = new JSONObject();
                noteJson.put("title", note.getTitle());
                noteJson.put("subtitle", note.getSubtitle());
                noteJson.put("date_time", note.getDateTime());
                noteJson.put("is_pinned", note.isPinned());
                String noteContentJson = new JsonConverter().fromNoteContentList(note.getNoteContentList());
                noteJson.put("note_content_list", new JSONArray(noteContentJson));

                notesArray.put(noteJson);
            }

            apiClient.uploadAllNotes(authToken, notesArray, new ApiClient.ApiResponseListener() {
                @Override
                public void onSuccess(JSONArray response) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Notes backed up successfully", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Notes backed up successfully", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Backup failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparing notes for upload", Toast.LENGTH_SHORT).show();
        }
    }

    private void retrieveNote() {
        String authToken = getAuthToken();

        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient apiClient = new ApiClient(this);
        apiClient.getAllNotes(authToken, new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String title = response.getString("title");
                    String content = response.getString("content");
                } catch (JSONException e) {
                }
            }

            @Override
            public void onSuccess(JSONArray response) {

                List<Note> notesFromServer = parseNotesFromJson(response);
                noteList.clear();
                noteList.addAll(notesFromServer);
                new Thread(() -> {
                    try {
                        NotesDatabase database = NotesDatabase.getDatabase(getApplicationContext());
                        NoteDao noteDao = database.noteDao();

                        noteDao.deleteAllNotes();

                        for (Note note : notesFromServer) {
                            noteDao.insertNote(note);
                        }

                    } catch (Exception e) {
                    }
                }).start();
                notesAdapter.setNotes(noteList);
            }


            @Override
            public void onError(String errorMessage) {
                Log.e("API", "Error fetching note: " + errorMessage);
            }
        });
    }

    private List<Note> parseNotesFromJson(JSONArray jsonArray) {
        List<Note> notes = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonNote = jsonArray.getJSONObject(i);
                Note note = new Note();

                note.setId(jsonNote.getInt("id"));
                note.setTitle(jsonNote.optString("title", ""));
                note.setSubtitle(jsonNote.optString("subtitle", ""));
                note.setDateTime(jsonNote.optString("date_time", ""));
                note.setPinned(jsonNote.optBoolean("is_pinned", false));

                JSONArray contentArray = jsonNote.getJSONArray("note_content_list");
                List<NoteContent> contentList = new ArrayList<>();

                for (int j = 0; j < contentArray.length(); j++) {
                    JSONObject contentObj = contentArray.getJSONObject(j);

                    int type = contentObj.optInt("type", 0);
                    if(type == NoteContent.TYPE_TEXT){

                        NoteContent noteContent = new NoteContent(type,
                                contentObj.optString("text", ""),
                                contentObj.optString("textFormatting", "[]"));
                        contentList.add(noteContent);
                    } else if(type == NoteContent.TYPE_CHECK) {

                        NoteContent noteContent = new
                                NoteContent(contentObj.getBoolean("checkBool"),
                                contentObj.optString("checkText", ""));
                        contentList.add(noteContent);
                    } else if(type == NoteContent.TYPE_IMAGE) {

                        NoteContent noteContent = new NoteContent(type,
                                contentObj.optString("imagePath", ""),
                                contentObj.optString("textFormatting", "[]"));
                        contentList.add(noteContent);
                    }
                }

                note.setNoteContentList(contentList);
                notes.add(note);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return notes;
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
                    NotesDatabase db = NotesDatabase.getDatabase(getApplicationContext());
                    NoteDao dao = db.noteDao();
                    List<Note> notes = dao.getAllNotes();
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
            getNotes();
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes();
            }
        }
    }
}