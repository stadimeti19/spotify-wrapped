package com.example.spotify_wrapped;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SongActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView songTextView;
    private ImageView imageViewSetting;
    private ImageView imageViewHome;
    private ImageView exportButton;
    private List<String> songs;
    public String timeRange;
    private MediaPlayer mediaPlayer;
    private Intent musicServiceIntent;
    private String songUrl;
    private List <String> trackListUrls;

    private static final String TAG = "SongActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_page);
        WrapData wrapData = getIntent().getParcelableExtra(WrapData.WRAP_DATA_KEY);

        // Initialize views
        songTextView = findViewById(R.id.textView2);
        imageViewSetting = findViewById(R.id.settings_button);
        imageViewHome = findViewById(R.id.home_button);
        exportButton = findViewById(R.id.export_button);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db = FirebaseFirestore.getInstance();
            String userId = user.getUid();
            timeRange = startActivity.getSelectedTimePeriod();
            if (wrapData != null) {
                Log.e(TAG, "successfully got wrapData songs");
                populateTopSongs(wrapData.getTrackList());
            } else {
                db.collection("users").document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    if(timeRange != null) {
                                        if(timeRange.equals("Monthly")) {
                                            songs = (List<String>) document.get("short_term_songs");
                                            songUrl = "shortTrackListUrls";
                                        } else if (timeRange.equals("Biyearly")) {
                                            songs = (List<String>) document.get("songs");
                                            songUrl = "trackListUrls";
                                        } else {
                                            songs = (List<String>) document.get("long_term_songs");
                                            songUrl = "longTrackListUrls";
                                        }
                                    } else {
                                        songs = (List<String>) document.get("songs");
                                        songUrl = "trackListUrls";
                                    }

                                    if (songs != null && !songs.isEmpty()) {
                                        populateTopSongs(songs);
                                        playSongFromFirebase(songUrl);
                                    }
                                }
                            } else {
                                // Handle errors
                            }
                        });
            }
        }

        // Set up gesture detector for left swipes
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        // Set up touch listener on the layout to detect screen tap
        View rootLayout = findViewById(android.R.id.content);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                navigateToNextActivity(wrapData);
                return true;
            }
        });

        // Set onClickListeners
        imageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SongActivity.this, SettingsPage.class));
            }
        });
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SongActivity.this, startActivity.class));
            }
        });
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewHome.setVisibility(View.GONE);
                imageViewSetting.setVisibility(View.GONE);
                exportButton.setVisibility(View.GONE);
                captureAndExportImage();
                imageViewHome.setVisibility(View.VISIBLE);
                imageViewSetting.setVisibility(View.VISIBLE);
                exportButton.setVisibility(View.VISIBLE);
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

    private void navigateToNextActivity(WrapData wrapData) {
        Intent intent = new Intent(SongActivity.this, SongSubActivity.class);
        intent.putExtra(WrapData.WRAP_DATA_KEY, wrapData);
        if (wrapData == null) {
            intent.putStringArrayListExtra("songsList", (ArrayList<String>) songs);
        } else {
            intent.putStringArrayListExtra("songsList", (ArrayList<String>) wrapData.getTrackList());
        }
        intent.putExtra("timeRange", timeRange);
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
        if (songs.size() > 0) {
            songTextView.setText(songs.get(0));
        }
    }

    private void navigateToIntroActivity() {
        Intent intent = new Intent(SongActivity.this, IntroActivity.class);
        startActivity(intent);
    }

    private void startMusicService() {
        // Start MusicService using an Intent
        musicServiceIntent = new Intent(this, MusicService.class);
        musicServiceIntent.putStringArrayListExtra("trackListUrls", (ArrayList<String>) trackListUrls);
        startService(musicServiceIntent);
    }
    private void playSongFromFirebase(String urlType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            // Retrieve the URL from Firebase Firestore
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    trackListUrls = (List<String>) documentSnapshot.get(urlType);
                    if (trackListUrls != null) {
                        startMusicService();
                        // Create a MediaPlayer instance
//                        mediaPlayer = new MediaPlayer();
//                        try {
//                            // Set the data source to the retrieved URL
//                            mediaPlayer.setDataSource(url);
//                            // Prepare the MediaPlayer asynchronously
//                            mediaPlayer.prepareAsync();
//                            // Set a listener to start playback once preparation is complete
//                            mediaPlayer.setOnPreparedListener(mp -> {
//                                // Start playback
//                                mp.start();
//                            });
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    } else {
                        Log.e(TAG, "URL is null for the specified ID");
                    }
                } else {
                    Log.e(TAG, "Document does not exist");
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error retrieving document from Firestore", e);
            });
        } else {
            Log.e(TAG, "Current user is null");
        }
    }
}