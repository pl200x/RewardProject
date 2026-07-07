package com.example.Music_management.controller.vo;

/**
 * 列表用轻量摘要:不含 lyrics 等重字段。
 * tags(曲风丸)/score(人气分,来自 Redis 排行,非 DB 查询)为展示型可选字段。
 */
public class MusicDigestVO {
    private String title;
    private String artist;
    private int id;
    private String tags;
    private double score;

    public MusicDigestVO() {
    }

    public MusicDigestVO(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MusicDigestVO{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }
}
