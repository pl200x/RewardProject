package com.example.ai_chatbot.service;

import com.example.ai_chatbot.context.MCPContextManager;
import com.example.ai_chatbot.entity.BisCondition;
import com.example.ai_chatbot.entity.Message;
import com.example.ai_chatbot.integration.AIChatIntegration;
import com.example.ai_chatbot.integration.VO.PrizeRecordVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class     MCPChatService {
    @Autowired
    private MCPContextManager mcpContextManager;
    @Autowired
    private OpenaiClientService openaiClientService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataToNaturalLanguageService dataToNaturalLanguageService;

    @Autowired
    private AIChatIntegration aiChatIntegration;
    public String chat(String sessionId, String userInput,int userId){
        Message message = new Message();
        message.setRole("user");
        message.setContent(userInput);

        mcpContextManager.appendMessage(sessionId,message);

        List<Message> messageList = mcpContextManager.getContext(sessionId);

        String modelAnswer = openaiClientService.chatCompletion(messageList);
        try{
            BisCondition bisCondition = objectMapper.readValue(modelAnswer, BisCondition.class);
            bisCondition.setTargetUserId(userId);

            if(bisCondition.getIntentCode().equals("AWARD_RECORD")){
                List<PrizeRecordVO> voList = aiChatIntegration.getByUserAndDateRange(bisCondition.getStartTime(),
                        bisCondition.getEndTime(), bisCondition.getTargetUserId());
                String jsonData = objectMapper.writeValueAsString(voList);

                String modelResponse = dataToNaturalLanguageService.convert(userInput,jsonData);
                Message appendModelResponse = new Message();
                appendModelResponse.setContent(modelResponse);
                appendModelResponse.setRole("assistant");

               mcpContextManager.appendMessage(sessionId,appendModelResponse);

            }else if(bisCondition.getIntentCode().equals("PLAY_RECORD")){

            }else if(bisCondition.getIntentCode().equals("TOTAL_DURATION")){

            }else if(bisCondition.getIntentCode().equals("CHAT")){

            }
        }catch(Exception e){
            log.error(e.getMessage());
        }



//        Message responseMessage = new Message();
//        responseMessage.setRole("assistant");
//        responseMessage.setContent();
//        mcpContextManager.appendMessage(sessionId,responseMessage);
        return null;
    }
}
