package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class GamePage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_page);

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
                                List<String> songs = (List<String>) document.get("songs");
                                if (songs != null && !songs.isEmpty()) {
                                    populateTopSongs(songs);
                                }
                            }
                        } else {
                        }
                    });

            // Populate top genres
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                List<String> genres = (List<String>) document.get("genres");
                                if (genres != null && !genres.isEmpty()) {
                                    populateTopGenres(genres);
                                }
                            }
                        } else {
                            // Handle errors
                        }
                    });

            // Populate top artists
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

            Button submitButton = findViewById(R.id.submitButton);
            submitButton.setOnClickListener(view -> {
                checkAnswer(R.id.answer1RadioButton, "topSong");
            });

            Button submitButton2 = findViewById(R.id.submitButton2);
            submitButton2.setOnClickListener(view -> {
                checkAnswer(R.id.answer1RadioButton2, "topArtist");
            });

            Button submitButton3 = findViewById(R.id.submitButton3);
            submitButton3.setOnClickListener(view -> {
                checkAnswer(R.id.answer1RadioButton3, "topGenre");
                startActivity(new Intent(GamePage.this, IntroActivity.class));
            });
        }
    }

    private void populateTopSongs(List<String> songs) {
        RadioGroup answersRadioGroup = findViewById(R.id.answersRadioGroup);
        populateRadioGroup(answersRadioGroup, songs);
    }

    private void populateTopGenres(List<String> genres) {
        RadioGroup answersRadioGroup = findViewById(R.id.answersRadioGroup2);
        populateRadioGroup(answersRadioGroup, genres);
    }

    private void populateTopArtists(List<String> artists) {
        RadioGroup answersRadioGroup = findViewById(R.id.answersRadioGroup3);
        populateRadioGroup(answersRadioGroup, artists);
    }

    private void populateRadioGroup(RadioGroup radioGroup, List<String> items) {
        for (int i = 0; i < Math.min(items.size(), 4); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(items.get(i));
            radioGroup.addView(radioButton);
        }
    }

    private void checkAnswer(int radioButtonId, String category) {
        RadioButton radioButton = findViewById(radioButtonId);
        if (radioButton != null && radioButton.isChecked()) {
            if (category.equals("topSong") && radioButtonId == R.id.answer1RadioButton) {
                score++;
            } else if (category.equals("topArtist") && radioButtonId == R.id.answer1RadioButton2) {
                score++;
            } else if (category.equals("topGenre") && radioButtonId == R.id.answer1RadioButton3) {
                score++;
            }
            Toast.makeText(this, "Score: " + score, Toast.LENGTH_SHORT).show();
        }
    }
}
