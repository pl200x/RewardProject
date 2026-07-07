package com.example.Multi_reward.service;

import com.example.Multi_reward.constant.Constants;
import com.example.Multi_reward.service.producer.RetryProducer;
import com.example.Multi_reward.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class Scheduling {
    @Autowired
    private CheckScheduleService checkScheduleService;
    private final static Logger logger = LoggerFactory.getLogger(RetryProducer.class);
//    @Scheduled(fixedDelay = 2000L)
//    public void firstSchedule(){
//        System.out.println("doing schedule task");
//    }
//    @Scheduled(cron = "*/5 * * * * *")
//    //秒(0-59,*代表任何,/)，分(0-59,*代表任何,/)，时，日，月，周
//    public void secondSchedule(){
//        System.out.println("doing second schedule task");
//    }
//@Scheduled(cron = "20 */1 * * * *")
////秒(0-59,*代表任何,/)，分(0-59,*代表任何,/)，时，日，月，周
//public void thirdSchedule(){
//    System.out.println("doing third schedule task");
//    System.out.println(DateTimeUtil.format(new Date(), Constants.PATTERN));
//}
////每周周五早上七点29分第五秒
////@Scheduled(cron = "5 29 7 * * */1")
//@Scheduled(cron = "25 32 7 ? * FRI")
////秒(0-59,*代表任何,/)，分(0-59,*代表任何,/)，时，日，月，周
//public void fourthSchedule(){
//    System.out.println("doing fourth schedule task");
//    System.out.println(DateTimeUtil.format(new Date(), Constants.PATTERN));
//}
    @Scheduled(cron = "0 */1 * * * *")
    public void scheduledCheck(){
        logger.info("event=SCHEDULE_CHECK_START time={}", DateTimeUtil.format(new Date(), Constants.PATTERN));
        checkScheduleService.checkAmount();
        checkScheduleService.checkLastMinute();
    }
}
