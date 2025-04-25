package com.example.mamaktech_assignment.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamaktech_assignment.R;
import com.example.mamaktech_assignment.utils.ApiFetcher;
import com.intuit.sdp.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    private Button registerButton, importButton, importActionButton, confirmButton;
    private LinearLayout userSelection, userRegistration, userImport;
    private TextView secret1, secret2, secret3, secret4;
    private EditText secretInput1, secretInput2, secretInput3, secretInput4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        registerButton = findViewById(R.id.registerButton);
        importButton = findViewById(R.id.importButton);
        userSelection = findViewById(R.id.userSelection);
        userRegistration = findViewById(R.id.userRegistration);
        userImport = findViewById(R.id.userImport);
        secret1 = findViewById(R.id.secret1);
        secret2 = findViewById(R.id.secret2);
        secret3 = findViewById(R.id.secret3);
        secret4 = findViewById(R.id.secret4);
        secretInput1 = findViewById(R.id.secretInput1);
        secretInput2 = findViewById(R.id.secretInput2);
        secretInput3 = findViewById(R.id.secretInput3);
        secretInput4 = findViewById(R.id.secretInput4);
        importActionButton = findViewById(R.id.importActionButton);
        confirmButton = findViewById(R.id.confirmButton);

        registerButton.setOnClickListener(view -> {
            registerButton.setEnabled(false);
            handleRegister();
        });

        importButton.setOnClickListener(view -> {
            userSelection.setVisibility(View.GONE);
            userImport.setVisibility(View.VISIBLE);
        });

        importActionButton.setOnClickListener(view -> {
            String[] secrets = new String[4];
            secrets[0] = String.valueOf(secretInput1.getText());
            secrets[1] = String.valueOf(secretInput2.getText());
            secrets[2] = String.valueOf(secretInput3.getText());
            secrets[3] = String.valueOf(secretInput4.getText());
            handleImport(secrets);
        });

        confirmButton.setOnClickListener(view -> {
            Intent intent = new Intent(UserActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("a", "a");

            Map<String, String> headers = new HashMap<>();
            headers.put("App-Version", BuildConfig.VERSION_NAME);
            headers.put("Device-ID", Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID));

            ApiFetcher.postRequest(
                    "http://10.0.2.2:3000/users/register",
                    requestBody,
                    headers,
                    new ApiFetcher.ApiCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            runOnUiThread(() -> {
                                registerButton.setEnabled(true);
                                try {
                                    JSONObject data = result.getJSONObject("data");
                                    String token = data.getString("token");
                                    String userId = data.getString("userId");

                                    Log.d("DATA", data.getString("secretWords"));
                                    String[] secrets = data.getString("secretWords").substring(1,
                                            data.getString("secretWords").
                                                    length() - 1).replace("\"",
                                            "").split(",");
                                    secret1.setText("1. " + secrets[0]);
                                    secret2.setText("2. " + secrets[1]);
                                    secret3.setText("3. " + secrets[2]);
                                    secret4.setText("4. " + secrets[3]);

                                    SharedPreferences prefs = getSharedPreferences(
                                            "AppPrefs", MODE_PRIVATE);
                                    prefs.edit()
                                            .putString("authToken", token)
                                            .putString("userId", userId)
                                            .apply();

                                    userSelection.setVisibility(View.GONE);
                                    userRegistration.setVisibility(View.VISIBLE);

                                    Toast.makeText(UserActivity.this,
                                            "Registration successful!", Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    Log.e("API_ERROR", "JSON parsing error", e);
                                    Toast.makeText(UserActivity.this,
                                            "Invalid server response", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage, int statusCode) {
                            runOnUiThread(() -> {
                                registerButton.setEnabled(true);
                                Log.e("API_ERROR", "Status: " + statusCode + ", Error: " + errorMessage);
                                Toast.makeText(UserActivity.this,
                                        "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    });

        } catch (JSONException e) {
            registerButton.setEnabled(true);
            Toast.makeText(UserActivity.this,
                    "Invalid input data", Toast.LENGTH_SHORT).show();
            Log.e("API_ERROR", "JSON creation error", e);
        }
    }

    private void handleImport(String[] secrets) {
        for (String secret : secrets) {
            if (secret == null || secret.trim().isEmpty()) {
                Toast.makeText(this, "All secret words must be filled", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            JSONObject requestBody = new JSONObject();
            JSONArray secretsArray = new JSONArray();

            for (String secret : secrets) {
                secretsArray.put(secret.trim());
            }

            requestBody.put("secrets", secretsArray);

            Map<String, String> headers = new HashMap<>();
            headers.put("App-Version", BuildConfig.VERSION_NAME);
            headers.put("Device-ID", Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID));
            headers.put("Content-Type", "application/json");

            ApiFetcher.postRequest(
                    "http://10.0.2.2:3000/users/login",
                    requestBody,
                    headers,
                    new ApiFetcher.ApiCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            runOnUiThread(() -> {
                                try {
                                    JSONObject data = result.getJSONObject("data");
                                    String token = data.getString("token");
                                    String userId = data.getString("userId");

                                    SharedPreferences prefs = getSharedPreferences(
                                            "AppPrefs", MODE_PRIVATE);
                                    prefs.edit()
                                            .putString("authToken", token)
                                            .putString("userId", userId)
                                            .apply();

                                    Toast.makeText(UserActivity.this,
                                            "Import successful!", Toast.LENGTH_SHORT).show();
                                    Log.e("TOKEN", token);

                                    Intent intent = new Intent(UserActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();

                                } catch (JSONException e) {
                                    Log.e("API_ERROR", "JSON parsing error", e);
                                    Toast.makeText(UserActivity.this,
                                            "Invalid server response", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage, int statusCode) {
                            runOnUiThread(() -> {
                                Log.e("API_ERROR", "Status: " + statusCode + ", Error: " + errorMessage);
                                Toast.makeText(UserActivity.this,
                                        "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    });

        } catch (JSONException e) {
            Toast.makeText(UserActivity.this,
                    "Invalid input data", Toast.LENGTH_SHORT).show();
            Log.e("API_ERROR", "JSON creation error", e);
        }
    }
}