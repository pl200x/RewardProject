package com.example.Music_management.service.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.Music_management.constant.Constant.MUSIC_TOPIC;

//用于异步推荐
@Service
public class AddMusicProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    private final static Logger logger = LoggerFactory.getLogger(AddMusicProducer.class);
    public void send(String content){
        kafkaTemplate.send(MUSIC_TOPIC,content);
        logger.info("Message has been sent to: " + MUSIC_TOPIC +"content is: "+ content);
    }
}
