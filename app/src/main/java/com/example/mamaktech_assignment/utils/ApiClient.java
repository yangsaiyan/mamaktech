package com.example.mamaktech_assignment.utils;

import android.content.Context;
import android.provider.Settings;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.intuit.sdp.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    private static final String BASE_URL = "https://notedown-backend-514961d738c0.herokuapp.com/";
    private static final String NOTES_ENDPOINT = BASE_URL + "notes/";
    private static final String USERS_ENDPOINT = BASE_URL + "users/";
    private final RequestQueue requestQueue;
    private final Context context;

    public ApiClient(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface ApiResponseListener {
        void onSuccess(JSONObject response);
        void onSuccess(JSONArray response);
        void onError(String errorMessage);
    }

    public void uploadAllNotes(String authToken, JSONArray notesArray, ApiResponseListener listener) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.POST, NOTES_ENDPOINT + "uploadAllNotes", notesArray,
                response -> listener.onSuccess(response),
                error -> listener.onError(error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", authToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void getAllNotes(String authToken, ApiResponseListener listener) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, NOTES_ENDPOINT + "getAllNotes", null,
                response -> listener.onSuccess(response),
                error -> listener.onError(error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", authToken);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void registerUser(JSONObject requestBody, ApiResponseListener listener) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, USERS_ENDPOINT + "register", requestBody,
                response -> listener.onSuccess(response),
                error -> listener.onError(error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("App-Version", BuildConfig.VERSION_NAME);
                headers.put("Device-ID", Settings.Secure.getString(
                        context.getContentResolver(), Settings.Secure.ANDROID_ID));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void loginUser(JSONObject requestBody, ApiResponseListener listener) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, USERS_ENDPOINT + "login", requestBody,
                response -> listener.onSuccess(response),
                error -> listener.onError(error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("App-Version", BuildConfig.VERSION_NAME);
                headers.put("Device-ID", Settings.Secure.getString(
                        context.getContentResolver(), Settings.Secure.ANDROID_ID));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }
}