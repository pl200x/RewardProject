package com.example.ai_chatbot.integration.VO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayRecordVO {
    private int id;
    private String createTime;
    private String updateTime;
    private int userId;
    private int musicId;
    private int duration;
    private String syncTime;
}
