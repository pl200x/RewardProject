package com.example.ai_chatbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
public class JsonConfig {
    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

}
