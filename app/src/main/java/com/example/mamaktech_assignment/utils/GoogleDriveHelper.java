//package com.example.mamaktech_assignment.utils;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.util.Log;
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.Scope;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.DriveScopes;
//
//import java.util.Collections;
//
//import com.google.api.client.http.javanet.NetHttpTransport;
//
//
//public class GoogleDriveHelper {
//    private static final int REQUEST_CODE_SIGN_IN = 1;
//    private static final String TAG = "GoogleDriveHelper";
//
//    private final Activity activity;
//    private GoogleSignInClient googleSignInClient;
//    private GoogleDriveServiceHelper driveServiceHelper;
//
//    public GoogleDriveHelper(Activity activity) {
//        this.activity = activity;
//
//        // Configure sign-in options
//        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
//                .build();
//
//        googleSignInClient = GoogleSignIn.getClient(activity, signInOptions);
//    }
//
//    public void signIn() {
//        Intent signInIntent = googleSignInClient.getSignInIntent();
//        activity.startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
//    }
//
//    public void handleSignInResult(Intent data) {
//        GoogleSignIn.getSignedInAccountFromIntent(data)
//                .addOnSuccessListener(googleAccount -> {
//                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());
//
//                    // Initialize Drive service
//                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
//                            activity, Collections.singleton(DriveScopes.DRIVE_FILE));
//                    credential.setSelectedAccount(googleAccount.getAccount());
//
//                    Drive driveService = new Drive.Builder(
//                            new NetHttpTransport(), // Replace AndroidHttp.newCompatibleTransport()
//                            new GsonFactory(),
//                            credential)
//                            .setApplicationName("Your App Name")
//                            .build();
//
//                    driveServiceHelper = new GoogleDriveServiceHelper(driveService);
//                })
//                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in: " + exception));
//    }
//
//    public GoogleDriveServiceHelper getDriveServiceHelper() {
//        return driveServiceHelper;
//    }
//}
//
