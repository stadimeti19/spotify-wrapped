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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GamePage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int score = 0;
    private Random random = new Random();

    private String correctSong;
    private String correctArtist;
    private String correctGenre;


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
                                    List<String> formattedSongs = removePrefix(songs);
                                    populateTopSongs(formattedSongs);
                                }
                            }
                        } else {
                            // Handle errors
                        }
                    });

            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                List<String> genres = (List<String>) document.get("genres");
                                if (genres != null && !genres.isEmpty()) {
                                    List<String> formattedGenres = removePrefix(genres);
                                    populateTopGenres(formattedGenres);
                                }
                            }
                        } else {
                            // Handle errors
                        }
                    });

            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                List<String> artists = (List<String>) document.get("artists");
                                if (artists != null && !artists.isEmpty()) {
                                    List<String> formattedArtists = removePrefix(artists);
                                    populateTopArtists(formattedArtists);
                                }
                            }
                        } else {
                            // Handle errors
                        }
                    });

            Button submitButton = findViewById(R.id.submitButton);
            submitButton.setOnClickListener(view -> {
                RadioGroup radioGroup = findViewById(R.id.answersRadioGroup);
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                if(checkedRadioButtonId != -1) {
                    checkAnswer(checkedRadioButtonId, correctSong);
                    submitButton.setEnabled(false);
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                }
            });

            Button submitButton2 = findViewById(R.id.submitButton2);
            submitButton2.setOnClickListener(view -> {
                RadioGroup radioGroup = findViewById(R.id.answersRadioGroup2);
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                if(checkedRadioButtonId != -1) {
                    checkAnswer(checkedRadioButtonId, correctGenre);
                    submitButton2.setEnabled(false);
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                }
            });

            Button submitButton3 = findViewById(R.id.submitButton3);
            submitButton3.setOnClickListener(view -> {
                RadioGroup radioGroup = findViewById(R.id.answersRadioGroup3);
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                if(checkedRadioButtonId != -1) {
                    checkAnswer(checkedRadioButtonId, correctArtist);
                    Intent intent = new Intent(GamePage.this, IntroActivity.class);
                    intent.putExtra("score", score); // Pass the score as an extra
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void populateTopSongs(List<String> songs) {
        RadioButton[] radioButtons = {
                findViewById(R.id.answer1RadioButton),
                findViewById(R.id.answer2RadioButton),
                findViewById(R.id.answer3RadioButton),
                findViewById(R.id.answer4RadioButton)
        };

        correctSong = songs.get(0);
        List<String> otherSongs = new ArrayList<>(songs.subList(1, songs.size()));
        Collections.shuffle(otherSongs);
        int correctIndex = random.nextInt(4);
        radioButtons[correctIndex].setText(songs.get(0));
        for (int i = 0, j = 0; i < 4; i++) {
            if (i != correctIndex) {
                radioButtons[i].setText(otherSongs.get(j++));
            }
        }
        shuffleRadioButtons(radioButtons);
    }

    private void populateTopGenres(List<String> genres) {
        RadioButton[] radioButtons = {
                findViewById(R.id.answer1RadioButton2),
                findViewById(R.id.answer2RadioButton2),
                findViewById(R.id.answer3RadioButton2),
                findViewById(R.id.answer4RadioButton2)
        };

        correctGenre = genres.get(0);
        List<String> otherGenres = new ArrayList<>(genres.subList(1, genres.size()));
        Collections.shuffle(otherGenres);
        int correctIndex = random.nextInt(4);
        radioButtons[correctIndex].setText(genres.get(0));
        for (int i = 0, j = 0; i < 4; i++) {
            if (i != correctIndex) {
                radioButtons[i].setText(otherGenres.get(j++));
            }
        }
        shuffleRadioButtons(radioButtons);
    }

    private void populateTopArtists(List<String> artists) {
        RadioButton[] radioButtons = {
                findViewById(R.id.answer1RadioButton3),
                findViewById(R.id.answer2RadioButton3),
                findViewById(R.id.answer3RadioButton3),
                findViewById(R.id.answer4RadioButton3)
        };

        correctArtist = artists.get(0);
        List<String> otherArtists = new ArrayList<>(artists.subList(1, artists.size()));
        Collections.shuffle(otherArtists);
        int correctIndex = random.nextInt(4);
        radioButtons[correctIndex].setText(artists.get(0));
        for (int i = 0, j = 0; i < 4; i++) {
            if (i != correctIndex) {
                radioButtons[i].setText(otherArtists.get(j++));
            }
        }
        shuffleRadioButtons(radioButtons);
    }

    private void shuffleRadioButtons(RadioButton[] radioButtons) {
        List<RadioButton> radioButtonList = Arrays.asList(radioButtons);
        Collections.shuffle(radioButtonList);
        for (int i = 0; i < 4; i++) {
            radioButtons[i] = radioButtonList.get(i);
        }
    }

    private void checkAnswer(int checkedRadioButtonId, String correctValue) {
        RadioButton selectedRadioButton = findViewById(checkedRadioButtonId);
        if (selectedRadioButton != null) {
            String selectedText = selectedRadioButton.getText().toString();
            if (selectedText.equals(correctValue)) {
                score++;
                Toast.makeText(this, "Correct! Score: " + score, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Wrong answer!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
        }
    }
    private List<String> removePrefix(List<String> strings) {
        List<String> formattedStrings = new ArrayList<>();
        for (String str : strings) {
            formattedStrings.add(str.replaceAll("^\\d+\\.\\s*", ""));
        }
        return formattedStrings;
    }
}
