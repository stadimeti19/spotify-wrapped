package com.example.spotify_wrapped;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class SongActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView songTextView;
    private ImageView imageViewSetting;

    private ImageView imageViewHome;
    private ImageView exportButton;

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
        songTextView = findViewById(R.id.textView2);

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
                                List<String> artists = (List<String>) document.get("songs");
                                if (artists != null && !artists.isEmpty()) {
                                    populateTopSongs(artists);
                                }
                            }
                        } else {
                            // Handle errors
                        }
                    });
        }
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
        imageViewSetting = findViewById(R.id.settings_button);
        imageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SongActivity.this, SettingsPage.class));
            }
        });
        imageViewHome = findViewById(R.id.home_button);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SongActivity.this, HomePage.class));
            }
        });
        exportButton = findViewById(R.id.export_button);

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the buttons before taking a screenshot
                imageViewHome.setVisibility(View.GONE);
                imageViewSetting.setVisibility(View.GONE);
                exportButton.setVisibility(View.GONE);

                // Capture and export the image
                captureAndExportImage();

                // Show the buttons again after exporting
                imageViewHome.setVisibility(View.VISIBLE);
                imageViewSetting.setVisibility(View.VISIBLE);
                exportButton.setVisibility((View.VISIBLE));
            }
        });
    }
    private void captureAndExportImage() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);
        saveBitmapToFile(bitmap);
    }

    private void saveBitmapToFile(Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "song_image");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        ContentResolver resolver = getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri != null) {
            try {
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(SongActivity.this, ArtistActivity.class);
        startActivity(intent);
        finish();
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
    private void populateTopSongs(List<String> songs) {
        if (songs.size() >= 3) {
            String topArtists = songs.get(0) + "\n" + songs.get(1) + "\n" + songs.get(2);
            songTextView.setText(topArtists);
        }
    }

    private void navigateToIntroActivity() {
        Intent intent = new Intent(SongActivity.this, IntroActivity.class);
        startActivity(intent);
    }
}
