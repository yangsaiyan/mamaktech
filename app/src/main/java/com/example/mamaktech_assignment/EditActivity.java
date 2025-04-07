package com.example.mamaktech_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;


public class EditActivity extends AppCompatActivity {

    private LinearLayout addContentLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ImageView imageView2 = findViewById(R.id.menuIcon);
        addContentLayout = findViewById(R.id.addContentLayout);
        addEditTextDefault();
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PopupMenuDebug", "imageView2 clicked");
                showPopupMenu(v);
            }
        });
    }

    public void showPopupMenu(View view) {
        Log.d("PopupMenuDebug", "PopupMenu created");

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.overflow_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.Share) {
                Toast.makeText(EditActivity.this, "Share clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.Pin) {
                Toast.makeText(EditActivity.this, "Pin clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.ConvertPdf) {
                Toast.makeText(EditActivity.this, "Convert to PDF clicked", Toast.LENGTH_SHORT).show();
            } else {
                return false;
            }
            return true;
        });

        popupMenu.show();
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
