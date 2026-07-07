package com.example.Music_management.service;

import java.util.List;

public interface LikeService {
     void like(int userId, int musicId);
     void unlike(int userId, int musicId);
     List<Integer> getUserLike(int userId);
     List<Integer> getMusicLike(int musicId);
     long getUserTotalLike(int userId);
     long getMusicTotalLike(int musicId);
}
