package com.example.Multi_reward.util;

import com.example.Multi_reward.entity.Configuration;
import com.example.Multi_reward.exception.ConfigurationNotExistException;
import com.example.Multi_reward.service.ConfigurationService;
import org.apache.commons.jexl3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RewardCalculator {
    //calculate stage and amount
    @Autowired
    private ConfigurationService configurationService;
    public int evaluateAmount(String code, int totalDuration){
        JexlEngine jexl = new JexlBuilder().create();
        Configuration config = configurationService.queryByCode(code);
        if(config == null){
            throw new ConfigurationNotExistException("configuration " + code + " is not exist!");
        }
        String s = config.getRule();
        JexlExpression e = jexl.createExpression(s);
        JexlContext context = new MapContext();
        context.set("totalDuration",totalDuration);
        int result = (int) e.evaluate(context);
        return result;
    }
    public int evaluateStage(String code, int totalDuration){
        JexlEngine jexl = new JexlBuilder().create();
        Configuration config = configurationService.queryByCode(code);
        if(config == null){
            throw new ConfigurationNotExistException("configuration " + code + " is not exist!");
        }
        String s = config.getRule();
        JexlExpression e = jexl.createExpression(s);
        JexlContext context = new MapContext();
        context.set("totalDuration",totalDuration);
        int result = (int) e.evaluate(context);
        return result;
    }
}
