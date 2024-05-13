package com.example.spotify_wrapped;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WrappedActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MaterialCardView mCardView;
    private TextView mTextViewTopArtist;
    private ImageView mImageViewTopArtist;
    private TextView mTextViewMostPlayedSongs;
    private TextView mTextViewSongRecommendation;

    private TextView mTextViewSongRecommendationName;
    private ImageView mImageViewSongRecommendation;
    private Button mButtonExportImage;
    private SeekBar mSeekBarTimeSpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrapped_main);

        mCardView = findViewById(R.id.cardView);
        mTextViewTopArtist = findViewById(R.id.textViewTopArtist);
        mImageViewTopArtist = findViewById(R.id.imageViewTopArtist);
        mTextViewMostPlayedSongs = findViewById(R.id.textViewMostPlayedSongs);
        mTextViewSongRecommendation = findViewById(R.id.textViewSongRecommendation);
        mImageViewSongRecommendation = findViewById(R.id.imageViewSongRecommendation);
        mButtonExportImage = findViewById(R.id.buttonExportImage);
        mSeekBarTimeSpan = findViewById(R.id.seekBarTimeSpan);
        mTextViewSongRecommendationName = findViewById(R.id.textViewMostViewedSongName);
        mTextViewTopArtist.setText("Top Artist: Drake");
        mImageViewTopArtist.setImageResource(R.drawable.drake_icon);
        mTextViewMostPlayedSongs.setText("Most Played Songs:");
        LinearLayout linearLayout = findViewById(R.id.horizontalScrollViewMostPlayedSongsContainer);
        linearLayout.removeAllViews();
        for (int i = 0; i < 3; i++) {
            TextView textView = new TextView(this);
            textView.setText("Song "+i+" ");
            textView.setTextSize(16);
            textView.setTextColor(Color.BLACK);
            linearLayout.addView(textView);
        }
        mTextViewSongRecommendation.setText("Song Recommendation:");
        mImageViewSongRecommendation.setImageResource(R.drawable.song_recommendation);
        mTextViewSongRecommendationName.setText("Song 2");

        mButtonExportImage.setOnClickListener(v -> exportImage());
        mSeekBarTimeSpan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTimeSpan(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        updateTimeSpan(mSeekBarTimeSpan.getProgress());
    }

    private void updateTimeSpan(int progress) {
        mSeekBarTimeSpan.setProgress(progress);
        mTextViewMostPlayedSongs.setText("Most Played Songs (" + progress + " days)");
    }

    private void exportImage() {
        mCardView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(mCardView.getWidth(), mCardView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mCardView.draw(canvas);
        mCardView.setDrawingCacheEnabled(false);

        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory != null) {
            File file = new File(directory, "spotify_wrapped.png");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Toast.makeText(this, "Image saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Failed to save image", e);
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "External storage directory not available", Toast.LENGTH_SHORT).show();
        }
    }
}
