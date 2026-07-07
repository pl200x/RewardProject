package com.example.PrizeCenter.service.impl;

import com.example.PrizeCenter.annotation.DistributedLock;
import com.example.PrizeCenter.aspect.DistributedLockAspect;
import com.example.PrizeCenter.config.RedissonConfig;
import com.example.PrizeCenter.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private RedissonClient redissonClient;

    private static final Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

    @DistributedLock(prefix = "lock:order:", key = "'test'")
    @Override
    public void test() {
        String lockKey = "test:lock:"+"unique";


//        try{
//            boolean isLock = rlock.tryLock(3,4, TimeUnit.SECONDS);
//            if(!isLock){
//                //logger.warn("acquire lock failed");
//                throw new RuntimeException("Failed");
//            }
//            logger.info("lock successfully! Start task...");
//            Thread.sleep(3000L);
//            logger.info("Task finished");
//        }catch(InterruptedException e){
//            logger.info("e.getMessage()");
//        }finally{
//            if(rlock.isHeldByCurrentThread()){
//                rlock.unlock();
//                logger.info("Lock released");
//            }
//        }
        logger.info("lock successfully! Start task...");
        try{
            Thread.sleep(3000L);
        }catch(Exception e){

        }
        logger.info("Task finished");
    }
}
