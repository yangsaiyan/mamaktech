package com.example.mamaktech_assignment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ImageView imageView2 = findViewById(R.id.menuIcon);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PopupMenuDebug", "imageView2 clicked");
                showPopupMenu(v);
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
}
