package com.example.spotify_wrapped;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

            // Now you can use the wrapData object as needed
            if (wrapData != null) {
                Log.e(TAG, "successfully got wrapData songs");
                populateTopSongs(wrapData.getTrackList());
            } else {
                db.collection("users").document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    timeRange = startActivity.getSelectedTimePeriod();
                                    if(timeRange.equals("Monthly")) {
                                        songs = (List<String>) document.get("short_term_songs");
                                    } else if (timeRange.equals("Biyearly")) {
                                        songs = (List<String>) document.get("songs");
                                    } else {
                                        songs = (List<String>) document.get("long_term_songs");
                                    }
                                    if (songs != null && !songs.isEmpty()) {
                                        populateTopSongs(songs);
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
            intent.putStringArrayListExtra("artistsList", (ArrayList<String>) songs);
        } else {
            intent.putStringArrayListExtra("artistsList", (ArrayList<String>) wrapData.getTrackList());
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
        songTextView.setText(songs.get(0));
    }

    private void navigateToIntroActivity() {
        Intent intent = new Intent(SongActivity.this, IntroActivity.class);
        startActivity(intent);
    }
}