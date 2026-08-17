package com.example.ai_chatbot.Controller;

import com.example.ai_chatbot.entity.ChatContext;
import com.example.ai_chatbot.security.ChatbotJwtAuthFilter;
import com.example.ai_chatbot.service.ChatContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chatbot/context")
public class ChatContextController {

    private static final Logger log = LoggerFactory.getLogger(ChatContextController.class);

    private final ChatContextService chatContextService;

    public ChatContextController(ChatContextService chatContextService) {
        this.chatContextService = chatContextService;
    }

    @PostMapping
    public DataVO<ChatContext> save(
            @RequestBody ChatContext chatContext,
            @RequestAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID) int userId) {
        long start = System.currentTimeMillis();
        try {
            chatContext.setUserId(userId);
            ChatContext saved = chatContextService.save(chatContext);
            return success(start, saved);
        } catch (IllegalArgumentException e) {
            return failure(400, start, e.getMessage());
        } catch (Exception e) {
            log.error("event=CHAT_CONTEXT_SAVE_ERROR error={}", e.getMessage(), e);
            return failure(500, start, "Unable to save chat context");
        }
    }

    @GetMapping("/{id}")
    public DataVO<ChatContext> findById(
            @PathVariable int id,
            @RequestAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID) int userId) {
        long start = System.currentTimeMillis();
        try {
            ChatContext chatContext = chatContextService.findById(id, userId);
            if (chatContext == null) {
                return failure(404, start, "Chat context not found");
            }
            return success(start, chatContext);
        } catch (IllegalArgumentException e) {
            return failure(400, start, e.getMessage());
        } catch (Exception e) {
            log.error("event=CHAT_CONTEXT_FIND_ERROR id={} userId={} error={}",
                    id, userId, e.getMessage(), e);
            return failure(500, start, "Unable to query chat context");
        }
    }

    @GetMapping
    public DataVO<List<ChatContext>> findBySessionId(
            @RequestParam String sessionId,
            @RequestAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID) int userId) {
        long start = System.currentTimeMillis();
        try {
            return success(start, chatContextService.findBySessionId(sessionId, userId));
        } catch (IllegalArgumentException e) {
            return failure(400, start, e.getMessage());
        } catch (Exception e) {
            log.error("event=CHAT_CONTEXT_LIST_ERROR sessionId={} userId={} error={}",
                    sessionId, userId, e.getMessage(), e);
            return failure(500, start, "Unable to query chat history");
        }
    }

    @PatchMapping("/{id}/read-status")
    public DataVO<Boolean> updateReadStatus(
            @PathVariable int id,
            @RequestAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID) int userId,
            @RequestParam boolean readStatus) {
        long start = System.currentTimeMillis();
        try {
            boolean updated = chatContextService.updateReadStatus(id, userId, readStatus);
            if (!updated) {
                return failure(404, start, "Chat context not found");
            }
            return success(start, true);
        } catch (IllegalArgumentException e) {
            return failure(400, start, e.getMessage());
        } catch (Exception e) {
            log.error("event=CHAT_CONTEXT_READ_ERROR id={} userId={} error={}",
                    id, userId, e.getMessage(), e);
            return failure(500, start, "Unable to update read status");
        }
    }

    @PatchMapping("/read-status")
    public DataVO<Integer> updateSessionReadStatus(
            @RequestParam String sessionId,
            @RequestAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID) int userId,
            @RequestParam(defaultValue = "true") boolean readStatus) {
        long start = System.currentTimeMillis();
        try {
            int updated = chatContextService.updateSessionReadStatus(
                    sessionId, userId, readStatus);
            return success(start, updated);
        } catch (IllegalArgumentException e) {
            return failure(400, start, e.getMessage());
        } catch (Exception e) {
            log.error("event=CHAT_CONTEXT_SESSION_READ_ERROR sessionId={} userId={} error={}",
                    sessionId, userId, e.getMessage(), e);
            return failure(500, start, "Unable to update chat history read status");
        }
    }

    @DeleteMapping("/{id}")
    public DataVO<Boolean> deleteById(
            @PathVariable int id,
            @RequestAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID) int userId) {
        long start = System.currentTimeMillis();
        try {
            boolean deleted = chatContextService.deleteById(id, userId);
            if (!deleted) {
                return failure(404, start, "Chat context not found");
            }
            return success(start, true);
        } catch (IllegalArgumentException e) {
            return failure(400, start, e.getMessage());
        } catch (Exception e) {
            log.error("event=CHAT_CONTEXT_DELETE_ERROR id={} userId={} error={}",
                    id, userId, e.getMessage(), e);
            return failure(500, start, "Unable to delete chat context");
        }
    }

    @DeleteMapping
    public DataVO<Integer> deleteBySessionId(
            @RequestParam String sessionId,
            @RequestAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID) int userId) {
        long start = System.currentTimeMillis();
        try {
            return success(start, chatContextService.deleteBySessionId(sessionId, userId));
        } catch (IllegalArgumentException e) {
            return failure(400, start, e.getMessage());
        } catch (Exception e) {
            log.error("event=CHAT_CONTEXT_SESSION_DELETE_ERROR sessionId={} userId={} error={}",
                    sessionId, userId, e.getMessage(), e);
            return failure(500, start, "Unable to delete chat history");
        }
    }

    private <T> DataVO<T> success(long start, T data) {
        return DataVO.buildDataVO(
                200, System.currentTimeMillis() - start, true, null, data);
    }

    private <T> DataVO<T> failure(int code, long start, String errorMessage) {
        return DataVO.buildDataVO(
                code, System.currentTimeMillis() - start, false, errorMessage, null);
    }
}
