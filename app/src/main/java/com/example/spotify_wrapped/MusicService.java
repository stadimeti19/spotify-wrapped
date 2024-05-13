package com.example.spotify_wrapped;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable stopPlaybackRunnable;
    private ArrayList<String> trackListUrls;
    private int currIndex;
    private static final String TAG = "MusicService";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "MusicService start command");
        // Initialize MediaPlayer and start playback
        mediaPlayer = new MediaPlayer();
        trackListUrls = intent.getStringArrayListExtra("trackListUrls");
        Log.e(TAG, "track size: " + trackListUrls.size());
        currIndex = 0;
        playNextSong();
        return START_STICKY;
    }
    private void playNextSong() {
        if (currIndex < trackListUrls.size()) {
            String nextSongUrl = trackListUrls.get(currIndex);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(nextSongUrl);
                mediaPlayer.setOnPreparedListener(mp -> {
                    Log.e(TAG, "onPreparedListener: " + currIndex);
                    if (mp == mediaPlayer) {
                        mediaPlayer.start();
                        handler = new Handler();
                        stopPlaybackRunnable = () -> {
                            if (mp != null && mp.isPlaying()) {
                                mp.stop();
                                currIndex++;
                                playNextSong();
                            }
                        };
                        handler.postDelayed(stopPlaybackRunnable, 15000); // Track duration}
                    }
                });
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Stop the service when all songs have been played
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (handler != null) {
            handler.removeCallbacks(stopPlaybackRunnable);
            handler = null;
        }
    }
}
