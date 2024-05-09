package com.example.spotify_wrapped;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class WrapData implements Parcelable {
    private String date;
    private List<String> trackList;
    private List<String> trackListUrls;
    private List<String> artistList;
    private List<String> topGenres;
    private String trackImageUrl;
    private String artistImageUrl;
    private String genreImageUrl;
    public WrapData(String date, List<String> trackList, List<String> trackListUrls, String trackImageUrl,
                    List<String> artistList, String artistImageUrl, List<String> topGenres, String genreImageUrl) {
        this.date = date;
        this.trackList = trackList;
        this.trackListUrls = trackListUrls;
        this.artistList = artistList;
        this.topGenres = topGenres;
        this.trackImageUrl = trackImageUrl;
        this.artistImageUrl = artistImageUrl;
        this.genreImageUrl = genreImageUrl;
    }

    public static final String WRAP_DATA_KEY = "wrapData";

    // Parcelable constructor
    protected WrapData(Parcel in) {
        date = in.readString();
        trackList = in.createStringArrayList();
        trackListUrls = in.createStringArrayList();
        trackImageUrl = in.readString();
        artistList = in.createStringArrayList();
        artistImageUrl = in.readString();
        topGenres = in.createStringArrayList();
        genreImageUrl = in.readString();
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
    public List<String> getTrackList() {
        return trackList;
    }
    public List<String> getTrackListUrls() {
        return trackListUrls;
    }
    public List<String> getArtistList() {
        return artistList;
    }
    public List<String> getTopGenres() {
        return topGenres;
    }
    public String getTrackImageUrl() {
        return trackImageUrl;
    }
    public String getArtistImageUrl() {
        return artistImageUrl;
    }
    public String getGenreImageUrl() {
        return genreImageUrl;
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
        dest.writeStringList(trackListUrls);
        dest.writeString(trackImageUrl);
        dest.writeStringList(artistList);
        dest.writeString(artistImageUrl);
        dest.writeStringList(topGenres);
        dest.writeString(genreImageUrl);
    }
}