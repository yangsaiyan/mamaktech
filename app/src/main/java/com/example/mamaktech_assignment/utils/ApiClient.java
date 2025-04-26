package com.example.mamaktech_assignment.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    private static final String BASE_URL = "https://notedown-backend-514961d738c0.herokuapp.com/notes/";
    private final RequestQueue requestQueue;

    public ApiClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface ApiResponseListener {
        void onSuccess(JSONObject response);
        void onSuccess(JSONArray response);  // Add this new method
        void onError(String errorMessage);
    }

    public void uploadAllNotes(String authToken, JSONArray notesArray, ApiResponseListener listener) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.POST,
                BASE_URL + "uploadAllNotes",
                notesArray,
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
                Request.Method.GET,
                BASE_URL + "getAllNotes",
                null,
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
}