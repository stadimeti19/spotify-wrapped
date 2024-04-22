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

public class ArtistActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView artistTextView;
    private ImageView imageViewSetting;
    private ImageView imageViewHome;

    private ImageView exportButton;
    private List<String> artists;
    public String timeRange;

    private static final String TAG = "ArtistActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_page);
        WrapData wrapData = getIntent().getParcelableExtra(WrapData.WRAP_DATA_KEY);

        artistTextView = findViewById(R.id.textView2);
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
                Log.e(TAG, "successfully got wrapData artists");
                populateTopArtists(wrapData.getArtistList());
            }

            else {
                db.collection("users").document(userId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    if (timeRange != null) {
                                        if(timeRange.equals("Monthly")) {
                                            artists = (List<String>) document.get("short_term_artists");
                                        } else if (timeRange.equals("Biyearly")) {
                                            artists = (List<String>) document.get("artists");
                                        } else {
                                            artists = (List<String>) document.get("long_term_artists");
                                        }
                                    } else {
                                        artists = (List<String>) document.get("artists");
                                    }
                                    if (artists != null && !artists.isEmpty()) {
                                        populateTopArtists(artists);
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

        imageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ArtistActivity.this, SettingsPage.class));
            }
        });
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ArtistActivity.this, startActivity.class));
            }
        });

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
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "artist_image");
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
        if (artists.size() > 0) {
            artistTextView.setText(artists.get(0));
        }
    }

    private void navigateToNextActivity(WrapData wrapData) {
        Intent intent = new Intent(ArtistActivity.this, ArtistSubActivity.class);
        intent.putExtra(WrapData.WRAP_DATA_KEY, wrapData);
        if (wrapData == null) {
            intent.putStringArrayListExtra("artistsList", (ArrayList<String>) artists);
        } else {
            intent.putStringArrayListExtra("artistsList", (ArrayList<String>) wrapData.getArtistList());
        }
        intent.putExtra("timeRange", timeRange);
        startActivity(intent);
        finish();
    }
}