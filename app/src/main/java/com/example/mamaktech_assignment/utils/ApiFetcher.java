package com.example.mamaktech_assignment.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiFetcher {
    private static final String TAG = "ApiFetcher";
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 20000;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage, int statusCode);
    }

    public static <T> void postRequest(String url, JSONObject requestBody,
                                       Map<String, String> headers,
                                       ApiCallback<T> callback) {
        new AsyncTask<Void, Void, ApiResponse>() {
            @Override
            protected ApiResponse doInBackground(Void... voids) {
                HttpURLConnection connection = null;
                try {
                    URL apiUrl = new URL(url);
                    connection = (HttpURLConnection) apiUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(CONNECT_TIMEOUT);
                    connection.setReadTimeout(READ_TIMEOUT);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    // Set headers
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    if (headers != null) {
                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                            connection.setRequestProperty(entry.getKey(), entry.getValue());
                        }
                    }

                    // Write request body
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    // Get response
                    int responseCode = connection.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        return new ApiResponse(response.toString(), responseCode);
                    } else {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getErrorStream()));
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        reader.close();
                        return new ApiResponse(errorResponse.toString(), responseCode);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Request failed", e);
                    return new ApiResponse(e.getMessage(), -1);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(ApiResponse response) {
                if (response.statusCode >= 200 && response.statusCode < 300) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body);
                        callback.onSuccess((T) jsonResponse);
                    } catch (Exception e) {
                        callback.onError("Invalid JSON response", response.statusCode);
                    }
                } else {
                    callback.onError(response.body, response.statusCode);
                }
            }
        }.execute();
    }

    private static class ApiResponse {
        String body;
        int statusCode;

        ApiResponse(String body, int statusCode) {
            this.body = body;
            this.statusCode = statusCode;
        }
    }
}