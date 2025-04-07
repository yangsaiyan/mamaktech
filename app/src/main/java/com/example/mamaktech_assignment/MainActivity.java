package com.example.mamaktech_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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
        editText.setBackground(null);
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

    public void addEditTextDefault() {
        EditText editText = new EditText(this);

        // Layout parameters with margins
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 16, 16, 16); // Add margins (left, top, right, bottom)
        editText.setLayoutParams(params);

        // Text and hint styling
        editText.setHint("Enter text here...");
        editText.setTextColor(Color.BLACK); // Set text color
        editText.setHintTextColor(Color.GRAY); // Set hint text color
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size in SP

        // Background and padding
        editText.setBackgroundResource(android.R.drawable.edit_text); // Default EditText background
        editText.setPadding(32, 32, 32, 32); // Internal padding (in pixels)

        // Minimum height to ensure visibility
        editText.setMinHeight(120); // Minimum height in pixels

        addContentLayout.addView(editText);
        addContentLayout.requestLayout();

        // Request focus and show keyboard after a slight delay
        editText.postDelayed(() -> {
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }
}
