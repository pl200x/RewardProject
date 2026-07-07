package com.example.PrizeCenter.Repository;

import com.example.PrizeCenter.entity.Prize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PrizeRepository {
    @Autowired
    private RedisTemplate redisTemplate;
    public Prize getByCode(String code){
        String key = buildPrizeCodeKey(code);
        return (Prize)redisTemplate.opsForValue().get(key);
    }
    public Prize getById(int id){
        String key = buildPrizeIdKey(id);
        return (Prize)redisTemplate.opsForValue().get(key);
    }
    public void addPrizeByCode(Prize prize){
        String codeKey = buildPrizeCodeKey(prize.getCode());
        redisTemplate.opsForValue().set(codeKey,prize);
    }
    public void addPrizeById(Prize prize){
        String codeKey = buildPrizeIdKey(prize.getId());
        redisTemplate.opsForValue().set(codeKey,prize);
    }
    public void deletePrize(String code, int id){
        String codeKey = buildPrizeCodeKey(code);
        String idKey = buildPrizeIdKey(id);
        redisTemplate.delete(codeKey);
        redisTemplate.delete(idKey);
    }
    private String buildPrizeCodeKey(String code){
        return "prize:" + code;
    }
    private String buildPrizeIdKey(int id){
        return "prize:" + id;
    }
}
