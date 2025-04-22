package com.example.mamaktech_assignment.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class EditActivity extends AppCompatActivity {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_CHECK = 1;
    public static final int TYPE_IMAGE = 2;
    private List<NoteContent> noteContentList = new ArrayList<>();
    private Button pickColorButton;
    private int mDefaultColor;
    private LinearLayout layoutDrawTools, layoutTextTools, contentContainer;
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

        contentContainer = findViewById(R.id.contentContainer);
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
        addChecklistIcon = findViewById(R.id.addChecklist);

        addChecklistIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addChecklistContent(false, "");
            }
        });

        addImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                }else{
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

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
        } else {
            addTextContent("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            addImage(selectedImage);
        }
    }

    private void setViewOrUpdateNote() {
        if (alreadyAvailableNote != null) {
            inputNoteTitle.setText(alreadyAvailableNote.getTitle());
            inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());

            noteContentList.clear();
            contentContainer.removeAllViews();

            Log.d("DISPLAY", "Current size before display: " + alreadyAvailableNote.getNoteContentList().size());

            if (alreadyAvailableNote.getNoteContentList() != null && !alreadyAvailableNote.getNoteContentList().isEmpty()) {
                for (NoteContent content : alreadyAvailableNote.getNoteContentList()) {
                    Log.d("DISPLAY", "Current: " + content.getText());
                    if (content.typeCheck() == NoteContent.TYPE_TEXT && content.getText() != null) {
                        addTextContent(content.getText());
                    }else if (content.typeCheck() == NoteContent.TYPE_CHECK && content.getCheckText() != null) {
                        addChecklistContent(content.isCheckBool(), content.getCheckText());
                    }else if (content.typeCheck() == NoteContent.TYPE_IMAGE && content.getImagePath() != null) {
                        addImage(Uri.parse(content.getImagePath()));
                    }
                }
            }
            textDateTime.setText(alreadyAvailableNote.getDateTime());
        }
    }

    private void addTextContent(String text) {
        final NoteContent noteContent = new NoteContent(NoteContent.TYPE_TEXT, text);
        noteContentList.add(noteContent);

        LayoutInflater inflater = LayoutInflater.from(this);
        View noteTextView = inflater.inflate(R.layout.note_text, contentContainer, false);
        EditText editText = noteTextView.findViewById(R.id.inputNote);
        editText.setText(text);
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                noteContent.setText(s.toString());
                Log.d("TEXT_UPDATE", "Updated text: " + s.toString());
            }
        });
        contentContainer.addView(noteTextView);
    }

    private void addChecklistContent(Boolean checkStatus, String text) {
        final NoteContent noteContent = new NoteContent(checkStatus, text);
        noteContentList.add(noteContent);

        LayoutInflater inflater = LayoutInflater.from(this);
        View noteTextView = inflater.inflate(R.layout.note_checklist, contentContainer, false);
        LinearLayout lay = noteTextView.findViewById(R.id.inputCheckLayout);
        CheckBox checkBox = noteTextView.findViewById(R.id.editCheckBox);
        EditText editText = noteTextView.findViewById(R.id.editCheckText);

        checkBox.setChecked(checkStatus);
        editText.setText(text);
        editText.setBackground(null);

        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                noteContent.setCheckText(s.toString());
                Log.d("TEXT_UPDATE", "Updated text: " + s.toString());
            }
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            noteContent.setCheckBool(isChecked);
            Log.d("CHECK_UPDATE", "Checked: " + isChecked);
        });

        contentContainer.addView(noteTextView);
    }

    private void addImage(Uri imagePath) {
        final NoteContent noteContent = new NoteContent(NoteContent.TYPE_IMAGE, String.valueOf(imagePath));
        noteContentList.add(noteContent);

        LayoutInflater inflater = LayoutInflater.from(this);
        View noteTextView = inflater.inflate(R.layout.note_image, contentContainer, false);
        ImageView imageView = noteTextView.findViewById(R.id.inputImage);
        imageView.setImageURI(imagePath);

        contentContainer.addView(noteTextView);
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

        Log.d("SAVE_NOTE", "noteContentList: " + noteContentList.size());
        for(NoteContent content: noteContentList){
            Log.d("SAVE_NOTE", "CHECK_SAVING_CONTENT: " + content.getText());
            if (content.typeCheck() == NoteContent.TYPE_TEXT) {
                note.insertText(content.getText());
            } else if (content.typeCheck() == NoteContent.TYPE_CHECK) {
                note.insertCheck(content.isCheckBool(), content.getCheckText());
            } else if(content.typeCheck() == NoteContent.TYPE_IMAGE) {
                note.insertImage(String.valueOf(content.getImagePath()));
            }
        }

        Log.d("SAVE_NOTE", "Note size " + note.getNoteContentList().size());
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