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
    private String email;
    private List<String> songs;
    private List<String> artists;
    private List<String> albums;
    private List<String> genres;

    // User class constructors
    public User(String firstName, String lastName, String username, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.songs = new ArrayList<>();
        this.artists = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.genres = new ArrayList<>();
    }

    // getters and setters for user attributes
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String username) { this.email = username; }
    public List<String> getSongs() { return songs; }
    public void setSongs(List<String> songs) { this.songs = songs; }
    public List<String> getArtists() { return artists; }
    public void setArtists(List<String> artists) { this.artists = artists; }
    public List<String> getAlbums() { return albums; }
    public void setAlbums(List<String> albums) { this.albums = albums; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    // other methods
    // Method to convert User object to HashMap
    public Map<String, Object> toHashMap() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("songs", songs);
        userMap.put("artists", artists);
        userMap.put("albums", albums);
        userMap.put("genres", genres);
        return userMap;
    }
}