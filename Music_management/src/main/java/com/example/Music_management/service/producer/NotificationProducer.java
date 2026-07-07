package com.example.Music_management.service.producer;

import com.example.Music_management.entity.NotificationMessage;
import com.example.Music_management.exception.SendMessageFailureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.Music_management.constant.Constant.NOTIFICATION;

@Service
public class NotificationProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private final static Logger logger = LoggerFactory.getLogger(NotificationProducer.class);
    public void send(NotificationMessage message){
        try{
            kafkaTemplate.send(NOTIFICATION,objectMapper.writeValueAsString(message));
            logger.info("Notification has been send to " + NOTIFICATION + " message is " + message);
        }catch(Exception e){
            logger.error("Notification has not been send due to failed serialization");
            throw new SendMessageFailureException("Notification has not been send due to failed serialization");
        }
    }
}
