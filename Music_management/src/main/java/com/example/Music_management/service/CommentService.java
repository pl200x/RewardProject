package com.example.Music_management.service;

import com.example.Music_management.entity.MusicComment;

import java.util.List;

public interface CommentService {
    void addComment(int userId, int musicId, String content);
    List<MusicComment> getMusicComments(int musicId);
    long getMusicTotalComment(int musicId);
    List<Integer> getUserComment(int userId);
    long getUserTotalComment(int userId);
}
