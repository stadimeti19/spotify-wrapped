package com.example.spotify_wrapped;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class PastWrapsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewWraps;
    private WrapAdapter wrapAdapter;
    private List<WrapData> wrapDataList;
    private static final String TAG = "PastWrapsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_to_wrapped);

        Log.e(TAG, "Started PastWrapsActivity");
        recyclerViewWraps = findViewById(R.id.recyclerViewWraps);
        recyclerViewWraps.setLayoutManager(new LinearLayoutManager(this));

        // Initialize wrap data list
        wrapDataList = new ArrayList<>();
        Log.e(TAG, "Initialized Wrap data list");

        // Populate wrap data list with data from Firebase
        fetchWrapDataFromFirebase();
    }

    // Method to fetch data from Firebase and update wrapDataList
    private void fetchWrapDataFromFirebase() {
        Log.e(TAG, "In fetchWrapData from firebase");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Log.e(TAG, "User not null");
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference wrapsCollection = db.collection("wraps").document(userId).collection("dates");

            wrapsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    // Accessing the data inside each document
                    String date = document.getId(); // Get the date from the document ID
                    List<String> trackList = (List<String>) document.get("trackList");
                    List<String> artistList = (List<String>) document.get("artistList");
                    List<String> topGenres = (List<String>) document.get("topGenres");

                    // Create a WrapData object with the retrieved data
                    WrapData wrapData = new WrapData(date, trackList, artistList, topGenres);

                    // Add the WrapData object to the list
                    wrapDataList.add(wrapData);
                }

                // Create adapter and set it to the RecyclerView
                wrapAdapter = new WrapAdapter(PastWrapsActivity.this, wrapDataList);
                recyclerViewWraps.setAdapter(wrapAdapter);
            }).addOnFailureListener(e -> {
                // Handle failure
                Log.e(TAG, "Failed to fetch wrap data from Firebase: " + e.getMessage(), e);
                Toast.makeText(PastWrapsActivity.this, "Failed to fetch wrap data from Firebase", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "Current user is null");
            Toast.makeText(this, "Current user is null. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }
}
