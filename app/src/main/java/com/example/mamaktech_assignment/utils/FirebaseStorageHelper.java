//package com.example.mamaktech_assignment.utils;
//
//import android.content.Context;
//import android.net.Uri;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//
//import java.io.File;
//import java.util.List;
//import java.util.UUID;
//
//public class FirebaseStorageHelper {
//    private static final String TAG = "FirebaseStorageHelper";
//    private final FirebaseStorage storage;
//    private final Context context;
//
//    public FirebaseStorageHelper(Context context) {
//        this.context = context;
//        storage = FirebaseStorage.getInstance();
//    }
//
//    /**
//     * Upload a file to Firebase Storage
//     * @param fileUri The URI of the file to upload
//     * @param folderName The folder name where to store the file
//     * @param listener Callback to handle the result
//     */
//    public void uploadFile(Uri fileUri, String folderName, UploadResultListener listener) {
//        if (fileUri == null) {
//            if (listener != null) {
//                listener.onFailure("File URI is null");
//            }
//            return;
//        }
//
//        // Create a unique filename
//        String fileName = UUID.randomUUID().toString();
//
//        // Create a reference to the file location
//        StorageReference storageRef = storage.getReference();
//        StorageReference fileRef = storageRef.child(folderName + "/" + fileName);
//
//        // Upload the file
//        UploadTask uploadTask = fileRef.putFile(fileUri);
//
//        // Monitor the upload
//        uploadTask.addOnSuccessListener(taskSnapshot -> {
//            // Get the download URL
//            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
//                if (listener != null) {
//                    listener.onSuccess(downloadUri.toString(), fileRef.getPath());
//                }
//                Log.d(TAG, "File uploaded successfully: " + downloadUri.toString());
//            });
//        }).addOnFailureListener(e -> {
//            if (listener != null) {
//                listener.onFailure(e.getMessage());
//            }
//            Log.e(TAG, "Upload failed: " + e.getMessage());
//        }).addOnProgressListener(taskSnapshot -> {
//            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//            Log.d(TAG, "Upload is " + progress + "% done");
//            if (listener != null) {
//                listener.onProgress((int) progress);
//            }
//        });
//    }
//
//    /**
//     * Upload a local file to Firebase Storage
//     * @param localFile The local file to upload
//     * @param folderName The folder name where to store the file
//     * @param listener Callback to handle the result
//     */
//    public void uploadLocalFile(File localFile, String folderName, UploadResultListener listener) {
//        if (localFile == null || !localFile.exists()) {
//            if (listener != null) {
//                listener.onFailure("File does not exist");
//            }
//            return;
//        }
//
//        uploadFile(Uri.fromFile(localFile), folderName, listener);
//    }
//
//    /**
//     * Download a file from Firebase Storage
//     * @param storagePath The path to the file in Firebase Storage
//     * @param localFile The local file where to save the downloaded file
//     * @param listener Callback to handle the result
//     */
//    public void downloadFile(String storagePath, File localFile, DownloadResultListener listener) {
//        StorageReference fileRef = storage.getReference(storagePath);
//
//        fileRef.getFile(localFile)
//                .addOnSuccessListener(taskSnapshot -> {
//                    if (listener != null) {
//                        listener.onSuccess(localFile);
//                    }
//                    Log.d(TAG, "File downloaded successfully to: " + localFile.getAbsolutePath());
//                })
//                .addOnFailureListener(e -> {
//                    if (listener != null) {
//                        listener.onFailure(e.getMessage());
//                    }
//                    Log.e(TAG, "Download failed: " + e.getMessage());
//                })
//                .addOnProgressListener(taskSnapshot -> {
//                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                    Log.d(TAG, "Download is " + progress + "% done");
//                    if (listener != null) {
//                        listener.onProgress((int) progress);
//                    }
//                });
//    }
//
//    /**
//     * Delete a file from Firebase Storage
//     * @param storagePath The path to the file in Firebase Storage
//     * @param listener Callback to handle the result
//     */
//    public void deleteFile(String storagePath, DeleteResultListener listener) {
//        StorageReference fileRef = storage.getReference(storagePath);
//
//        fileRef.delete()
//                .addOnSuccessListener(aVoid -> {
//                    if (listener != null) {
//                        listener.onSuccess();
//                    }
//                    Log.d(TAG, "File deleted successfully");
//                })
//                .addOnFailureListener(e -> {
//                    if (listener != null) {
//                        listener.onFailure(e.getMessage());
//                    }
//                    Log.e(TAG, "Delete failed: " + e.getMessage());
//                });
//    }
//
//    /**
//     * List files in a folder
//     * @param folderPath The path to the folder in Firebase Storage
//     * @param listener Callback to handle the result
//     */
//    public void listFiles(String folderPath, ListResultListener listener) {
//        StorageReference folderRef = storage.getReference(folderPath);
//
//        folderRef.listAll()
//                .addOnSuccessListener(listResult -> {
//                    if (listener != null) {
//                        listener.onSuccess(listResult.getItems());
//                    }
//                    Log.d(TAG, "Files listed successfully");
//                })
//                .addOnFailureListener(e -> {
//                    if (listener != null) {
//                        listener.onFailure(e.getMessage());
//                    }
//                    Log.e(TAG, "List failed: " + e.getMessage());
//                });
//    }
//
//    // Interfaces for callbacks
//    public interface UploadResultListener {
//        void onSuccess(String downloadUrl, String storagePath);
//        void onFailure(String errorMessage);
//        void onProgress(int progress);
//    }
//
//    public interface DownloadResultListener {
//        void onSuccess(File localFile);
//        void onFailure(String errorMessage);
//        void onProgress(int progress);
//    }
//
//    public interface DeleteResultListener {
//        void onSuccess();
//        void onFailure(String errorMessage);
//    }
//
//    public interface ListResultListener {
//        void onSuccess(List<StorageReference> items);
//        void onFailure(String errorMessage);
//    }
//}