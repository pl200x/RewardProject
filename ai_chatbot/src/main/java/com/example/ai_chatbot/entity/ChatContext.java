package com.example.ai_chatbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatContext {
    private int id;
    private String sessionId;
    private int userId;
    private String messageContent;
    private String messageSentTime;
    private Boolean readStatus;
}
