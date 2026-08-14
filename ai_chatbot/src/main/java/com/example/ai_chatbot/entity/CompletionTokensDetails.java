package com.example.ai_chatbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompletionTokensDetails {
    private Integer reasoning_tokens;
    private Integer audio_tokens;
    private Integer accepted_prediction_tokens;
    private Integer rejected_prediction_tokens;
}
