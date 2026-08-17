package com.example.ai_chatbot.Controller;

import com.example.ai_chatbot.agent.AssistantAgent;
import com.example.ai_chatbot.entity.ChatContext;
import com.example.ai_chatbot.security.ChatbotJwtAuthFilter;
import com.example.ai_chatbot.service.ChatContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("chatbot")
public class MCPChatController {

    private static final Logger log = LoggerFactory.getLogger(MCPChatController.class);

    private final AssistantAgent assistantAgent;
    private final ChatContextService chatContextService;

    public MCPChatController(
            AssistantAgent assistantAgent,
            ChatContextService chatContextService) {
        this.assistantAgent = assistantAgent;
        this.chatContextService = chatContextService;
    }

    @PostMapping("/chat")
    public DataVO<String> chat(
            @RequestParam String sessionId,
            @RequestParam String userInput,
            @RequestAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID) int userId) {
        long start = System.currentTimeMillis();
        String normalizedSessionId = sessionId == null ? "" : sessionId.trim();
        String normalizedInput = userInput == null ? "" : userInput.trim();
        if (normalizedSessionId.isEmpty() || normalizedSessionId.length() > 128) {
            return DataVO.buildDataVO(400, System.currentTimeMillis() - start,
                    false, "sessionId must contain 1 to 128 characters", null);
        }
        if (normalizedInput.isEmpty() || normalizedInput.length() > 500) {
            return DataVO.buildDataVO(400, System.currentTimeMillis() - start,
                    false, "userInput must contain 1 to 500 characters", null);
        }
        long end;
        try {
            saveChatContext(normalizedSessionId, userId, normalizedInput, true);
            String answer = assistantAgent.chat(normalizedSessionId, normalizedInput, userId);
            saveChatContext(normalizedSessionId, userId, answer, false);
            end = System.currentTimeMillis();
            return DataVO.buildDataVO(200, end - start, true, null, answer);
        } catch (Exception e) {
            end = System.currentTimeMillis();
            log.error("event=CHAT_ERROR sessionId={} userId={} error={}",
                    normalizedSessionId, userId, e.getMessage(), e);
            return DataVO.buildDataVO(
                    500, end - start, false, "Unable to complete the chat request", null);
        }
    }

    private void saveChatContext(
            String sessionId,
            int userId,
            String messageContent,
            boolean readStatus) {
        ChatContext chatContext = new ChatContext();
        chatContext.setSessionId(sessionId);
        chatContext.setUserId(userId);
        chatContext.setMessageContent(messageContent);
        chatContext.setReadStatus(readStatus);
        chatContextService.save(chatContext);
    }
}
