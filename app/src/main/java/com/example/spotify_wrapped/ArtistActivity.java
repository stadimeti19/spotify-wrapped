package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ArtistActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView artistTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_page);

        artistTextView = findViewById(R.id.textView2);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db = FirebaseFirestore.getInstance();
            String userId = user.getUid();

            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                List<String> artists = (List<String>) document.get("artists");
                                if (artists != null && !artists.isEmpty()) {
                                    populateTopArtists(artists);
                                }
                            }
                        } else {
                            // Handle errors
                        }
                    });
        }

        View rootLayout = findViewById(android.R.id.content);
        rootLayout.setOnTouchListener((v, event) -> {
            navigateToNextActivity();
            return true;
        });
    }

    private void populateTopArtists(List<String> artists) {
        if (artists.size() >= 3) {
            String topArtists = "1. " + artists.get(0) + "\n2. " + artists.get(1) + "\n3. " + artists.get(2);
            artistTextView.setText(topArtists);
        }
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(ArtistActivity.this, GenreActivity.class);
        startActivity(intent);
        finish(); // Finish ArtistActivity to prevent going back to it on back press
    }
}
