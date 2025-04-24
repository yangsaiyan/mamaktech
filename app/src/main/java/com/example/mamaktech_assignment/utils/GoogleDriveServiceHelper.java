//package com.example.mamaktech_assignment.utils;
//
//import com.google.android.gms.tasks.Task;
//import com.google.android.gms.tasks.Tasks;
//import com.google.api.client.http.FileContent;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.model.FileList;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Executors;
//
//public class GoogleDriveServiceHelper {
//    private static final String TAG = "DriveServiceHelper";
//    private final Drive driveService;
//
//    public GoogleDriveServiceHelper(Drive driveService) {
//        this.driveService = driveService;
//    }
//
//    /**
//     * Upload a file to Google Drive
//     */
//    public Task<String> uploadFile(final File localFile, final String mimeType) {
//        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
//            // Create file metadata
//            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
//            fileMetadata.setName(localFile.getName());
//
//            // File's content
//            FileContent mediaContent = new FileContent(mimeType, localFile);
//
//            // Upload file
//            com.google.api.services.drive.model.File file = driveService.files().create(fileMetadata, mediaContent)
//                    .setFields("id")
//                    .execute();
//
//            return file.getId();
//        });
//    }
//
//    /**
//     * Download a file from Google Drive
//     */
//    public Task<File> downloadFile(final String fileId, final File destinationFile) {
//        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
//            // Create output stream for downloaded file
//            OutputStream outputStream = new FileOutputStream(destinationFile);
//
//            // Download file
//            driveService.files().get(fileId)
//                    .executeMediaAndDownloadTo(outputStream);
//
//            return destinationFile;
//        });
//    }
//
//    /**
//     * List files from Google Drive
//     */
//    public Task<List<GoogleDriveFileHolder>> queryFiles() {
//        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
//            List<GoogleDriveFileHolder> fileList = new ArrayList<>();
//
//            // Query files
//            FileList result = driveService.files().list()
//                    .setFields("files(id, name, mimeType, size, createdTime)")
//                    .execute();
//
//            for (com.google.api.services.drive.model.File file : result.getFiles()) {
//                GoogleDriveFileHolder fileHolder = new GoogleDriveFileHolder();
//                fileHolder.setId(file.getId());
//                fileHolder.setName(file.getName());
//                fileHolder.setMimeType(file.getMimeType());
//                fileList.add(fileHolder);
//            }
//
//            return fileList;
//        });
//    }
//}
