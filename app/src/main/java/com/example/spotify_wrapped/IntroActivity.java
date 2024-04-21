package com.example.spotify_wrapped;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class IntroActivity extends AppCompatActivity {
    private TextView welcomeText;
    private TextView tapToContinueText;
    private ImageView imageViewSetting;

    private ImageView imageViewHome;
    private ImageView profileImage;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String accessToken;
    private String un;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_page);
        int score = getIntent().getIntExtra("score", 0);
        accessToken = getIntent().getStringExtra("accessToken");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeText = findViewById(R.id.welcomeText);
        tapToContinueText = findViewById(R.id.tapToContinueText);
        profileImage = findViewById(R.id.profile_picture);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String username = document.getString("username");
                                    un = username;
                                    if (username != null) {
                                        welcomeText.setText("Welcome, " + username + ", you got " + score + "!");
                                    }
                                    String profileImageUrl = document.getString("profileImageUrl");
                                    if (profileImageUrl != null) {
                                        Picasso.get().load(profileImageUrl).into(profileImage);
                                    }
                                }
//                                profileImage.setVisibility(View.INVISIBLE);
//                                fetchStabilityImage("Generate an avatar image for the user "+un+" that contains all the letters in the username");
//                                profileImage.setVisibility(View.VISIBLE);
                            } else {
                                // Handle failure
                            }
                        }
                    });
            // Fetch profile picture from Spotify API
//            String profilePictureUrl = getProfilePictureUrl(accessToken);
//            if (profilePictureUrl != null) {
//                Picasso.get().load(profilePictureUrl).into(imageViewHome);
//            }
        } else {
            // handle error
        }

        // Set up touch listener on the layout to detect screen tap
        View introLayout = findViewById(R.id.introLayout);
        introLayout.setOnTouchListener(new View.OnTouchListener() {
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
                startActivity(new Intent(IntroActivity.this, SettingsPage.class));
            }
        });
        imageViewHome = findViewById(R.id.home_button);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroActivity.this, startActivity.class));
            }
        });
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(IntroActivity.this, SongActivity.class);
        startActivity(intent);
    }
    private void fetchStabilityImage(String prompt) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.stability.ai/v2beta/stable-image/generate/core")
                .addHeader("Authorization", BuildConfig.stableapikey) // Replace MYAPIKEY with your actual API key
                .addHeader("accept", "image/*")
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("prompt", prompt)
                        .addFormDataPart("output_format", "png")
                        .build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("fetchStabilityImage", "Failed to fetch Stability image: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    byte[] imageData = response.body().bytes();
                    runOnUiThread(() -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        profileImage.setImageBitmap(bitmap);
                    });
                }
            }
        });
    }



//    private String getProfilePictureUrl(String accessToken) {
//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url("https://api.spotify.com/v1/me")
//                .addHeader("Authorization", "Bearer " + accessToken)
//                .build();
//
//        try {
//            Response response = client.newCall(request).execute();
//            if (response.isSuccessful()) {
//                String responseBody = response.body().string();
//                JSONObject jsonObject = new JSONObject(responseBody);
//                // Extract profile picture URL from the JSON response
//                return jsonObject.getJSONArray("images").getJSONObject(0).getString("url");
//            } else {
//                // Handle unsuccessful response
//            }
//        } catch (IOException | JSONException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
}
