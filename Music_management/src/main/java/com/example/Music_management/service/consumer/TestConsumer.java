package com.example.Music_management.service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestConsumer {
    private final static Logger logger = LoggerFactory.getLogger(TestConsumer.class);
    @KafkaListener(topics = "test",groupId = "my-group")
    public void listen(String message){
        logger.info("Message listened: " + message);
    }
}
