package com.example.ai_chatbot.service.impl;

import com.example.ai_chatbot.entity.ChatContext;
import com.example.ai_chatbot.mapper.ChatContextMapper;
import com.example.ai_chatbot.service.ChatContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChatContextServiceImpl implements ChatContextService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ChatContextMapper chatContextMapper;

    public ChatContextServiceImpl(ChatContextMapper chatContextMapper) {
        this.chatContextMapper = chatContextMapper;
    }

    @Override
    @Transactional
    public ChatContext save(ChatContext chatContext) {
        validateForSave(chatContext);
        if (chatContext.getMessageSentTime() == null
                || chatContext.getMessageSentTime().isBlank()) {
            chatContext.setMessageSentTime(
                    LocalDateTime.now().format(MESSAGE_TIME_FORMATTER));
        }
        if (chatContext.getReadStatus() == null) {
            chatContext.setReadStatus(false);
        }
        chatContextMapper.insert(chatContext);
        return chatContext;
    }

    @Override
    public ChatContext findById(int id, int userId) {
        validateId(id);
        validateUserId(userId);
        return chatContextMapper.findById(id, userId);
    }

    @Override
    public List<ChatContext> findBySessionId(String sessionId, int userId) {
        validateSessionId(sessionId);
        validateUserId(userId);
        return chatContextMapper.findBySessionId(sessionId, userId);
    }

    @Override
    @Transactional
    public boolean updateReadStatus(int id, int userId, boolean readStatus) {
        validateId(id);
        validateUserId(userId);
        return chatContextMapper.updateReadStatus(id, userId, readStatus) > 0;
    }

    @Override
    @Transactional
    public int updateSessionReadStatus(String sessionId, int userId, boolean readStatus) {
        validateSessionId(sessionId);
        validateUserId(userId);
        return chatContextMapper.updateSessionReadStatus(sessionId, userId, readStatus);
    }

    @Override
    @Transactional
    public boolean deleteById(int id, int userId) {
        validateId(id);
        validateUserId(userId);
        return chatContextMapper.deleteById(id, userId) > 0;
    }

    @Override
    @Transactional
    public int deleteBySessionId(String sessionId, int userId) {
        validateSessionId(sessionId);
        validateUserId(userId);
        return chatContextMapper.deleteBySessionId(sessionId, userId);
    }

    private void validateForSave(ChatContext chatContext) {
        if (chatContext == null) {
            throw new IllegalArgumentException("Chat context must not be null");
        }
        validateSessionId(chatContext.getSessionId());
        validateUserId(chatContext.getUserId());
        if (chatContext.getMessageContent() == null
                || chatContext.getMessageContent().isBlank()) {
            throw new IllegalArgumentException("Message content must not be blank");
        }
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session id must not be blank");
        }
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User id must be greater than zero");
        }
    }

    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Chat context id must be greater than zero");
        }
    }
}
