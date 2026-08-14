package com.example.ai_chatbot.service;

import com.example.ai_chatbot.context.MCPContextManager;
import com.example.ai_chatbot.entity.BisCondition;
import com.example.ai_chatbot.entity.Message;
import com.example.ai_chatbot.entity.OpenaiRequest;
import com.example.ai_chatbot.entity.OpenaiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OpenaiClientService {
    private static final Logger logger = LoggerFactory.getLogger(OpenaiClientService.class);
    //@Value("${spring.ai.openai.api-url}")
    private String apiUrl;
    //@Value("${spring.ai.openai.api-key}")
    private String apiKey;
    //@Value("${spring.ai.openai.model}")
    private String model;
    //@Value("${spring.ai.openai.temperature}")
    private Double temperature;

    @Autowired
    private MCPContextManager mcpContextManager;

    @Autowired
    private ObjectMapper objectMapper;

    public String chatCompletion(List<Message> messages) {
        try{
            OpenaiRequest openaiRequest = new OpenaiRequest();
            openaiRequest.setMessages(messages);
            openaiRequest.setModel(model);


            String jsonBody = objectMapper.writeValueAsString(openaiRequest);
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            RequestBody requestBody = RequestBody.create(jsonBody, mediaType);

            Request request  = new Request.Builder()
                    .url(apiUrl)
                    .header("Content-Type", "application/json")
                    .header("Authorization","Bearer " + apiKey)
                    .post(requestBody)
                    .build();


                Response response = okHttpClient().newCall(request).execute();
                if(!response.isSuccessful() || response.body() == null){
                    logger.error("response failed");
                }

                ResponseBody body = response.body();
                String string = body.string();
                OpenaiResponse openaiResponse = objectMapper.readValue(string, OpenaiResponse.class);
                String answer =  openaiResponse.getChoices().get(0).getMessage().getContent();
                //BisCondition bisCondition = objectMapper.readValue(answer, BisCondition.class);

                return answer;

        }catch(Exception e){
                logger.error("convert failure");
                return null;
            }
    }

    private OkHttpClient okHttpClient(){
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60,TimeUnit.SECONDS)
                .build();
    }

}
