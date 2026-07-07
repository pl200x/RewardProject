package com.example.Music_management.controller.vo;

public class MusicDetailVO {
    private int id;
    private String title;
    private String artist;
    private int releaseYear;
    private String tags;
    private String lyrics;
    private String status;
    private long ranking;

    public long getRanking() {
        return ranking;
    }

    public void setRanking(long ranking) {
        this.ranking = ranking;
    }

    private double score;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public MusicDetailVO(int id, String title, String artist, int releaseYear, String tags, String lyrics, String status, long ranking, double score) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.releaseYear = releaseYear;
        this.tags = tags;
        this.lyrics = lyrics;
        this.status = status;
        this.ranking = ranking;
        this.score = score;
    }

    public MusicDetailVO() {
    }

    @Override
    public String toString() {
        return "MusicDetailVO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", releaseYear=" + releaseYear +
                ", tags='" + tags + '\'' +
                ", lyrics='" + lyrics + '\'' +
                ", status='" + status + '\'' +
                ", ranking=" + ranking +
                ", score=" + score +
                '}';
    }
}
