package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GenreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.genre_page);
//
//        // Set up the text views
//        TextView topGenresTextView = findViewById(R.id.textView1);
//        topGenresTextView.setText("Top Genres");
//
//        TextView genre1TextView = findViewById(R.id.textView2);
//        genre1TextView.setText("1. Country");
//
//        TextView genre2TextView = findViewById(R.id.textView3);
//        genre2TextView.setText("2. Hip Hop");
//
//        TextView genre3TextView = findViewById(R.id.textView4);
//        genre3TextView.setText("3. Rock");
//
//        TextView genre4TextView = findViewById(R.id.textView5);
//        genre4TextView.setText("4. Electronic");
//
//        TextView genre5TextView = findViewById(R.id.textView6);
//        genre5TextView.setText("5. Jazz");
//
//
//        // Set up the image view
//        ImageView countryImageView = findViewById(R.id.imageView);
//        countryImageView.setImageResource(R.drawable.country);
//
//        View rootLayout = findViewById(android.R.id.content); // Get the root layout
//        rootLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // Navigate to the next activity on screen tap
//                navigateToNextActivity();
//                return true;
//            }
//        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genre_page);

        // Set up the text views
        TextView artistTextView = findViewById(R.id.textView2);
        artistTextView.setText("1. Country\n2. Pop\n3. Rap"); // Set the song title text

        View rootLayout = findViewById(android.R.id.content); // Get the root layout
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Navigate to the next activity on screen tap
                navigateToNextActivity();
                return true;
            }
        });
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(GenreActivity.this, startActivity.class); // Replace NextActivity with the actual name of your next activity
        startActivity(intent);
        finish(); // Finish GenreActivity to prevent going back to it on back press
    }
}