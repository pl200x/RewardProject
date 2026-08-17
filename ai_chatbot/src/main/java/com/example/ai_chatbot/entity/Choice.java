package com.example.ai_chatbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Choice {
    private Integer index;
    private Message message;
    private String logprobs;
    private String finish_reason;
}
