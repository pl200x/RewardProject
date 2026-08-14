package com.example.ai_chatbot.Controller;

import com.example.ai_chatbot.agent.AssistantAgent;
import com.example.ai_chatbot.entity.ChatContext;
import com.example.ai_chatbot.service.ChatContextService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPChatControllerTest {

    @Test
    void savesUserAndAssistantMessages() {
        AssistantAgent assistantAgent = (sessionId, userInput, userId) -> "model answer";
        FakeChatContextService chatContextService = new FakeChatContextService();
        MCPChatController controller = new MCPChatController(
                assistantAgent, chatContextService);

        DataVO<String> response = controller.chat("session-1", "user question", 7);

        assertTrue(response.isSuccess());
        assertEquals("model answer", response.getData());

        List<ChatContext> saved = chatContextService.saved;

        assertEquals(2, saved.size());
        assertEquals("user question", saved.get(0).getMessageContent());
        assertTrue(saved.get(0).getReadStatus());
        assertEquals("model answer", saved.get(1).getMessageContent());
        assertFalse(saved.get(1).getReadStatus());
    }

    @Test
    void rejectsOversizedInputBeforeCallingDependencies() {
        AssistantAgent assistantAgent = (sessionId, userInput, userId) -> {
            throw new AssertionError("agent must not be called for invalid input");
        };
        FakeChatContextService chatContextService = new FakeChatContextService();
        MCPChatController controller = new MCPChatController(
                assistantAgent, chatContextService);

        DataVO<String> response = controller.chat("session-1", "x".repeat(501), 7);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertTrue(chatContextService.saved.isEmpty());
    }

    private static class FakeChatContextService implements ChatContextService {

        private final List<ChatContext> saved = new ArrayList<>();

        @Override
        public ChatContext save(ChatContext chatContext) {
            saved.add(chatContext);
            return chatContext;
        }

        @Override
        public ChatContext findById(int id, int userId) {
            return null;
        }

        @Override
        public List<ChatContext> findBySessionId(String sessionId, int userId) {
            return List.of();
        }

        @Override
        public boolean updateReadStatus(int id, int userId, boolean readStatus) {
            return false;
        }

        @Override
        public int updateSessionReadStatus(
                String sessionId, int userId, boolean readStatus) {
            return 0;
        }

        @Override
        public boolean deleteById(int id, int userId) {
            return false;
        }

        @Override
        public int deleteBySessionId(String sessionId, int userId) {
            return 0;
        }
    }
}
