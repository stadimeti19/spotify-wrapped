package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ArtistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.artist_sub_page);
//
//        // Set up the text views
//        TextView topArtistsTextView = findViewById(R.id.textView1);
//        topArtistsTextView.setText("Top Artists");
//
//        TextView artist1TextView = findViewById(R.id.textView2);
//        artist1TextView.setText("1. Snoop Dogg");
//
//        TextView artist2TextView = findViewById(R.id.textView3);
//        artist2TextView.setText("2. Eminem");
//
//        TextView artist3TextView = findViewById(R.id.textView4);
//        artist3TextView.setText("3. Shawn Mendes");
//
//        TextView artist4TextView = findViewById(R.id.textView5);
//        artist4TextView.setText("4. Drake");
//
//        TextView artist5TextView = findViewById(R.id.textView6);
//        artist5TextView.setText("5. Taylor Swift");
//
//        // Set up the image view
//        ImageView drakeImageView = findViewById(R.id.imageView);
//        drakeImageView.setImageResource(R.drawable.drake);
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
        setContentView(R.layout.artist_page);

        // Set up the text views
        TextView artistTextView = findViewById(R.id.textView2);
        artistTextView.setText("1. Drake\n2. Taylor Swift\n3. Eminem"); // Set the song title text

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
        Intent intent = new Intent(ArtistActivity.this, GenreActivity.class);
        startActivity(intent);
        finish(); // Finish ArtistActivity to prevent going back to it on back press
    }
}