package com.example.mamaktech_assignment.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamaktech_assignment.R;
import com.example.mamaktech_assignment.utils.ApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

            registerButton.setEnabled(false);

            ApiClient apiClient = new ApiClient(this);
            apiClient.registerUser(requestBody, new ApiClient.ApiResponseListener() {
                @Override
                public void onSuccess(JSONObject result) {
                    runOnUiThread(() -> {
                        registerButton.setEnabled(true);
                        try {
                            JSONObject data = result.getJSONObject("data");
                            String token = data.getString("token");
                            String userId = data.getString("userId");

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
                public void onSuccess(JSONArray response) {
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        registerButton.setEnabled(true);
                        Toast.makeText(UserActivity.this,
                                "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (JSONException e) {
            registerButton.setEnabled(true);
            Toast.makeText(UserActivity.this,
                    "Invalid input data", Toast.LENGTH_SHORT).show();
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

            ApiClient apiClient = new ApiClient(this);
            apiClient.loginUser(requestBody, new ApiClient.ApiResponseListener() {
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

                            Intent intent = new Intent(UserActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            Toast.makeText(UserActivity.this,
                                    "Invalid server response", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onSuccess(JSONArray response) {
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(UserActivity.this,
                                "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (JSONException e) {
            Toast.makeText(UserActivity.this,
                    "Invalid input data", Toast.LENGTH_SHORT).show();
        }
    }
}