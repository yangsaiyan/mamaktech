package com.example.mamaktech_assignment.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.mamaktech_assignment.entities.Note;
import com.example.mamaktech_assignment.entities.NoteContent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class PDFGenerator {
    private static final String TAG = "PDFGenerator";
    private static final int MAX_PDF_FILES = 20;

    public static void generatePdfFromNotes(Context context, Note note, String type) {

        final int MAX_IMAGE_HEIGHT = 450;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(612, 792, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = 80;
        int titleTextSize = 18;
        int contentTextSize = 14;
        int dateTextSize = 10;
        int textSize = 12;
        int margin = 40;
        int spacing = 20;
        int usableWidth = pageInfo.getPageWidth() - (2 * margin);

        Paint titlePaint = getPaint(titleTextSize);
        titlePaint.setFakeBoldText(true);
        canvas.drawText(note.getTitle(), margin, y, titlePaint);
        y += titleTextSize + 10;

        if (note.getSubtitle() != null && !note.getSubtitle().trim().isEmpty()) {
            canvas.drawText(note.getSubtitle(), margin, y, getPaint(contentTextSize));
            y += contentTextSize + 10;
        }

        Paint datePaint = getPaint(dateTextSize);
        datePaint.setColor(Color.GRAY);
        canvas.drawText(note.getDateTime(), margin, y, datePaint);
        y += dateTextSize + spacing;

        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(1);
        canvas.drawLine(margin, y, pageInfo.getPageWidth() - margin, y, linePaint);
        y += spacing;

        for (NoteContent content : note.getNoteContentList()) {
            if (y > 700) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 80;
            }

            if (content.typeCheck() == NoteContent.TYPE_TEXT) {
                String text = content.getText();
                Paint textPaint = getPaint(textSize);

                if (content.getTextFormatting() != null) {
                    String formatting = content.getTextFormatting();
                    if (formatting.contains("bold")) {
                        textPaint.setFakeBoldText(true);
                    }
                    if (formatting.contains("italic")) {
                        textPaint.setTextSkewX(-0.25f);
                    }
                    if (formatting.contains("underline")) {
                        canvas.drawText(text, margin, y, textPaint);
                        canvas.drawLine(margin, y + 2, margin + textPaint.measureText(text), y + 2, textPaint);
                        y += textSize + spacing;
                        continue;
                    }
                }

                canvas.drawText(text, margin, y, textPaint);
                y += textSize + spacing;

            } else if (content.typeCheck() == NoteContent.TYPE_CHECK) {
                Paint checkBoxPaint = new Paint();
                checkBoxPaint.setColor(Color.BLACK);
                checkBoxPaint.setStyle(Paint.Style.STROKE);
                checkBoxPaint.setStrokeWidth(1);

                RectF checkBox = new RectF(margin, y - 10, margin + 15, y + 5);
                canvas.drawRect(checkBox, checkBoxPaint);

                if (content.isCheckBool()) {
                    Paint checkmarkPaint = new Paint();
                    checkmarkPaint.setColor(Color.BLACK);
                    checkmarkPaint.setStrokeWidth(2);
                    canvas.drawLine(margin + 3, y - 2, margin + 6, y + 2, checkmarkPaint);
                    canvas.drawLine(margin + 6, y + 2, margin + 12, y - 7, checkmarkPaint);
                }

                canvas.drawText(content.getCheckText(), margin + 25, y, getPaint(textSize));
                y += textSize + spacing;

            } else if (content.typeCheck() == NoteContent.TYPE_IMAGE) {
                try {
                    String imageBase64 = content.getImagePath();
                    Bitmap imageBitmap = null;

                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        if (isBase64Image(imageBase64)) {
                            byte[] decodedString = Base64.decode(extractBase64Data(imageBase64), Base64.DEFAULT);
                            imageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        } else {
                            imageBitmap = loadImageFromPath(context, imageBase64);
                        }
                    }

                    if (imageBitmap != null) {
                        float scaledWidth, scaledHeight;
                        float pageWidth = usableWidth;

                        if (imageBitmap.getWidth() > pageWidth) {
                            float ratio = pageWidth / imageBitmap.getWidth();
                            scaledWidth = pageWidth;
                            scaledHeight = imageBitmap.getHeight() * ratio;
                        } else {
                            scaledWidth = imageBitmap.getWidth();
                            scaledHeight = imageBitmap.getHeight();
                        }

                        if (scaledHeight > MAX_IMAGE_HEIGHT) {
                            float ratio = MAX_IMAGE_HEIGHT / scaledHeight;
                            scaledHeight = MAX_IMAGE_HEIGHT;
                            scaledWidth = scaledWidth * ratio;
                        }

                        if (y + scaledHeight > 700) {
                            document.finishPage(page);
                            page = document.startPage(pageInfo);
                            canvas = page.getCanvas();
                            y = 80;
                        }

                        Rect src = new Rect(0, 0, imageBitmap.getWidth(), imageBitmap.getHeight());
                        RectF dst = new RectF(margin, y, margin + scaledWidth, y + scaledHeight);
                        canvas.drawBitmap(imageBitmap, src, dst, null);

                        y += scaledHeight + spacing;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing image: " + e.getMessage());
                    Paint errorPaint = new Paint();
                    errorPaint.setColor(Color.LTGRAY);
                    canvas.drawRect(margin, y, margin + 100, y + 50, errorPaint);
                    canvas.drawText("Image Error", margin + 10, y + 30, getPaint(textSize));
                    y += 50 + spacing;
                }
            }
        }

        document.finishPage(page);

        limitPdfCount(context, MAX_PDF_FILES);

        FileOutputStream outputStream = null;
        File pdfFile = null;

        try {
            pdfFile = createPdfFile(context, note.getTitle());
            Log.d(TAG, "PDF file path: " + pdfFile.getAbsolutePath());

            File parent = pdfFile.getParentFile();
            if (parent != null && !parent.exists()) {
                boolean created = parent.mkdirs();
                Log.d(TAG, "Created directories: " + created);
            }

            outputStream = new FileOutputStream(pdfFile);
            document.writeTo(outputStream);
            Log.d(TAG, "PDF created successfully");

            if(type.equals("convertPDF")) {
                openPdf(context, pdfFile);
            } else {
                sharePdf(context, pdfFile);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating PDF: " + e.getMessage(), e);
            Toast.makeText(context, "Failed to create PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing output stream: " + e.getMessage());
                }
            }
        }
    }

    private static boolean isBase64Image(String str) {
        return str != null && (str.startsWith("data:image") ||
                str.startsWith("iVBORw0KGgo") ||
                str.startsWith("/9j/") ||
                str.startsWith("R0lGOD"));
    }

    private static String extractBase64Data(String base64String) {
        if (base64String.startsWith("data:image")) {
            return base64String.substring(base64String.indexOf(",") + 1);
        }
        return base64String;
    }

    private static Bitmap loadImageFromPath(Context context, String imagePath) {
        Bitmap imageBitmap = null;

        try {
            if (imagePath.startsWith("content://")) {
                Uri imageUri = Uri.parse(imagePath);
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load from content URI: " + e.getMessage());
                    String realPath = getRealPathFromURI(context, imageUri);
                    if (!realPath.isEmpty()) {
                        imageBitmap = BitmapFactory.decodeFile(realPath);
                    }
                }
            } else {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    imageBitmap = BitmapFactory.decodeFile(imagePath);
                } else {
                    try {
                        Uri imageUri = Uri.parse(imagePath);
                        imageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to load image: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image from path: " + e.getMessage());
        }

        return imageBitmap;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String filePath = "";

        if (contentUri.toString().startsWith("content://media/")) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                Log.e("ImagePath", "Error getting real path: " + e.getMessage());
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        else if (contentUri.toString().startsWith("file://")) {
            filePath = contentUri.getPath();
        }
        else if (contentUri.toString().startsWith("content://")) {
            try {
                String fileName = getFileNameFromUri(context, contentUri);
                File destFile = new File(context.getCacheDir(), fileName);

                InputStream inputStream = context.getContentResolver().openInputStream(contentUri);
                FileOutputStream outputStream = new FileOutputStream(destFile);

                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();

                filePath = destFile.getAbsolutePath();
            } catch (Exception e) {
                Log.e("ImagePath", "Error copying file: " + e.getMessage());
            }
        }

        return filePath;
    }

    private static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static Paint getPaint(int textSize) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        return paint;
    }

    private static File createPdfFile(Context context, String noteTitle) {
        File docsDir;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            docsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Notes");
        } else {
            docsDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "Notes");
        }

        if (!docsDir.exists()) {
            boolean created = docsDir.mkdirs();
            Log.d(TAG, "Created directory " + docsDir.getPath() + ": " + created);
        }

        String safeTitle = noteTitle.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (safeTitle.length() > 20) safeTitle = safeTitle.substring(0, 20);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = safeTitle + "_" + timeStamp + ".pdf";

        return new File(docsDir, fileName);
    }

    private static void limitPdfCount(Context context, int maxCount) {
        try {
            File docsDir;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                docsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Notes");
            } else {
                docsDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), "Notes");
            }

            if (docsDir.exists()) {
                File[] files = docsDir.listFiles(file -> file.isFile() && file.getName().endsWith(".pdf"));

                if (files != null && files.length >= maxCount) {
                    Log.d(TAG, "Found " + files.length + " PDF files, cleaning up older files...");

                    Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

                    for (int i = 0; i < files.length - maxCount + 1; i++) {
                        boolean deleted = files[i].delete();
                        Log.d(TAG, "Deleted old PDF: " + files[i].getName() + ", success: " + deleted);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limiting PDF files: " + e.getMessage(), e);
        }
    }

    private static void openPdf(Context context, File pdfFile) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF: " + e.getMessage());
            Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show();
        }
    }

    private static void sharePdf(Context context, File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    pdfFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"));

        } catch (Exception e) {
            Log.e(TAG, "Error sharing PDF: " + e.getMessage(), e);
            Toast.makeText(context, "Failed to share PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}