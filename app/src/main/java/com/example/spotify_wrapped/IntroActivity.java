package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class IntroActivity extends AppCompatActivity {
    private TextView welcomeText;
    private TextView tapToContinueText;
    private ImageView imageViewSetting;

    private ImageView imageViewHome;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String accessToken;

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
                                    if (username != null) {
                                        welcomeText.setText("Welcome, " + username + ", you got " + score + "!");
                                    }
                                }
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
