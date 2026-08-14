package com.example.ai_chatbot.agent;

public interface AssistantAgent {
    String chat(String sessionId, String userInput, int userId);
}
