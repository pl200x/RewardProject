package com.example.ai_chatbot.service.impl;

import com.example.ai_chatbot.entity.ChatContext;
import com.example.ai_chatbot.mapper.ChatContextMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatContextServiceImplTest {

    private FakeChatContextMapper chatContextMapper;
    private ChatContextServiceImpl chatContextService;

    @BeforeEach
    void setUp() {
        chatContextMapper = new FakeChatContextMapper();
        chatContextService = new ChatContextServiceImpl(chatContextMapper);
    }

    @Test
    void fillsDefaultsAndSavesChatContext() {
        ChatContext chatContext = new ChatContext();
        chatContext.setSessionId("session-1");
        chatContext.setUserId(7);
        chatContext.setMessageContent("hello");

        ChatContext result = chatContextService.save(chatContext);

        assertEquals(10, result.getId());
        assertNotNull(result.getMessageSentTime());
        assertFalse(result.getReadStatus());
        assertEquals(chatContext, chatContextMapper.inserted);
    }

    @Test
    void queriesHistoryWithinUserAndSessionBoundary() {
        ChatContext expected = new ChatContext();
        chatContextMapper.history = List.of(expected);

        List<ChatContext> result = chatContextService.findBySessionId("session-1", 7);

        assertEquals(List.of(expected), result);
        assertEquals("session-1", chatContextMapper.queriedSessionId);
        assertEquals(7, chatContextMapper.queriedUserId);
    }

    @Test
    void rejectsIncompleteChatContext() {
        ChatContext chatContext = new ChatContext();
        chatContext.setSessionId("session-1");
        chatContext.setUserId(7);

        assertThrows(IllegalArgumentException.class,
                () -> chatContextService.save(chatContext));
    }

    private static class FakeChatContextMapper implements ChatContextMapper {

        private ChatContext inserted;
        private List<ChatContext> history = List.of();
        private String queriedSessionId;
        private int queriedUserId;

        @Override
        public int insert(ChatContext chatContext) {
            inserted = chatContext;
            chatContext.setId(10);
            return 1;
        }

        @Override
        public ChatContext findById(int id, int userId) {
            return null;
        }

        @Override
        public List<ChatContext> findBySessionId(String sessionId, int userId) {
            queriedSessionId = sessionId;
            queriedUserId = userId;
            return history;
        }

        @Override
        public int updateReadStatus(int id, int userId, boolean readStatus) {
            return 0;
        }

        @Override
        public int updateSessionReadStatus(
                String sessionId, int userId, boolean readStatus) {
            return 0;
        }

        @Override
        public int deleteById(int id, int userId) {
            return 0;
        }

        @Override
        public int deleteBySessionId(String sessionId, int userId) {
            return 0;
        }
    }
}
