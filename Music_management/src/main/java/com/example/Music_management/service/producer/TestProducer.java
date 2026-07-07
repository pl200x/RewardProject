package com.example.Music_management.service.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    private final static Logger logger = LoggerFactory.getLogger(TestProducer.class);
    //topic表示消息目的地，content是内容
    public void send(String content){
        kafkaTemplate.send("test",content);
        logger.info("Message has been send, topic is: test," + " content is: "+ content);
    }
}
