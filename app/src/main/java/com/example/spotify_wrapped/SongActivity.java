package com.example.spotify_wrapped;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SongActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView songTextView;
    private ImageView imageViewSetting;
    private ImageView imageViewHome;
    private ImageView exportButton;

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
                String prompt  = "Please generate a short sentence describing user's music taste and personality, using second-person point of view,  based on this list of songs: " + String.join(", ", wrapData.getTrackList());
                generateGeminiText(prompt);
            } else {
                db.collection("users").document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    List<String> artists = (List<String>) document.get("songs");
                                    if (artists != null && !artists.isEmpty()) {
                                        populateTopSongs(artists);
                                        String prompt  = "Please generate a short sentence describing user's music taste and personality, using second-person point of view,  based on this list of songs: " + String.join(", ", artists);
                                        generateGeminiText(prompt);
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
                startActivity(new Intent(SongActivity.this, HomePage.class));
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
        Intent intent = new Intent(SongActivity.this, ArtistActivity.class);
        intent.putExtra(WrapData.WRAP_DATA_KEY, wrapData);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void generateGeminiText(String inputText) {
        GenerativeModel gm = new GenerativeModel("gemini-pro", BuildConfig.apikey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(inputText)
                .build();

        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String generatedText = result.getText();
                runOnUiThread(() -> {
                    TextView generatedTextView = findViewById(R.id.generatedTextView);
                    generatedTextView.setText(generatedText);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, executor);
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