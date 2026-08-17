package com.example.ai_chatbot.context;

import com.example.ai_chatbot.entity.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MCPContextManager {
    private final Map<String, List<Message>> contextMap
             = new ConcurrentHashMap<>();
    private final static String firstSystemPrompt = """
            你是业务查询参数抽取器，只输出 JSON，禁止输出多余文字，禁止使用 Markdown。

            可选意图：

            PLAY_RECORD：查询播放记录

            AWARD_RECORD：查询发奖记录

            TOTAL_DURATION：查询播放总时长

            CHAT：普通闲聊，不需要查询业务

            抽取规则：

            1. targetUserId：直接设置为空。

            2. startTime、endTime：用户提到时间段时进行提取，格式为 yyyy-MM-dd HH:mm:ss；如果没有提供时间，默认取最近 30 天；
            如果用户指定某一天，则取当天 00:00:00 至 23:59:59。

            3. currentDate：默认传入当天日期，字符串类型，格式为 yyyy-MM-dd。

            4. 如果是闲聊，intentCode 固定为 CHAT，其他字段留空。

            5. intentCode根据用户的输入自动识别。
            输出固定 JSON 结构示例：

            {
              "intentCode": "AWARD_RECORD",
              "targetUserId": 10001,
              "startTime": "2026-08-01 00:00:00",
              "endTime": "2026-08-03 12:00:00",
              "currentDate": "2026-08-03"
            }
            """;
    @Value("${spring.ai.openai.max-message-size}")
    private int maxMessageSize;

    public List<Message> getContext(String sessionId){
        //return contextMap.get(sessionId);
        List<Message> returnMessageList = contextMap.get(sessionId);
        if(returnMessageList == null){
            returnMessageList = new ArrayList<>();
            Message firstMessage = new Message();
            firstMessage.setRole("system");
            firstMessage.setContent(firstSystemPrompt);
            returnMessageList.add(firstMessage);
            contextMap.put(sessionId, returnMessageList);
        }
        return returnMessageList;
    }

    public void appendMessage(String sessionId,Message message){
        List<Message> targetMessageList = getContext(sessionId);
        targetMessageList.add(message);
        if(targetMessageList.size() > maxMessageSize){
            targetMessageList.remove(1);
        }
    }

    public void deleteContext(String sessionId){
        contextMap.remove(sessionId);
    }


}
