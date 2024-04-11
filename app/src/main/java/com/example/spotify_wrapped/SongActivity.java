package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SongActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.songs_sub_page);
//
//        // Set up the text views
//        TextView topAlbumsTextView = findViewById(R.id.textView1);
//        topAlbumsTextView.setText("Top Albums");
//
//        TextView album1TextView = findViewById(R.id.textView2);
//        album1TextView.setText("1. Album 1");
//
//        TextView album2TextView = findViewById(R.id.textView3);
//        album2TextView.setText("2. Album 2");
//
//        TextView album3TextView = findViewById(R.id.textView4);
//        album3TextView.setText("3. Album 3");
//
//        TextView album4TextView = findViewById(R.id.textView5);
//        album4TextView.setText("4. Album 4");
//
//        TextView album5TextView = findViewById(R.id.textView6);
//        album5TextView.setText("5. Album 5");
//
//        // Set up the image view
//        ImageView drakeImageView = findViewById(R.id.imageView);
//        drakeImageView.setImageResource(R.drawable.drake);
//
//        // Set up gesture detector for left swipes
//        gestureDetector = new GestureDetector(this, new SwipeGestureListener());
//
//        // Set up touch listener on the layout to detect screen tap
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
        setContentView(R.layout.song_page);

        // Set up the text views
        TextView songTitleTextView = findViewById(R.id.textView2);
        songTitleTextView.setText("Love Story!"); // Set the song title text

        // Set up gesture detector for left swipes
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        // Set up touch listener on the layout to detect screen tap
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
        Intent intent = new Intent(SongActivity.this, ArtistActivity.class);
        startActivity(intent);
        finish(); // Finish SongActivity to prevent going back to it on back press
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY) &&
                        Math.abs(diffX) > SWIPE_THRESHOLD &&
                        Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // Swipe right (not implemented)
                    } else {
                        // Swipe left
                        navigateToIntroActivity();
                    }
                    result = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    private void navigateToIntroActivity() {
        Intent intent = new Intent(SongActivity.this, IntroActivity.class);
        startActivity(intent);
    }
}
