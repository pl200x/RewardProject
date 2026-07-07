package com.example.Music_management.controller;

import com.example.Music_management.service.consumer.TestConsumer;
import com.example.Music_management.service.producer.TestProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("producer")
public class KafkaTestController {
    @Autowired
    private TestProducer testProducer;
    private final static Logger logger = LoggerFactory.getLogger(TestProducer.class);

    @GetMapping("/send")
    public void send(String content){
        try {
            testProducer.send(content);
        }catch(Exception e){
            logger.error(e.toString());
        }
    }
}
