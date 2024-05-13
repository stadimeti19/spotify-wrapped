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

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
public class GenreSubActivity extends AppCompatActivity {
    private TextView genreTextView;
    private ImageView imageViewSetting;
    private ImageView imageViewHome;
    private ImageView imageViewGenre;
    private ImageView exportButton;
    private GestureDetector gestureDetector;
    private static final String TAG = "GenreSubActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WrapData wrapData = getIntent().getParcelableExtra(WrapData.WRAP_DATA_KEY);
        List<String> genres = getIntent().getStringArrayListExtra("genresList");
        String timeRange = getIntent().getStringExtra("timeRange");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genre_sub_page);

        genreTextView = findViewById(R.id.textView2);
        imageViewSetting = findViewById(R.id.settings_button);
        imageViewHome = findViewById(R.id.home_button);
        imageViewGenre = findViewById(R.id.imageView);
        exportButton = findViewById(R.id.export_button);
        populateTopGenres(genres);
        String prompt = "Please generate one sentence (25 word limit) describing user's music taste and personality, and how someone who listens to this kind of music tends to act/think/dress, using second-person point of view,  based on this list of genres: " + String.join(", ", genres);
        generateGeminiText(prompt);

        gestureDetector = new GestureDetector(this, new GenreSubActivity.SwipeGestureListener());

        View rootLayout = findViewById(android.R.id.content);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                navigateToNextActivity();
                return true;
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (wrapData != null) {
                if (wrapData.getGenreImageUrl() != null) {
                    Picasso.get().load(wrapData.getGenreImageUrl()).into(imageViewGenre);
                } else {
                    Log.e(TAG, "The genreImageURL is null!");
                }
            } else {
                String userId = currentUser.getUid();
                db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String genreImageURL;
                                if (timeRange != null) {
                                    if (timeRange.equals("Monthly")) {
                                        genreImageURL = documentSnapshot.getString("short_genre_image");
                                    } else if (timeRange.equals("Biyearly")) {
                                        genreImageURL = documentSnapshot.getString("genreImageUrl");
                                    } else {
                                        genreImageURL = documentSnapshot.getString("long_genre_image");
                                    }
                                } else {
                                    genreImageURL = documentSnapshot.getString("genreImageUrl");
                                }
                                Log.e(TAG, "Successfully fetched genre image" + genreImageURL);
                                // Load the image into the imageView using Picasso
                                Picasso.get().load(genreImageURL).into(imageViewGenre); // Change to the appropriate imageView
                            } else {
                                Log.d(TAG, "Document does not exist");
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error fetching genre image URL", e));
            }
        }
//        imageViewGenre.setVisibility(View.INVISIBLE);
//        fetchStabilityImage("Generate an image for the music genre" + genres.get(0));
//        imageViewGenre.setVisibility(View.VISIBLE);

        // Set onClickListeners
        imageViewSetting.setOnClickListener(v -> startActivity(new Intent(GenreSubActivity.this, SettingsPage.class)));
        imageViewHome.setOnClickListener(v -> startActivity(new Intent(GenreSubActivity.this, startActivity.class)));
        exportButton.setOnClickListener(v -> {
            imageViewHome.setVisibility(View.GONE);
            imageViewSetting.setVisibility(View.GONE);
            exportButton.setVisibility(View.GONE);
            captureAndExportImage();
            imageViewHome.setVisibility(View.VISIBLE);
            imageViewSetting.setVisibility(View.VISIBLE);
            exportButton.setVisibility(View.VISIBLE);
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
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "genre_image");
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
    private void populateTopGenres(List<String> genres) {
        if (genres != null) {
            StringBuilder topGenres = new StringBuilder();
            if (genres.size() >= 5) {
                for (int i = 0; i < 5; ++i) {
                    topGenres.append(genres.get(i));
                }
            } else {
                for (int i = 0; i < genres.size(); ++i) {
                    topGenres.append(genres.get(i));
                }
            }
            genreTextView.setText(topGenres.toString());
        }
    }
    private void navigateToNextActivity() {
        stopService(new Intent(this, MusicService.class));
        Log.e(TAG, "Stopped MusicService");
        Intent intent = new Intent(GenreSubActivity.this, startActivity.class);
        startActivity(intent);
        finish();
    }
    private void navigateToIntroActivity() {
        stopService(new Intent(this, MusicService.class));
        Intent intent = new Intent(GenreSubActivity.this, IntroActivity.class);
        startActivity(intent);
    }
}

