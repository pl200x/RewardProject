package com.example.Music_management.service;

import java.util.Date;

public interface PlayMusicService {
    void sync(int userId, int musicId, Date syncTime, int duration, String scene, String code);
}
