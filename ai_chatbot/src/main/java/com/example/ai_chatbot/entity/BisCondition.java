package com.example.ai_chatbot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BisCondition {
    private String intentCode;
    private int targetUserId;
    private String startTime;
    private String endTime;
    private String currentDate;
}
