package com.example.mamaktech_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Note> note = new ArrayList<>();
    //Maybe export Hashmap to json? each note represents one json?
    private HashMap<String, Note> noteMap = new HashMap<>();
    private TextView textViewEditable;
    private EditText editText;
    private ScrollView scrollView;
    private ViewGroup parentLayout;
    private LinearLayout ContentContainer;
    private LinearLayout addContentLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        addContentLayout = findViewById(R.id.addContentLayout);
        addEditTextDefault();
        ImageView imageView2 = findViewById(R.id.menuIcon);
        textViewEditable = findViewById(R.id.textViewTitle);

        parentLayout = (ViewGroup) textViewEditable.getParent();

        scrollView = findViewById(R.id.scrollViewContent);
        ContentContainer = findViewById(R.id.addContentLayout);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PopupMenuDebug", "imageView2 clicked");
                showPopupMenu(v);
            }
        });

        textViewEditable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToEditText();
            }
        });
    }

    private void showPopupMenu(View view) {
        Log.d("PopupMenuDebug", "PopupMenu created");

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.overflow_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.Share) {
                addChecklistNote("");
                Toast.makeText(MainActivity.this, "Share clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.Pin) {
                Toast.makeText(MainActivity.this, "Pin clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.ConvertPdf) {
                Toast.makeText(MainActivity.this, "Convert to PDF clicked", Toast.LENGTH_SHORT).show();
            } else {
                return false;
            }
            return true;
        });

        popupMenu.show();
    }

    private void switchToEditText() {
        if (parentLayout == null) return;

        editText = new EditText(this);
        editText.setLayoutParams(textViewEditable.getLayoutParams());
        editText.setTextSize(20);
        editText.setText(textViewEditable.getText());
        editText.setPadding(60, 0 , 0, 0);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.requestFocus();

        int index = parentLayout.indexOfChild(textViewEditable);
        parentLayout.removeView(textViewEditable);
        parentLayout.addView(editText, index);

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                switchToTextView();
            }
        });
    }

    private void switchToTextView() {
        if (parentLayout == null) return;

        textViewEditable.setText(editText.getText().toString());

        int index = parentLayout.indexOfChild(editText);
        parentLayout.removeView(editText);
        parentLayout.addView(textViewEditable, index);
    }

    public void addTextNote(String text) {
        Note newNote = new Note(text);
        note.add(newNote);
        noteMap.put(newNote.getId(), newNote);
        addTextViewToLayout(text);
    }

    public void addImageNote(Uri imageUri) {
        Note newNote = new Note(imageUri);
        note.add(newNote);
        noteMap.put(newNote.getId(), newNote);
        addImageViewToLayout(newNote.getImageUri());
    }

    public void addChecklistNote(String checklistText) {
        Note newNote = new Note(checklistText, false);
        note.add(newNote);
        noteMap.put(newNote.getId(), newNote);
        addChecklistToLayout(checklistText, false);
    }
    private void addTextViewToLayout(String text) {
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 16, 16, 16);
        editText.setLayoutParams(params);
        editText.setHint("Enter text here...");
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTextColor(Color.BLACK);
        editText.setHintTextColor(Color.GRAY);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        editText.setText(text);
        addContentLayout.addView(editText);
    }

    private void addImageViewToLayout(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        addContentLayout.addView(imageView);
    }

    private void addChecklistToLayout(String text, boolean isChecked) {
        LinearLayout checkLayout = new LinearLayout(this);
        CheckBox checkBox = new CheckBox(this);
        EditText editText = new EditText(this);

        editText.setBackgroundColor(Color.TRANSPARENT);
        checkBox.setChecked(isChecked);
        editText.setText(text.length() != 0 ? text : "Text here!");

        checkLayout.addView(checkBox);
        checkLayout.addView(editText);
        addContentLayout.addView(checkLayout);
    }

    private void addEditTextDefault() {
        if (note.isEmpty()) {
            addTextNote("");
        } else {
            for (Note item : note) {
                switch (item.getType()) {
                    case Note.TYPE_TEXT:
                        addTextViewToLayout(item.getText());
                        break;
                    case Note.TYPE_IMAGE:
                        addImageViewToLayout(item.getImageUri());
                        break;
                    case Note.TYPE_CHECKLIST:
                        addChecklistToLayout(item.getCheckList(), item.isChecked());
                        break;
                }
            }
        }
    }
}
