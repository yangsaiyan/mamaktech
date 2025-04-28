package com.example.mamaktech_assignment.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamaktech_assignment.R;
import com.example.mamaktech_assignment.dao.NoteDao;
import com.example.mamaktech_assignment.database.NotesDatabase;
import com.example.mamaktech_assignment.entities.Board;
import com.example.mamaktech_assignment.entities.Note;
import com.example.mamaktech_assignment.entities.NoteContent;
import com.example.mamaktech_assignment.utils.PDFGenerator;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import yuku.ambilwarna.AmbilWarnaDialog;

public class EditActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private int mDefaultColor;
    private boolean isViewOrUpdate = false, isLoading = true, isListening = false;
    private List<NoteContent> noteContentList = new ArrayList<>();
    private Board drawBoard;
    private Button pickColorButton;
    private LinearLayout layoutDrawTools, layoutTextTools, layoutDrawBoard, contentContainer;
    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private ImageView addChecklistIcon, addImageIcon, drawToolsIcon, textToolsIcon, textSpeechIcon,
            displayColor, drawStroke1, drawStroke2, drawStroke3, drawStroke4, drawStroke5, textBold,
            textItalic, textUnderline, textFontInc, textFontDcr, imageSave, imageBack;
    private Note alreadyAvailableNote;
    private Button saveDrawBoardButton, clearDrawBoardButton;
    private ImageButton menuButton;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //Request permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        mDefaultColor = 0;
        findViewsByIds();
        allSetListeners();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION
            );
        }

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );


        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            isViewOrUpdate = true;
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
            isLoading = false;
        } else {
            addTextContent(new NoteContent(NoteContent.TYPE_TEXT, "", ""));
            isLoading = false;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String base64Image = convertUriToBase64(selectedImage);
            addImage(base64Image);
        }
    }

    private void allSetListeners() {
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

        textBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textActions("bold");
            }
        });

        textItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textActions("italic");
            }
        });

        textUnderline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textActions("underline");
            }
        });

        textFontInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textActions("fontInc");
            }
        });

        textFontDcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textActions("fontDcr");
            }
        });

        changeStrokeSizeListeners();
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                } else {
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

        textSpeechIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isListening) {
                    speechRecognizer.stopListening();
                    isListening = false;
                } else {
                    startVoiceRecognition();
                }
            }
        });

        drawToolsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (layoutDrawTools.getVisibility() == View.VISIBLE) {

                    layoutDrawTools.setVisibility(View.GONE);
                    layoutDrawBoard.setVisibility(View.GONE);
                } else {
                    layoutDrawTools.setVisibility(View.VISIBLE);
                    layoutDrawBoard.setVisibility(View.VISIBLE);
                }
            }
        });

        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        pickColorButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openColorPickerDialogue();
                    }
                });

        saveDrawBoardButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        final Uri drawBoardImage = drawBoard.saveToImage(String.valueOf(Math.random() * 1000000001) + "NoteDown");
                        String base64Image = convertUriToBase64(drawBoardImage);
                        addImage(base64Image);
                    }
                });

        clearDrawBoardButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        drawBoard.clearDrawing();
                    }
                });
    }

    private void findViewsByIds(){
        contentContainer = findViewById(R.id.contentContainer);
        layoutDrawTools = findViewById(R.id.layoutDrawTools);
        layoutTextTools = findViewById(R.id.layoutTextTools);
        layoutDrawBoard = findViewById(R.id.layoutDrawBoard);
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
        drawBoard = findViewById(R.id.drawBoard);
        saveDrawBoardButton = findViewById(R.id.drawBoardSave);
        clearDrawBoardButton = findViewById(R.id.drawBoardClear);
        drawStroke1 = findViewById(R.id.drawStroke1);
        drawStroke2 = findViewById(R.id.drawStroke2);
        drawStroke3 = findViewById(R.id.drawStroke3);
        drawStroke4 = findViewById(R.id.drawStroke4);
        drawStroke5 = findViewById(R.id.drawStroke5);
        textBold = findViewById(R.id.textBold);
        textItalic = findViewById(R.id.textItalic);
        textUnderline = findViewById(R.id.textUnderline);
        textFontInc = findViewById(R.id.textFontIncrease);
        textFontDcr = findViewById(R.id.textFontDecrease);
        menuButton = findViewById(R.id.menu);
        imageSave = findViewById(R.id.imageSave);
        imageBack = findViewById(R.id.imageBack);
        pickColorButton = findViewById(R.id.pickColorButton);
    }

    private void setViewOrUpdateNote() {
        if (alreadyAvailableNote != null) {
            inputNoteTitle.setText(alreadyAvailableNote.getTitle());
            inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());

            noteContentList.clear();
            contentContainer.removeAllViews();

            if (alreadyAvailableNote.getNoteContentList() != null && !alreadyAvailableNote.getNoteContentList().isEmpty()) {
                for (NoteContent content : alreadyAvailableNote.getNoteContentList()) {
                    if (content.typeCheck() == NoteContent.TYPE_TEXT && content.getText() != null) {
                        addTextContent(content);
                    } else if (content.typeCheck() == NoteContent.TYPE_CHECK && content.getCheckText() != null) {
                        addChecklistContent(content.isCheckBool(), content.getCheckText());
                    } else if (content.typeCheck() == NoteContent.TYPE_IMAGE && content.getImagePath() != null) {
                        addImage(content.getImagePath());
                    }
                }
            }
            textDateTime.setText(alreadyAvailableNote.getDateTime());
        }
    }

    //Voice to Text - Start
    private void startVoiceRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission();
            return;
        }

        if (speechRecognizer == null) {
            initializeSpeechRecognizer();
        }

        if (isListening) {
            return;
        }

        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to add text");

            speechRecognizer.startListening(intent);
            isListening = true;
        } catch (Exception e) {
            Toast.makeText(EditActivity.this, "Start listening failed", Toast.LENGTH_SHORT).show();
            handleRecognitionError(SpeechRecognizer.ERROR_CLIENT);
        }
    }

    private void initializeSpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                runOnUiThread(() -> {
                    isListening = true;
                    Toast.makeText(EditActivity.this, "Speak now...", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                isListening = false;
            }

            @Override
            public void onError(int error) {
                isListening = false;
                runOnUiThread(() -> handleRecognitionError(error));
            }

            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    runOnUiThread(() -> {
                        String recognizedText = matches.get(0);
                        ArrayList<EditText> editTexts = new ArrayList<>();
                        findAllEditTexts(contentContainer, editTexts);

                        if (!editTexts.isEmpty()) {
                            EditText target = editTexts.get(editTexts.size() - 1);
                            target.setText(target.getText() + " " + recognizedText);
                        }
                    });
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void handleRecognitionError(int errorCode) {
        String errorMsg;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_CLIENT:
                errorMsg = "Please try speaking again";
                initializeSpeechRecognizer();
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorMsg = "Microphone permission required";
                requestAudioPermission();
                break;
            default:
                errorMsg = "Error: " + getErrorText(errorCode);
        }

        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        super.onDestroy();
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                speechRecognizer.stopListening();
                isListening = false;
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                speechRecognizer.stopListening();
                isListening = false;
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                speechRecognizer.stopListening();
                isListening = false;
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                speechRecognizer.stopListening();
                isListening = false;
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                speechRecognizer.stopListening();
                isListening = false;
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No speech recognized";
                speechRecognizer.stopListening();
                isListening = false;
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                speechRecognizer.stopListening();
                isListening = false;
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Server error";
                speechRecognizer.stopListening();
                isListening = false;
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Unknown error";
        }
        return message;
    }
    //Voice to Text - End

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.overflow_menu, popupMenu.getMenu());

        MenuItem pinItem = popupMenu.getMenu().findItem(R.id.pin);
        if (alreadyAvailableNote != null) {
            pinItem.setTitle(alreadyAvailableNote.isPinned() ? "Unpin" : "Pin");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.share) {
                    if(alreadyAvailableNote == null){
                        return false;
                    }
                    PDFGenerator.generatePdfFromNotes(EditActivity.this, alreadyAvailableNote, "share");
                    return true;
                } else if (id == R.id.pin) {
                    if(alreadyAvailableNote == null){
                        return false;
                    }
                    @SuppressLint("StaticFieldLeak")
                    class TogglePinTask extends AsyncTask<Void, Void, Void> {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase db = NotesDatabase.getDatabase(getApplicationContext());
                            NoteDao dao = db.noteDao();

                            if (alreadyAvailableNote.isPinned()) {
                                dao.unpinNote(alreadyAvailableNote.getId());
                                alreadyAvailableNote.setPinned(false);
                            } else {
                                dao.pinNote(alreadyAvailableNote.getId());
                                alreadyAvailableNote.setPinned(true);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            String message = alreadyAvailableNote.isPinned() ?
                                    "Note pinned successfully" : "Note unpinned successfully";
                            Toast.makeText(EditActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                    new TogglePinTask().execute();
                    return true;
                } else if (id == R.id.convertPdf) {
                    if(alreadyAvailableNote == null){
                        return false;
                    }
                    PDFGenerator.generatePdfFromNotes(EditActivity.this, alreadyAvailableNote, "convertPDF");
                    return true;
                } else if (id == R.id.delete) {
                    if(alreadyAvailableNote == null){
                        return false;
                    }
                    Log.d("DELETE_NOTE", "Menu clicked");
                    deleteNote();
                    return true;
                }

                return false;
            }
        });

        popupMenu.show();
    }

    private void changeStrokeSizeListeners() {

        drawStroke1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        drawBoard.setStrokeWidth(5f);
                    }
                });

        drawStroke2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        drawBoard.setStrokeWidth(8f);
                    }
                });

        drawStroke3.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        drawBoard.setStrokeWidth(12f);
                    }
                });

        drawStroke4.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        drawBoard.setStrokeWidth(15f);
                    }
                });

        drawStroke5.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        drawBoard.setStrokeWidth(20f);
                    }
                });
    }

    //Content - Start
    private void addTextContent(NoteContent content) {
        final NoteContent noteContent = new NoteContent(NoteContent.TYPE_TEXT, content.getText(), "");
        noteContentList.add(noteContent);

        LayoutInflater inflater = LayoutInflater.from(this);
        View noteTextView = inflater.inflate(R.layout.note_text, contentContainer, false);
        EditText editText = noteTextView.findViewById(R.id.inputNote);

        if (!Objects.equals(content.getTextFormatting(), "") && content.getTextFormatting().length() > 0) {
            restoreTextFormatting(editText, content);
        } else {
            editText.setText(content.getText());
        }

        editText.addTextChangedListener(new android.text.TextWatcher() {
            private boolean backspacePressed = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (after < count && s.length() <= 1) {
                    backspacePressed = true;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteContent.setText(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String currentText = s.toString();

                if (backspacePressed && currentText.isEmpty()) {
                    backspacePressed = false;

                    int currentPosition = noteContentList.indexOf(noteContent);

                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            if (currentPosition > 0) {

                                View previousView = contentContainer.getChildAt(currentPosition - 1);
                                EditText previousEditText =
                                        noteContentList.get(currentPosition - 1).typeCheck() == NoteContent.TYPE_CHECK ?
                                                previousView.findViewById(R.id.editCheckText) :
                                                previousView.findViewById(R.id.inputNote);

                                if(currentText.length() == 0) {

                                    noteContentList.remove(currentPosition);
                                    contentContainer.removeViewAt(currentPosition);
                                    if (previousEditText != null) {
                                        previousEditText.requestFocus();
                                        previousEditText.setSelection(previousEditText.getText().length());
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

        checkIsEmptyWhenBackspace(noteTextView, editText, noteContent);

        contentContainer.addView(noteTextView);
    }

    private void addChecklistContent(Boolean checkStatus, String text) {
        final NoteContent noteContent = new NoteContent(checkStatus, text);
        checkNoteContentListpreviousItem();
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
            private boolean backspacePressed = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (after < count && s.length() <= 1) {
                    backspacePressed = true;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteContent.setCheckText(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String currentText = s.toString();

                if (backspacePressed && currentText.isEmpty()) {
                    backspacePressed = false;

                    int currentPosition = noteContentList.indexOf(noteContent);

                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            if (currentPosition > 0) {

                                View previousView = contentContainer.getChildAt(currentPosition - 1);
                                EditText previousEditText =
                                        noteContentList.get(currentPosition - 1).typeCheck() == NoteContent.TYPE_CHECK ?
                                previousView.findViewById(R.id.editCheckText) :
                                                previousView.findViewById(R.id.inputNote);

                                if(currentText.length() == 0) {

                                    noteContentList.remove(currentPosition);
                                    contentContainer.removeViewAt(currentPosition);
                                    if (previousEditText != null) {
                                        previousEditText.requestFocus();
                                        previousEditText.setSelection(previousEditText.getText().length());
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            noteContent.setCheckBool(isChecked);
        });

        checkIsEmptyWhenBackspace(noteTextView, editText, noteContent);

        contentContainer.addView(noteTextView);
        checkNoteContentListLastItem();
    }

    private void addImage(String base64Image) {
        final NoteContent noteContent = new NoteContent(NoteContent.TYPE_IMAGE, base64Image, "");
        checkNoteContentListpreviousItem();
        noteContentList.add(noteContent);

        LayoutInflater inflater = LayoutInflater.from(this);
        View noteTextView = inflater.inflate(R.layout.note_image, contentContainer, false);
        ImageView imageView = noteTextView.findViewById(R.id.inputImage);

        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        drawBoard.clearDrawing();
        layoutDrawTools.setVisibility(View.GONE);
        layoutDrawBoard.setVisibility(View.GONE);
        contentContainer.addView(noteTextView);
        checkNoteContentListLastItem();
    }
    //Content - End

    //Content Image Utility
    private String convertUriToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Content Text Utility - Start
    private void findAllEditTexts(ViewGroup viewGroup, ArrayList<EditText> editTexts) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof EditText) {
                editTexts.add((EditText) child);
            } else if (child instanceof ViewGroup) {
                findAllEditTexts((ViewGroup) child, editTexts);
            }
        }
    }

    private void textActions(String type) {
        if (contentContainer == null) {
            return;
        }

        ArrayList<EditText> allEditTexts = new ArrayList<>();
        findAllEditTexts(contentContainer, allEditTexts);

        for (EditText editText : allEditTexts) {
            if (editText.hasFocus()) {
                Editable editable = editText.getText();
                int selStart = editText.getSelectionStart();
                int selEnd = editText.getSelectionEnd();

                if (selStart != selEnd) {
                    if (type.equals("bold")) {
                        editable.setSpan(new StyleSpan(Typeface.BOLD),
                                selStart, selEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return;
                    } else if (type.equals("italic")) {
                        editable.setSpan(new StyleSpan(Typeface.ITALIC),
                                selStart, selEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return;
                    } else if (type.equals("underline")) {
                        editable.setSpan(new UnderlineSpan(),
                                selStart, selEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return;
                    } else if (type.equals("fontInc")) {
                        editable.setSpan(new RelativeSizeSpan(1.25f),
                                selStart, selEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return;
                    } else if (type.equals("fontDcr")) {
                        editable.setSpan(new RelativeSizeSpan(0.8f),
                                selStart, selEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return;
                    }
                }
            }
        }
    }
    //Content Text Utility - End

    //Board_Draw Color Picker
    private void openColorPickerDialogue() {

        final AmbilWarnaDialog colorPickerDialogue = new AmbilWarnaDialog(this, mDefaultColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {

                        mDefaultColor = color;
                        displayColor.setColorFilter(mDefaultColor);
                        drawBoard.setColor(mDefaultColor);
                    }
                });
        colorPickerDialogue.show();
    }

    //Validation - Start
    private void checkIsEmptyWhenBackspace(View view, EditText editText, NoteContent noteContent) {

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_DEL &&
                        editText.getText().toString().isEmpty()) {

                    int currentPosition = noteContentList.indexOf(noteContent);

                    if (currentPosition > 0 && noteContentList.get(currentPosition - 1).typeCheck() != NoteContent.TYPE_TEXT) {
                        noteContentList.remove(currentPosition - 1);
                        contentContainer.removeViewAt(currentPosition - 1);

                        return true;
                    } else if (currentPosition > 0 &&
                            noteContentList.get(currentPosition - 1).typeCheck() == NoteContent.TYPE_TEXT){
                        noteContentList.remove(currentPosition);
                        contentContainer.removeViewAt(currentPosition);
                        View previousView = contentContainer.getChildAt(currentPosition - 1);
                        EditText previousEditText = previousView.findViewById(R.id.inputNote);
                        previousEditText.requestFocus();
                        previousEditText.setSelection(previousEditText.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void checkNoteContentListLastItem() {

        if (isLoading) return;

        if (noteContentList.get(noteContentList.size() - 1).typeCheck() != NoteContent.TYPE_TEXT) {
            addTextContent(new NoteContent(NoteContent.TYPE_TEXT, "", ""));
        }
    }

    private void checkNoteContentListpreviousItem() {

        if (isLoading) return;

        ArrayList<EditText> allEditTexts = new ArrayList<>();
        findAllEditTexts(contentContainer, allEditTexts);
        if (allEditTexts.get(allEditTexts.size() - 1).getText().toString().trim().isEmpty()
                && noteContentList.get(noteContentList.size() - 1).getText().equals("")) {
            allEditTexts.remove(allEditTexts.size() - 1);
            noteContentList.remove(noteContentList.size() - 1);
            contentContainer.removeViewAt(contentContainer.getChildCount() - 1);
        }
    }
    //Validation - End

    //Text Formatting - Start
    private String saveTextFormatting(EditText editText) {
        Editable text = editText.getText();
        JSONArray formattingArray = new JSONArray();

        StyleSpan[] styleSpans = text.getSpans(0, text.length(), StyleSpan.class);
        for (StyleSpan span : styleSpans) {
            try {
                JSONObject spanObj = new JSONObject();
                spanObj.put("start", text.getSpanStart(span));
                spanObj.put("end", text.getSpanEnd(span));
                spanObj.put("isBold", span.getStyle() == Typeface.BOLD);
                spanObj.put("isItalic", span.getStyle() == Typeface.ITALIC);
                formattingArray.put(spanObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        UnderlineSpan[] underlineSpans = text.getSpans(0, text.length(), UnderlineSpan.class);
        for (UnderlineSpan span : underlineSpans) {
            try {
                JSONObject spanObj = new JSONObject();
                spanObj.put("start", text.getSpanStart(span));
                spanObj.put("end", text.getSpanEnd(span));
                spanObj.put("isUnderline", true);
                formattingArray.put(spanObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        RelativeSizeSpan[] sizeSpans = text.getSpans(0, text.length(), RelativeSizeSpan.class);
        for (RelativeSizeSpan span : sizeSpans) {
            try {
                JSONObject spanObj = new JSONObject();
                spanObj.put("start", text.getSpanStart(span));
                spanObj.put("end", text.getSpanEnd(span));
                spanObj.put("sizeRatio", span.getSizeChange());
                formattingArray.put(spanObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("SHOW_JSON", formattingArray.toString());
        return formattingArray.toString();
    }

    private void restoreTextFormatting(EditText editText, NoteContent content) {
        String formattingData = content.getTextFormatting();
        if (formattingData == null || formattingData.isEmpty()) {
            return;
        }

        editText.setText(content.getText());
        Editable editable = editText.getText();

        try {
            JSONArray formattingArray = new JSONArray(formattingData);
            for (int i = 0; i < formattingArray.length(); i++) {
                JSONObject spanObj = formattingArray.getJSONObject(i);
                int start = spanObj.getInt("start");
                int end = spanObj.getInt("end");

                if (spanObj.has("isBold") && spanObj.getBoolean("isBold")) {
                    editable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (spanObj.has("isItalic") && spanObj.getBoolean("isItalic")) {
                    editable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (spanObj.has("isUnderline") && spanObj.getBoolean("isUnderline")) {
                    editable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (spanObj.has("sizeRatio")) {
                    float sizeRatio = (float) spanObj.getDouble("sizeRatio");
                    editable.setSpan(new RelativeSizeSpan(sizeRatio), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //Text Formatting - End

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

        ArrayList<EditText> allEditTexts = new ArrayList<>();
        findAllEditTexts(contentContainer, allEditTexts);

        for (int i = 0; i < noteContentList.size(); i++) {
            NoteContent content = noteContentList.get(i);

            if (content.typeCheck() == NoteContent.TYPE_TEXT) {
                String textFormatting = "";
                for (EditText editText : allEditTexts) {

                    if (editText.getText().toString().equals(content.getText())) {
                        textFormatting = saveTextFormatting(editText);
                    }
                }
                note.insertText(content.getText(), textFormatting);

            } else if (content.typeCheck() == NoteContent.TYPE_CHECK) {
                note.insertCheck(content.isCheckBool(), content.getCheckText());
            } else if (content.typeCheck() == NoteContent.TYPE_IMAGE) {
                note.insertImage(String.valueOf(content.getImagePath()));
            }
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                    return null;
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
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
                                .noteDao().deleteNote(alreadyAvailableNote.getId());
                        return null;
                    } catch (Exception e) {
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
}