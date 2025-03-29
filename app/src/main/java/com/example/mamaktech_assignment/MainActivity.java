package com.example.mamaktech_assignment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

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

        scrollView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                addNewEditText();
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

    private void addNewEditText() {
        EditText editText = new EditText(this);
        editText.setHint("Enter text here...");
        editText.setBackground(null);
        editText.setPadding(20, 10, 20, 10);

        ContentContainer.addView(editText);
    }
}
