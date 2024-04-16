package com.example.spotify_wrapped;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class WrapData implements Parcelable {
    private String date;
    private List<String> trackList;
    private List<String> artistList;
    private List<String> topGenres;

    public static final String WRAP_DATA_KEY = "wrapData";

    // Constructor
    public WrapData(String date, List<String> trackList, List<String> artistList, List<String> topGenres) {
        this.date = date;
        this.trackList = trackList;
        this.artistList = artistList;
        this.topGenres = topGenres;
    }

    // Parcelable constructor
    protected WrapData(Parcel in) {
        date = in.readString();
        trackList = in.createStringArrayList();
        artistList = in.createStringArrayList();
        topGenres = in.createStringArrayList();
    }

    // Parcelable CREATOR
    public static final Creator<WrapData> CREATOR = new Creator<WrapData>() {
        @Override
        public WrapData createFromParcel(Parcel in) {
            return new WrapData(in);
        }

        @Override
        public WrapData[] newArray(int size) {
            return new WrapData[size];
        }
    };

    // Getter and setter methods

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<String> trackList) {
        this.trackList = trackList;
    }

    public List<String> getArtistList() {
        return artistList;
    }

    public void setArtistList(List<String> artistList) {
        this.artistList = artistList;
    }

    public List<String> getTopGenres() {
        return topGenres;
    }

    public void setTopGenres(List<String> topGenres) {
        this.topGenres = topGenres;
    }

    // Parcelable methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeStringList(trackList);
        dest.writeStringList(artistList);
        dest.writeStringList(topGenres);
    }
}