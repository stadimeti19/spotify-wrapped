package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    private TextView welcomeText;
    private TextView tapToContinueText;
    private ImageView imageViewSetting;

    private ImageView imageViewHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_page);
        int score = getIntent().getIntExtra("score", 0);

        welcomeText = findViewById(R.id.welcomeText);
        tapToContinueText = findViewById(R.id.tapToContinueText);
        welcomeText.setText("Welcome, sashankt19, you got " + score + "!");


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
                startActivity(new Intent(IntroActivity.this, HomePage.class));
            }
        });
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(IntroActivity.this, SongActivity.class);
        startActivity(intent);
    }
}
