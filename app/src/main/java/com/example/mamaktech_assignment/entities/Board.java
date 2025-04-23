package com.example.mamaktech_assignment.entities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class Board extends View {
    private int rows = 8;
    private int cols = 8;
    private int cellSize;

    // Board paint
    private Paint whiteBackground;

    // Drawing paint
    private Paint drawPaint;
    private Path drawPath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    // Store all drawing paths
    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Paint> paintList = new ArrayList<>();

    // Current drawing properties
    private int currentColor = Color.BLACK;
    private float currentStrokeWidth = 5f;

    public Board(Context context) {
        super(context);
        init();
    }

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Board(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Board paints
        whiteBackground = new Paint();
        whiteBackground.setColor(Color.rgb(255, 255, 255));

        // Drawing setup
        drawPaint = new Paint();
        drawPaint.setColor(currentColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(currentStrokeWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        drawPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        // Ensure minimum size
        if (width <= 0) width = 300;
        if (height <= 0) height = 300;

        int size = Math.min(width, height);
        cellSize = size / Math.max(rows, cols);

        // Make sure cell size is at least 1
        if (cellSize < 1) cellSize = 1;

        setMeasuredDimension(cellSize * cols, cellSize * rows);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw board background
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int left = col * cellSize;
                int top = row * cellSize;
                int right = left + cellSize;
                int bottom = top + cellSize;

                Paint paint = whiteBackground;
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        // Draw all saved paths
        for (int i = 0; i < paths.size(); i++) {
            canvas.drawPath(paths.get(i), paintList.get(i));
        }

        // Draw current path
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    private void touchStart(float x, float y) {
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // Create a quadratic bezier curve for smoother lines
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        drawPath.lineTo(mX, mY);

        // Store the path
        Paint newPaint = new Paint(drawPaint);
        paths.add(new Path(drawPath));
        paintList.add(newPaint);

        // Reset the path for the next drawing
        drawPath.reset();
    }

    // Set the pen color
    public void setColor(int color) {
        currentColor = color;
        drawPaint.setColor(color);
    }

    // Set the pen stroke width
    public void setStrokeWidth(float width) {
        currentStrokeWidth = width;
        drawPaint.setStrokeWidth(width);
    }

    // Clear all drawings
    public void clearDrawing() {
        paths.clear();
        paintList.clear();
        drawPath.reset();
        invalidate();
    }

    // Method to save the board as an image
    public Uri saveToImage(String filename) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);

        OutputStream outputStream = null;
        Uri imageUri = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NoteDown");

                ContentResolver resolver = getContext().getContentResolver();
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri);
                }
            } else {
                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imageFile = new File(storageDir, filename + ".png");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                outputStream = new FileOutputStream(imageFile);
                imageUri = Uri.fromFile(imageFile);
            }

            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageUri;
    }


    // Method to set board dimensions
    public void setBoardSize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        requestLayout();
        invalidate();
    }
}