package com.example.ai_chatbot.service;

import com.example.ai_chatbot.context.MCPContextManager;
import com.example.ai_chatbot.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataToNaturalLanguageService {

    @Autowired
    private OpenaiClientService openaiClientService;

    public String convert(String userInput, String jsonData){
         String prompt ="""
             你是智能数据助手，严格遵守以下规则：
             1. 不要输出原始JSON、不要markdown代码块；
             2. 根据【用户提问】和【查询数据】，整理成通俗易懂的中文回答；
             3. 如果查询结果为空、没有记录，直接礼貌告知：未查询到对应记录；
             4. 可以适当做简单汇总，例如记录条数、总额，但不要编造不存在的数据；
             5. 回答简洁干净，不要多余开场白。
             用户原始提问：%s
             后端查询得到的数据：%s
             """.formatted(userInput, jsonData);
         List<Message> messages = new ArrayList<>();
         Message responseNaturalLanguage = new Message();
         responseNaturalLanguage.setContent(prompt);
         responseNaturalLanguage.setRole("system");
         messages.add(responseNaturalLanguage);
         return openaiClientService.chatCompletion(messages);

    }
}
