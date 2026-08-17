package com.example.ai_chatbot.service;

import com.example.ai_chatbot.entity.ChatContext;

import java.util.List;

public interface ChatContextService {

    ChatContext save(ChatContext chatContext);

    ChatContext findById(int id, int userId);

    List<ChatContext> findBySessionId(String sessionId, int userId);

    boolean updateReadStatus(int id, int userId, boolean readStatus);

    int updateSessionReadStatus(String sessionId, int userId, boolean readStatus);

    boolean deleteById(int id, int userId);

    int deleteBySessionId(String sessionId, int userId);
}
