package com.example.mamaktech_assignment.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamaktech_assignment.R;
import com.example.mamaktech_assignment.database.NotesDatabase;
import com.example.mamaktech_assignment.entities.Note;
import com.example.mamaktech_assignment.entities.NoteContent;
import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class EditActivity extends AppCompatActivity {

    private Button pickColorButton;
    private int mDefaultColor;
    private LinearLayout layoutDrawTools, layoutTextTools;
    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private ImageView addChecklistIcon, addImageIcon, drawToolsIcon, textToolsIcon, textSpeechIcon, displayColor;
    private Slider colorSlider;
    private Note alreadyAvailableNote;
    private boolean isViewOrUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        layoutDrawTools = findViewById(R.id.layoutDrawTools);
        layoutTextTools = findViewById(R.id.layoutTextTools);
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        addChecklistIcon = findViewById(R.id.addChecklist);
        addImageIcon = findViewById(R.id.addImage);
        drawToolsIcon = findViewById(R.id.drawTools);
        textToolsIcon = findViewById(R.id.textTools);
        textSpeechIcon = findViewById(R.id.textSpeech);
        displayColor = findViewById(R.id.displayColor);

        textToolsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (layoutTextTools.getVisibility() == View.VISIBLE) {
                    layoutTextTools.setVisibility(View.GONE);
                } else {
                    layoutTextTools.setVisibility(View.VISIBLE);
                }
            }
        });

        drawToolsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (layoutDrawTools.getVisibility() == View.VISIBLE) {
                    layoutDrawTools.setVisibility(View.GONE);
                } else {
                    layoutDrawTools.setVisibility(View.VISIBLE);
                }
            }
        });

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        ImageView imageSave = findViewById(R.id.imageSave);
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        pickColorButton = findViewById(R.id.pickColorButton);

        mDefaultColor = 0;

        pickColorButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openColorPickerDialogue();
                    }
                });

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            isViewOrUpdate = true;
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
    }

    private void setViewOrUpdateNote() {
        if (alreadyAvailableNote != null) {
            inputNoteTitle.setText(alreadyAvailableNote.getTitle());
            inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());

            StringBuilder noteText = new StringBuilder();
            if (alreadyAvailableNote.getNoteContentList() != null && !alreadyAvailableNote.getNoteContentList().isEmpty()) {
                for (NoteContent content : alreadyAvailableNote.getNoteContentList()) {
                    if (content.getText() != null) {
                        noteText.append(content.getText()).append("\n");
                    } else if (content.getCheckText() != null) {
                        noteText.append(content.isCheckBool() ? "☑ " : "☐ ")
                                .append(content.getCheckText()).append("\n");
                    } else if (content.getImagePath() != null) {
                        noteText.append("[Image: ").append(content.getImagePath()).append("]\n");
                    }
                }
            }
            inputNoteText.setText(noteText.toString().trim());

            textDateTime.setText(alreadyAvailableNote.getDateTime());
        }
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputNoteSubtitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = isViewOrUpdate ? alreadyAvailableNote : new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());

        if (!isViewOrUpdate) {
            note.setDateTime(textDateTime.getText().toString());
        } else {
            note.setDateTime(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                    .format(new Date()));
        }

        if (isViewOrUpdate && note.getNoteContentList() != null) {
            note.getNoteContentList().clear();
        }

        if (!inputNoteText.getText().toString().trim().isEmpty()) {
            note.insertTextOrCheck(NoteContent.TYPE_TEXT, inputNoteText.getText().toString());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Log.d("SAVE_NOTE", "Attempting to save note: " + note.getTitle());
                    Log.d("SAVE_NOTE", "Note content count: " +
                            (note.getNoteContentList() != null ? note.getNoteContentList().size() : "null"));

                    NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                    Log.d("SAVE_NOTE", "Note saved successfully");
                    return null;
                } catch (Exception e) {
                    Log.e("SAVE_NOTE", "Error saving note", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                Log.d("SAVE_NOTE", "Returning to MainActivity with RESULT_OK");
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void deleteNote() {
        if (alreadyAvailableNote != null) {
            @SuppressLint("StaticFieldLeak")
            class DeleteNoteTask extends AsyncTask<Void, Void, Void> {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        NotesDatabase.getDatabase(getApplicationContext())
                                .noteDao().deleteNote(alreadyAvailableNote);
                        Log.d("DELETE_NOTE", "Note deleted successfully");
                        return null;
                    } catch (Exception e) {
                        Log.e("DELETE_NOTE", "Error deleting note", e);
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Intent intent = new Intent();
                    intent.putExtra("isNoteDeleted", true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            new DeleteNoteTask().execute();
        }
    }

    public void openColorPickerDialogue() {

        final AmbilWarnaDialog colorPickerDialogue = new AmbilWarnaDialog(this, mDefaultColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {

                        mDefaultColor = color;
                        displayColor.setColorFilter(mDefaultColor);
                    }
                });
        colorPickerDialogue.show();
    }
}