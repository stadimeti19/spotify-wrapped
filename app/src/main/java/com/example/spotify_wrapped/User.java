package com.example.spotify_wrapped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    // user attributes
    private String firstName;
    private String lastName;
    private String username;
    private HashMap<String, ArrayList<ArrayList<String>>> datesWrapped;
    private List<String> songs;
    private List<String> artists;
    private List<String> albums;
    private List<String> genres;

    // User class constructors
    public User(String firstName, String lastName, String username) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.datesWrapped = new HashMap<>();
        this.songs = new ArrayList<>();
        this.artists = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.genres = new ArrayList<>();
    }
    // Method to convert User object to HashMap
    public Map<String, Object> toHashMap() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("username", username);
        userMap.put("datesWrapped", datesWrapped);
        userMap.put("songs", songs);
        userMap.put("artists", artists);
        userMap.put("albums", albums);
        userMap.put("genres", genres);
        return userMap;
    }
}
