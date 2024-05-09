package com.example.spotify_wrapped;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private List<String> trackList;
    private List<String> trackListUrls;
    private List<String> artistList;
    private List<String> genreList;
    private String trackImageUrl;
    private String artistImageUrl;
    private String genreImageUrl;


    private String timeRange;
    private static final String TAG = "PastWrapsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.past_wraps);

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
            timeRange = startActivity.getSelectedTimePeriod();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference wrapsCollection = db.collection("wraps").document(userId).collection("dates");

            wrapsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    // Accessing the data inside each document
                    String date = document.getId(); // Get the date from the document ID
                    if (timeRange != null) {
                        if(timeRange.equals("Monthly")) {
                            trackList = (List<String>) document.get("shortTrackList");
                            trackListUrls = (List<String>) document.get("shortTrackListUrls");
                            artistList = (List<String>) document.get("shortArtistList");
                            genreList = (List<String>) document.get("shortTopGenres");
                            trackImageUrl = (String) document.get("shortTrackImageUrl");
                            artistImageUrl = (String) document.get("shortArtistImageUrl");
                            genreImageUrl = (String) document.get("shortGenreImageUrl");
                        } else if (timeRange.equals("Biyearly")) {
                            trackList = (List<String>) document.get("trackList");
                            trackListUrls = (List<String>) document.get("trackListUrls");
                            artistList = (List<String>) document.get("artistList");
                            genreList = (List<String>) document.get("topGenres");
                            trackImageUrl = (String) document.get("trackImageUrl");
                            artistImageUrl = (String) document.get("artistImageUrl");
                            genreImageUrl = (String) document.get("genreImageUrl");
                        } else {
                            trackList = (List<String>) document.get("longTrackList");
                            trackListUrls = (List<String>) document.get("longTrackListUrls");
                            artistList = (List<String>) document.get("longArtistList");
                            genreList = (List<String>) document.get("longTopGenres");
                            trackImageUrl = (String) document.get("longTrackImageUrl");
                            artistImageUrl = (String) document.get("longArtistImageUrl");
                            genreImageUrl = (String) document.get("longGenreImageUrl");
                        }
                    } else {
                        trackList = (List<String>) document.get("trackList");
                        trackListUrls = (List<String>) document.get("trackListUrls");
                        artistList = (List<String>) document.get("artistList");
                        genreList = (List<String>) document.get("topGenres");
                        trackImageUrl = (String) document.get("trackImageUrl");
                        artistImageUrl = (String) document.get("artistImageUrl");
                        genreImageUrl = (String) document.get("genreImageUrl");
                    }

                    // Create a WrapData object with the retrieved data
                    if (trackList != null) {
                        WrapData wrapData = new WrapData(date, trackList, trackListUrls,
                                trackImageUrl, artistList, artistImageUrl, genreList, genreImageUrl);
                        // Add the WrapData object to the list
                        wrapDataList.add(wrapData);
                    } else {
                        trackList = (List<String>) document.get("trackList");
                        trackListUrls = (List<String>) document.get("trackListUrls");
                        artistList = (List<String>) document.get("artistList");
                        genreList = (List<String>) document.get("topGenres");
                        trackImageUrl = (String) document.get("trackImageUrl");
                        artistImageUrl = (String) document.get("artistImageUrl");
                        genreImageUrl = (String) document.get("genreImageUrl");

                        WrapData wrapData = new WrapData(date, trackList, trackListUrls,
                                trackImageUrl, artistList, artistImageUrl, genreList, genreImageUrl);
                        // Add the WrapData object to the list
                        wrapDataList.add(wrapData);
                    }
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
