package com.example.Music_management.controller.cmd;

public class MusicCmd {
    private String title;
    private String artist;
    private int releaseYear;
    private String tags;
    private String lyrics;

    public MusicCmd() {
    }

    public MusicCmd(String title, String artist, int releaseYear, String tags, String lyrics) {
        this.title = title;
        this.artist = artist;
        this.releaseYear = releaseYear;
        this.tags = tags;
        this.lyrics = lyrics;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    @Override
    public String toString() {
        return "MusicCmd{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", releaseYear=" + releaseYear +
                ", tags='" + tags + '\'' +
                ", lyrics='" + lyrics + '\'' +
                '}';
    }
}
