package com.example.spotify_wrapped;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import androidx.navigation.NavController;

import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.view.View;

import com.example.spotify_wrapped.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements HomePage.OnLoginSuccessListener {
    private static final String TAG = "MainActivity";
    public static final String CLIENT_ID = "c7e24e2587ce44b89dfe5494431930e3";
    public static final String REDIRECT_URI = "spotify-wrapped://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode;
    private Call mCall;
    private TextView tokenTextView, codeTextView, profileTextView, tracksTextView, genresTextView;
    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ArrayList<String> shortTrackList;
    private ArrayList<String> trackList;
    private ArrayList<String> longTrackList;
    private ArrayList<String> shortArtistList;
    private ArrayList<String> artistList;
    private ArrayList<String> longArtistList;
    private ArrayList<String> shortTopGenres;
    private ArrayList <String> topGenres;
    private ArrayList<String> longTopGenres;
    private String shortTrackImageUrl;
    private String trackImageUrl;
    private String longTrackImageUrl;
    private String shortArtistImageUrl;
    private String artistImageUrl;
    private String longArtistImageUrl;
    private String shortGenreImageUrl;
    private String genreImageUrl;
    private String longGenreImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the ActionBar
        setSupportActionBar(binding.toolbar);

        // inflate basic_layout.xml to access its views
        View rootView = getLayoutInflater().inflate(R.layout.basic_layout, null);

        // Initialize NavHostFragment and NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        // Set up the AppBarConfiguration with the NavGraph
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        // Set up ActionBar with NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        HomePage homePageFragment = HomePage.newInstance(MainActivity.this);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize the views
        tokenTextView = rootView.findViewById(R.id.token_text_view);
        codeTextView = rootView.findViewById(R.id.code_text_view);
        profileTextView = rootView.findViewById(R.id.response_text_view);
        tracksTextView = rootView.findViewById(R.id.tracks_text_view);
        genresTextView = rootView.findViewById(R.id.genres_text_view);
    }

    public void onGetTopShortSongsClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user's top tracks
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonResponse = response.body().string();
                    Log.d("Track Response", jsonResponse);
                    JSONArray items = new JSONObject(jsonResponse).getJSONArray("items");
                    shortTrackList = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject track = items.getJSONObject(i);
                        shortTrackList.add((i + 1) + ". " + track.getString("name"));
                        JSONArray imagesArray = track.getJSONObject("album").getJSONArray("images");
                        if (i == 0) {
                            JSONObject firstImage = imagesArray.getJSONObject(0);
                            shortTrackImageUrl = firstImage.getString("url");
                            Log.d("Image URL", shortTrackImageUrl);
                            storeImageInFirebase(shortTrackImageUrl,"short_song_image");
                        }
                    }
                    storeTopInFirebase(shortTrackList, "short_term_songs", () -> {
                        onGetTopLongSongsClicked();
                    });
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void onGetTopLongSongsClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user's top tracks
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?time_range=long_term&limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonResponse = response.body().string();
                    Log.d("Track Response", jsonResponse);
                    JSONArray items = new JSONObject(jsonResponse).getJSONArray("items");
                    longTrackList = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject track = items.getJSONObject(i);
                        longTrackList.add((i + 1) + ". " + track.getString("name"));
                        JSONArray imagesArray = track.getJSONObject("album").getJSONArray("images");
                        if (i == 0) {
                            JSONObject firstImage = imagesArray.getJSONObject(0);
                            longTrackImageUrl = firstImage.getString("url");
                            Log.d("Image URL", longTrackImageUrl);
                            storeImageInFirebase(longTrackImageUrl,"long_song_image");
                        }
                    }
                    storeTopInFirebase(longTrackList, "long_term_songs", () -> {
                        onGetTopGenresClicked();
                    });
                    //setTextAsync(tracks.toString(), tracksTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onLoginSuccess() {
        Log.d(TAG, "Login success callback invoked");
        getToken();
    }

    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken() {
        Log.d(TAG, "Login success6986876 callback invoked");
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_CODE_REQUEST_CODE, request);
    }


    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Login successlkjh callback invoked");
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        // Check which request code is present (if any)
        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            getCode();
            onGetTopArtistsClicked();
            //onGetTopTracksClicked();
            Log.d(TAG, "Tracks callback invoked");

            //setTextAsync(mAccessToken, tokenTextView);

        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            setTextAsync(mAccessCode, codeTextView);
        }
    }


    /**
     * Get user profile
     * This method will get the user profile using the token
     */
    public void onGetTopArtistsClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request profileRequest = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(profileRequest);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch profile data: " + e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to fetch profile data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBodyString = response.body().string();
                    Log.d("Spotify Profile JSON", responseBodyString);

                    final JSONObject jsonObject = new JSONObject(responseBodyString);
                    JSONArray images = jsonObject.getJSONArray("images");
                    String profileImageUrl = "https://thumbs.dreamstime.com/b/default-avatar-profile-icon-vector-social-media-user-image-182145777.jpg";
                    if (images.length() > 0) {
                        JSONObject imageObj = images.getJSONObject(0);
                        profileImageUrl = imageObj.getString("url");
                    }

                    storeProfileImageInFirebase(profileImageUrl);

                    final Request artistsRequest = new Request.Builder()
                            .url("https://api.spotify.com/v1/me/top/artists?limit=10")
                            .addHeader("Authorization", "Bearer " + mAccessToken)
                            .build();

                    cancelCall(); // Cancel previous call if any
                    mCall = mOkHttpClient.newCall(artistsRequest);

                    mCall.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("HTTP", "Failed to fetch top artists: " + e);
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Failed to fetch top artists, watch Logcat for more details",
                                        Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String jsonResponse = response.body().string();
                                Log.d("JSON Response", jsonResponse);
                                JSONArray items = new JSONObject(jsonResponse).getJSONArray("items");
                                artistList = new ArrayList<>();
                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject artist = items.getJSONObject(i);
                                    artistList.add((i + 1) + ". " + artist.getString("name"));
                                    if (i == 0) {
                                        JSONArray imagesArray = artist.getJSONArray("images");
                                        if (imagesArray.length() > 0) {
                                            JSONObject firstImage = imagesArray.getJSONObject(0);
                                            artistImageUrl = firstImage.getString("url");
                                            Log.d("Image URL", artistImageUrl);
                                            storeArtistImageInFirebase(artistImageUrl);
                                        }
                                    }
                                }
                                storeTopInFirebase(artistList, "artists", () -> {
                                    onGetTopShortArtistClicked();
                                });

                                //setTextAsync(artists.toString(), profileTextView);
                            } catch (JSONException e) {
                                Log.d("JSON", "Failed to parse top artists data: " + e);
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Failed to parse top artists data, watch Logcat for more details",
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });

                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse profile data: " + e);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Failed to parse profile data, watch Logcat for more details",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    public void onGetTopShortArtistClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();
        cancelCall();
        mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to fetch data artists, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray items = jsonObject.getJSONArray("items");
                    shortArtistList = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject artist = items.getJSONObject(i);
                        shortArtistList.add((i + 1) + ". " + artist.getString("name"));
                        if (i == 0) {
                            JSONArray imagesArray = artist.getJSONArray("images");
                            if (imagesArray.length() > 0) {
                                JSONObject firstImage = imagesArray.getJSONObject(0);
                                shortArtistImageUrl = firstImage.getString("url");
                                Log.d("Image URL", shortArtistImageUrl);
                                storeImageInFirebase(shortArtistImageUrl, "short_artist_image");
                            }
                        }
                    }
                    storeTopInFirebase(shortArtistList, "short_term_artists", () -> {
                        onGetTopLongArtistClicked();
                    });
                    //setTextAsync(artists.toString(), profileTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void onGetTopLongArtistClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=long_term&limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();
        cancelCall();
        mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to fetch data artists, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray items = jsonObject.getJSONArray("items");
                    longArtistList = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject artist = items.getJSONObject(i);
                        longArtistList.add((i + 1) + ". " + artist.getString("name"));
                        if (i == 0) {
                            JSONArray imagesArray = artist.getJSONArray("images");
                            if (imagesArray.length() > 0) {
                                JSONObject firstImage = imagesArray.getJSONObject(0);
                                longArtistImageUrl = firstImage.getString("url");
                                Log.d("Image URL", longArtistImageUrl);
                                storeImageInFirebase(longArtistImageUrl, "long_artist_image");
                            }
                        }
                    }
                    storeTopInFirebase(longArtistList, "long_term_artists", () -> {
                        onGetTopTracksClicked();
                    });
                    //setTextAsync(artists.toString(), profileTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeArtistImageInFirebase(String artistImageURL) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.update("artistImageUrl", artistImageURL)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Artist image URL stored in Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error storing artist image URL in Firebase", e));
        } else {
            Log.e(TAG, "Current user is null");
        }
    }


    private void storeImageInFirebase(String ImageUrl, String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.update(id, ImageUrl)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Genre image URL stored in Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error storing genre image URL in Firebase", e));
        } else {
            Log.e(TAG, "Current user is null");
        }
    }
    private void storeGenreImageInFirebase(String genreImageURL) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.update("genreImageUrl", genreImageURL)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Genre image URL stored in Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error storing genre image URL in Firebase", e));
        } else {
            Log.e(TAG, "Current user is null");
        }
    }

    private void storeProfileImageInFirebase(String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.update("profileImageUrl", imageUrl)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile image URL stored in Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error storing profile image URL in Firebase", e));
        } else {
            Log.e(TAG, "Current user is null");
        }
    }

    private void storeTopInFirebase(List<String> list, String type, final Runnable callback) {
        // Initialize Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get a reference to the users collection in Firestore
        CollectionReference usersCollection = db.collection("users");

        // Get current user's ID or username (you may need to adjust this based on your Firebase user management)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Store the array of top artists in Firebase under the user's document
        usersCollection.document(userId)
                .update(type, list)
                .addOnSuccessListener(documentReference -> {
                    // Successfully stored top artists in Firebase
                    Log.d(TAG, type +  "stored in Firebase: " + list);
                    callback.run();
                })
                .addOnFailureListener(e -> {
                    // Failed to store top artists in Firebase
                    Log.e(TAG, "Error storing top artists in Firebase", e);
                    Toast.makeText(MainActivity.this, "Failed to store top artists in Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    public void onGetTopTracksClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user's top tracks
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonResponse = response.body().string();
                    Log.d("Track Response", jsonResponse);
                    JSONArray items = new JSONObject(jsonResponse).getJSONArray("items");
                    trackList = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject track = items.getJSONObject(i);
                        trackList.add((i + 1) + ". " + track.getString("name"));
                        JSONArray imagesArray = track.getJSONObject("album").getJSONArray("images");
                        if (i == 0) {
                            JSONObject firstImage = imagesArray.getJSONObject(0);
                            trackImageUrl = firstImage.getString("url");
                            Log.d("Image URL", trackImageUrl);
                            storeSongImageInFirebase(trackImageUrl);
                        }
                    }
                    storeTopInFirebase(trackList, "songs", () -> {
                        onGetTopShortSongsClicked();
                    });
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeSongImageInFirebase(String songImageURL) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.update("songImageUrl", songImageURL)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Song image URL stored in Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error storing song image URL in Firebase", e));
        } else {
            Log.e(TAG, "Current user is null");
        }
    }


    public void onGetTopGenresClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user's top artists
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonResponse = response.body().string();
                    Log.d("Genre Response", jsonResponse);
                    JSONArray items = new JSONObject(jsonResponse).getJSONArray("items");
                    ArrayList<String> genres = new ArrayList<>();
                    topGenres = new ArrayList<>();

                    int count = 0;
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject artist = items.getJSONObject(i);
                        JSONArray imagesArray = artist.getJSONArray("images");
                        if (i==1) {
                            JSONObject firstImage = imagesArray.getJSONObject(0);
                            genreImageUrl = firstImage.getString("url");
                            Log.d("Genre Image URL", genreImageUrl);
                            storeGenreImageInFirebase(genreImageUrl);
                        }

                        JSONArray genresArray = artist.getJSONArray("genres");

                        for (int j = 0; j < genresArray.length(); j++) {
                            genres.add(genresArray.getString(j) + "\n");
                            count++;
                        }
                    }
                    Map<String, Integer> genreCounts = new HashMap<>();
                    for (String genre : genres) {
                        genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
                    }

                    // Create a new ArrayList to store the most occurring genres
                    List<Map.Entry<String, Integer>> genreList = new ArrayList<>(genreCounts.entrySet());
                    // Sort the genreList based on the count of occurrences (value)
                    genreList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    for (int i = 0; i < 10; ++i) {
                        topGenres.add((i + 1) + ". " + genreList.get(i).getKey());
                    }

                    storeTopInFirebase(topGenres, "genres", () -> {
                        onGetTopShortGenres();
                    });
                    //setTextAsync(topGenres.toString(), genresTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void onGetTopShortGenres() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user's top artists
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonResponse = response.body().string();
                    Log.d("Genre Response", jsonResponse);
                    JSONArray items = new JSONObject(jsonResponse).getJSONArray("items");
                    ArrayList<String> genres = new ArrayList<>();
                    shortTopGenres = new ArrayList<>();

                    int count = 0;
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject artist = items.getJSONObject(i);
                        JSONArray imagesArray = artist.getJSONArray("images");
                        if (i==1) {
                            JSONObject firstImage = imagesArray.getJSONObject(0);
                            shortGenreImageUrl = firstImage.getString("url");
                            Log.d("Genre Image URL", shortGenreImageUrl);
                            storeImageInFirebase(shortGenreImageUrl,"short_genre_image");
                        }

                        JSONArray genresArray = artist.getJSONArray("genres");

                        for (int j = 0; j < genresArray.length(); j++) {
                            genres.add(genresArray.getString(j) + "\n");
                            count++;
                        }
                    }
                    Map<String, Integer> genreCounts = new HashMap<>();
                    for (String genre : genres) {
                        genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
                    }

                    // Create a new ArrayList to store the most occurring genres
                    List<Map.Entry<String, Integer>> genreList = new ArrayList<>(genreCounts.entrySet());
                    // Sort the genreList based on the count of occurrences (value)
                    genreList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    for (int i = 0; i < 10; ++i) {
                        shortTopGenres.add((i + 1) + ". " + genreList.get(i).getKey());
                    }

                    storeTopInFirebase(shortTopGenres, "short_term_genres", () -> {
                        onGetTopLongGenres();
                    });
                    //setTextAsync(topGenres.toString(), genresTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void onGetTopLongGenres() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user's top artists
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=long_term&limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonResponse = response.body().string();
                    Log.d("Genre Response", jsonResponse);
                    JSONArray items = new JSONObject(jsonResponse).getJSONArray("items");
                    ArrayList<String> genres = new ArrayList<>();
                    longTopGenres = new ArrayList<>();
                    //String genres = "";


                    int count = 0;
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject artist = items.getJSONObject(i);
                        JSONArray imagesArray = artist.getJSONArray("images");
                        if (i==1) {
                            JSONObject firstImage = imagesArray.getJSONObject(0);
                            longGenreImageUrl = firstImage.getString("url");
                            Log.d("Genre Image URL", longGenreImageUrl);
                            storeImageInFirebase(longGenreImageUrl,"long_genre_image");
                        }

                        JSONArray genresArray = artist.getJSONArray("genres");

                        for (int j = 0; j < genresArray.length(); j++) {
                            genres.add(genresArray.getString(j) + "\n");
                            count++;
                        }
                    }
                    Map<String, Integer> genreCounts = new HashMap<>();
                    for (String genre : genres) {
                        genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
                    }

                    // Create a new ArrayList to store the most occurring genres
                    List<Map.Entry<String, Integer>> genreList = new ArrayList<>(genreCounts.entrySet());
                    // Sort the genreList based on the count of occurrences (value)
                    genreList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    for (int i = 0; i < 10; ++i) {
                        longTopGenres.add((i + 1) + ". " + genreList.get(i).getKey());
                    }

                    storeTopInFirebase(longTopGenres, "long_term_genres", () -> {
                        updateFirestoreWithWrap();
                    });
                    //setTextAsync(topGenres.toString(), genresTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateFirestoreWithWrap() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get a reference to the users collection in Firestore
        CollectionReference usersCollection = db.collection("users");

        // Get current user's ID or username (you may need to adjust this based on your Firebase user management)
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = usersCollection.document(userId);

            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String date = documentSnapshot.getString("datesWrapped"); // Replace "date" with the actual field name where the date is stored
                    if (date != null && !date.isEmpty()) {
                        // Now you have the date for the current user
                        // Proceed with creating the wrap data
                        DocumentReference userWrapsRef = db.collection("wraps").document(userId);

                        // Create a map containing the wrap data
                        Map<String, Object> wrapData = new HashMap<>();
                        wrapData.put("shortTrackList", shortTrackList);
                        wrapData.put("trackList", trackList);
                        wrapData.put("longTrackList", longTrackList);
                        wrapData.put("shortArtistList", shortArtistList);
                        wrapData.put("artistList", artistList);
                        wrapData.put("longArtistList", longArtistList);
                        wrapData.put("shortTopGenres", shortTopGenres);
                        wrapData.put("topGenres", topGenres);
                        wrapData.put("longTopGenres", longTopGenres);
                        wrapData.put("shortTrackImageUrl", shortTrackImageUrl);
                        wrapData.put("trackImageUrl", trackImageUrl);
                        wrapData.put("longTrackImageUrl", longTrackImageUrl);
                        wrapData.put("shortArtistImageUrl", shortArtistImageUrl);
                        wrapData.put("artistImageUrl", artistImageUrl);
                        wrapData.put("longArtistImageUrl", longArtistImageUrl);
                        wrapData.put("shortGenreImageUrl", shortGenreImageUrl);
                        wrapData.put("genreImageUrl", genreImageUrl);
                        wrapData.put("longGenreImageUrl", longGenreImageUrl);

                        // Set the wrap data under the current date
                        userWrapsRef.collection("dates").document(date)
                                .set(wrapData)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Wrap data successfully updated in Firestore"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating wrap data in Firestore", e));
                        navigateToStartActivity();
                    } else {
                        Log.d(TAG, "Date is null or empty");
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user document from Firestore", e);
            });
        } else {
            Log.e(TAG, "Current user is null");
        }
    }

    /**
     * Creates a UI thread to update a TextView in the background
     * Reduces UI latency and makes the system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

    /**
     * Get authentication request
     *
     * @param type the type of the request
     * @return the authentication request
     */
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-top-read" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
    }

    public void logout() {
        // Clear the access token
        mAccessToken = null;
        // Clear any other session-related information if needed

        // Redirect the user to the login screen or any initial screen
        // For example, you can start a new LoginActivity
        Intent intent = new Intent(MainActivity.this, WrappedActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity to prevent going back to it with back button
    }
    /**
     * Gets the redirect Uri for Spotify
     *
     * @return redirect Uri object
     */
    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void navigateToStartActivity() {
        Intent intent = new Intent(MainActivity.this, startActivity.class);
        intent.putExtra("accessToken", mAccessToken);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Navigate up when the ActionBar back button is pressed
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}