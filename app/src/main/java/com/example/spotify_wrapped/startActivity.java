package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class startActivity extends AppCompatActivity {
    private Spinner timePeriodSpinner;
    private Button letsGoButton;

    private Button viewPastWrapsButton;

    private ImageView imageViewSetting;

    private ImageView imageViewHome;
    private String accessToken = "";
    private static String selectedTimePeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);

        timePeriodSpinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_ranges, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timePeriodSpinner.setAdapter(adapter);
        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimePeriod = parent.getItemAtPosition(position).toString();
                // Handle the selected time period, e.g., store it in a variable or perform an action
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle nothing selected event if needed
            }
        });

        letsGoButton = findViewById(R.id.button);
        viewPastWrapsButton = findViewById(R.id.buttonViewPastWraps);
        letsGoButton.setOnClickListener(v -> navigateToGameClass());
        imageViewSetting = findViewById(R.id.settings_button);
        imageViewSetting.setOnClickListener(v -> startActivity(new Intent(startActivity.this, SettingsPage.class)));
        imageViewHome = findViewById(R.id.home_button);
        imageViewHome.setOnClickListener(v -> startActivity(new Intent(startActivity.this, HomePage.class)));
        viewPastWrapsButton.setOnClickListener(v -> startActivity(new Intent(startActivity.this, PastWrapsActivity.class)));
        accessToken = getIntent().getStringExtra("accessToken");
    }
    private void navigateToGameClass() {
        Intent intent = new Intent(startActivity.this, GamePage.class);
        intent.putExtra("accessToken", accessToken);
        startActivity(intent);
    }

    public static String getSelectedTimePeriod() {
        return selectedTimePeriod;
    }
}
