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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ArtistActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView artistTextView;
    private ImageView imageViewSetting;
    private ImageView imageViewHome;

    private ImageView exportButton;

    private static final String TAG = "ArtistActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_page);
        WrapData wrapData = getIntent().getParcelableExtra(WrapData.WRAP_DATA_KEY);

        artistTextView = findViewById(R.id.textView2);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db = FirebaseFirestore.getInstance();
            String userId = user.getUid();

            if (wrapData != null) {
                Log.e(TAG, "successfully got wrapData artists");
                populateTopArtists(wrapData.getArtistList());
                String prompt  = "Please generate a short sentence describing user's music taste and personality, using second-person point of view,  based on this list of songs: " + String.join(", ", wrapData.getArtistList());
                generateGeminiText(prompt);
            }

            else {
                db.collection("users").document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    List<String> artists = (List<String>) document.get("artists");
                                    if (artists != null && !artists.isEmpty()) {
                                        populateTopArtists(artists);
                                        String prompt = "Please generate a short sentence describing the user's music taste and personality, using second-person point of view,  based on this list of genres of songs: " + String.join(", ", artists);
                                        generateGeminiText(prompt);
                                    }
                                }
                            } else {
                                // Handle errors
                            }
                        });
            }
        }

        View rootLayout = findViewById(android.R.id.content);
        rootLayout.setOnTouchListener((v, event) -> {
            navigateToNextActivity(wrapData);
            return true;
        });

        imageViewSetting = findViewById(R.id.settings_button);
        imageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ArtistActivity.this, SettingsPage.class));
            }
        });
        imageViewHome = findViewById(R.id.home_button);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ArtistActivity.this, startActivity.class));
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


    private void populateTopArtists(List<String> artists) {
        if (artists.size() >= 3) {
            String topArtists = artists.get(0) + "\n" + artists.get(1) + "\n" + artists.get(2);
            artistTextView.setText(topArtists);
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

    private void navigateToNextActivity(WrapData wrapData) {
        Intent intent = new Intent(ArtistActivity.this, GenreActivity.class);
        intent.putExtra(WrapData.WRAP_DATA_KEY, wrapData);
        startActivity(intent);
        finish(); // Finish ArtistActivity to prevent going back to it on back press
    }
}