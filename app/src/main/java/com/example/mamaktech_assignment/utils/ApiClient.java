package com.example.mamaktech_assignment.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:3000/notes/";
    private final RequestQueue requestQueue;

    public ApiClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface ApiResponseListener {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }

    // Create or Update Note
    public void createOrUpdateNote(String token, JSONObject noteData, ApiResponseListener listener) {
        String url = BASE_URL + "uploadNote";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                noteData,
                response -> listener.onSuccess(response),
                error -> listener.onError(error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // Get Note
    public void getNote(String token, ApiResponseListener listener) {
        String url = BASE_URL + "getNote";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> listener.onSuccess(response),
                error -> listener.onError(error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // Delete Note
    public void deleteNote(String token, ApiResponseListener listener) {
        String url = BASE_URL; // Your delete endpoint

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> listener.onSuccess(response),
                error -> listener.onError(error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        requestQueue.add(request);
    }
}